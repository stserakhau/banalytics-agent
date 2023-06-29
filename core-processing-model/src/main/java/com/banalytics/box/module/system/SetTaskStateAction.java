package com.banalytics.box.module.system;

import com.banalytics.box.module.*;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Set;

import static com.banalytics.box.api.integration.utils.CommonUtils.DEFAULT_OBJECT_MAPPER;
import static com.banalytics.box.module.ConverterTypes.TYPE_NODE_CONFIGURATION;

@Slf4j
public class SetTaskStateAction extends AbstractAction<SetTaskStateActionConfiguration> implements PropertyValuesProvider {
    public SetTaskStateAction(BoxEngine engine, AbstractListOfTask<?> parent) {
        super(engine, parent);
    }

    @Override
    protected boolean isFireActionEvent() {
        return true;
    }

    @Override
    public String getTitle() {
        if (configuration.targetTask != null) {
            ITask<?> targetTask = engine.findTask(configuration.targetTask);
            if (targetTask == null) {
                return "undefined";
            }
            return targetTask.getSelfClassName() + "~" + targetTask.getTitle() + ": " + configuration.title;
        } else {
            return "undefined";
        }
    }

    @Override
    public Thing<?> getSourceThing() {
        ITask<?> targetTask = engine.findTask(configuration.targetTask);
        if (targetTask == null) {
            return null;
        }
        return targetTask.getSourceThing();
    }

    @Override
    public void doAction(ExecutionContext ctx) throws Exception {
        ITask<?> task = engine.findTask(configuration.targetTask);
        Map<String, Object> taskConfig = DEFAULT_OBJECT_MAPPER.readValue(configuration.taskFormData, TYPE_NODE_CONFIGURATION);
        engine.saveOrUpdateTask(task.parent().getUuid(), task.getUuid(), task.getSelfClassName(), taskConfig);
    }

    @Override
    public Set<String> provideValues(String propertyName) {
        if (configuration.targetTask != null) {
            ITask<?> targetTask = engine.findTask(configuration.targetTask);
            if (targetTask instanceof PropertyValuesProvider) {
                try {
                    String config = this.configuration.taskFormData;
                    Map<String, Object> conf = DEFAULT_OBJECT_MAPPER.readValue(config, TYPE_NODE_CONFIGURATION);
                    PropertyValuesProvider pvp = (PropertyValuesProvider) engine.buildTask(targetTask.getSelfClassName(), conf, targetTask.parent());
                    return pvp.provideValues(propertyName);
                } catch (Throwable e) {
                    log.error(e.getMessage(), e);
                    return Set.of();
                }
            }
        }
        return Set.of();
    }
}
