package com.banalytics.box.service.helper;

import com.banalytics.box.module.*;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static com.banalytics.box.module.utils.form.FormUtils.buildConfig;
import static com.banalytics.box.module.utils.form.FormUtils.populate;

public class InstanceXMLHandler extends DefaultHandler {
    private final BoxEngine metricDeliveryService;

    public InstanceXMLHandler(BoxEngine metricDeliveryService) {
        this.metricDeliveryService = metricDeliveryService;
    }

    private final LinkedList<Object> stack = new LinkedList<>();

    private final List<Thing<?>> things = new CopyOnWriteArrayList<>();

    private Instance instance;

    public Instance getInstance() {
        return instance;
    }

    int skipCounter = 0;

    StringBuilder errorMessageBuilder = new StringBuilder();

    @Override
    public void startElement(String uri, String localName, String clazz, Attributes attributes) throws SAXException {
        if (skipCounter > 0) {
            skipCounter++;
            return;
        }
        try {
            Class cls = Class.forName(clazz);

            if (ITask.class.isAssignableFrom(cls)) {
                Class<? extends AbstractTask<?>> dsgc = (Class<? extends AbstractTask<?>>) cls;
                ITask<?> iTask = ITask.blankOf(dsgc, metricDeliveryService, null);
                populate(iTask.getConfiguration(), attributes);
                stack.push(iTask);
            } else if (Thing.class.isAssignableFrom(cls)) {
                Class<? extends Thing<?>> thingClass = (Class<? extends Thing<?>>) cls;
                Thing<?> thing = thingClass.getDeclaredConstructor(new Class[]{BoxEngine.class}).newInstance(metricDeliveryService);
                populate(thing.getConfiguration(),attributes);
                things.add(thing);
                stack.push(thing);
            } else {
                throw new Exception("Class not supported by engine: " + cls);
            }
        } catch (ClassNotFoundException e) {//if class not found skip processing of the node
            errorMessageBuilder.append("Unknown class: ").append(clazz).append("\n");
            skipCounter++;
        } catch (Exception e) {
            throw new SAXException(e);
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (skipCounter > 0) {
            skipCounter--;
            return;
        }
        Object current = stack.pop();
        if (current instanceof AbstractTask) {
            AbstractTask<?> task = (AbstractTask<?>) current;
            if (stack.isEmpty()) {
                instance = (Instance) task;
                if (!errorMessageBuilder.isEmpty()) {
                    instance.state = State.ERROR;
                    instance.stateDescription = errorMessageBuilder.toString();
                }
                instance.addAllThings(things);
                things.clear();
                return;
            }
            AbstractListOfTask<?> parentSubtasks = (AbstractListOfTask<?>) stack.getFirst();
            parentSubtasks.addSubTask(task);
            task.parent(parentSubtasks);
        }
    }

    public static String toXML(Instance instance) {
        StringBuilder sb = new StringBuilder(20 * 1024);
        sb.append("<?xml version='1.0' encoding='utf-8' ?>\n");
        String clazz = instance.getClass().getName();
        sb.append('<').append(clazz);
        buildConfig(sb, instance.getConfiguration());
        sb.append(">\n");

        for (Thing<?> thing : instance.getThings()) {
            sb.append(toXML(thing));
        }

        for (ITask<?> st : instance.getSubTasks()) {
            buildTaskTree(sb, st, "    ");
        }

        sb.append("</").append(clazz).append('>');

        return sb.toString();
    }

    private static void buildTaskTree(StringBuilder sb, ITask<?> root, String tab) {
        String clazz = root.getClass().getName();
        sb.append(tab).append('<').append(clazz);
        buildConfig(sb, root.getConfiguration());
        if (root instanceof AbstractListOfTask) {
            sb.append(">\n");

            AbstractListOfTask<?> task = (AbstractListOfTask<?>) root;
            for (ITask<?> st : task.getSubTasks()) {
                buildTaskTree(sb, st, tab + "    ");
            }

            sb.append(tab).append("</").append(clazz).append(">\n");
        } else {
            sb.append(" />\n");
        }
    }

    private static String toXML(Thing thing) {
        StringBuilder sb = new StringBuilder(20 * 1024);
        String clazz = thing.getClass().getName();
        sb.append("    <").append(clazz);
        buildConfig(sb, thing.getConfiguration());
        sb.append("/>\n");
        return sb.toString();
    }
}
