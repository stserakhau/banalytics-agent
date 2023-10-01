package com.banalytics.box.module;

import com.banalytics.box.api.integration.form.ComponentType;
import com.banalytics.box.api.integration.form.annotation.UIComponent;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;

import java.io.File;
import java.util.*;

import static com.banalytics.box.module.Thing.StarUpOrder.BUSINESS_LONG_START;
import static com.banalytics.box.service.SystemThreadsService.STARTUP_EXECUTOR;

@Slf4j
public final class Instance extends AbstractListOfTask<Instance.Config> {

    private File instanceConfigFile;

    private final Map<UUID, Thing<?>> thingsMap = new HashMap<>();

    @Override
    public String getTitle() {
        return this.configuration.getTitle();
    }

    public Instance(BoxEngine metricDeliveryService, AbstractListOfTask<?> parent) {
        super(metricDeliveryService, parent);
    }

    @Override
    public void doInit() throws Exception {
        for (Thing<?> thing : getThings()) {
            thing.init();
        }
        super.doInit();
    }

    @Override
    public void doStop() throws Exception {
        for (Thing<?> thing : getThings()) {
            thing.stop();
        }

        super.doStop();
    }

    @Override
    protected boolean canStart() {
        return true;
    }

    @Override
    public void doStart(boolean ignoreAutostartProperty, boolean startChildren) throws Exception {
        // start things and their subscribers

        List<Thing<?>> orderedThings = getThings();

        orderedThings.sort((t1, t2) -> {
            Order order1 = t1.getClass().getAnnotation(Order.class);
            int orderVal1 = order1 == null ? BUSINESS_LONG_START : order1.value();
            Order order2 = t2.getClass().getAnnotation(Order.class);
            int orderVal2 = order2 == null ? BUSINESS_LONG_START : order2.value();
            return orderVal1 - orderVal2;
        });

        List<Thing<?>> longStartThing = new ArrayList<>();

        log.info("Things startup sequence: ");
        for (Thing<?> thing : orderedThings) {
            Order order = thing.getClass().getAnnotation(Order.class);
            int orderVal = order == null ? BUSINESS_LONG_START : order.value();
            log.info("\t\t {}: {}", orderVal, thing.getSelfClassName() + ": " + thing.getTitle());
            if (orderVal >= BUSINESS_LONG_START) {
                longStartThing.add(thing);
            } else {
                thing.start(ignoreAutostartProperty, false);
            }
        }
        while (STARTUP_EXECUTOR.getActiveCount() > 0) {//wait while things not started
            Thread.sleep(1000);
        }

        for (Thing<?> thing : longStartThing) {
            thing.start(ignoreAutostartProperty, false);
        }
        while (STARTUP_EXECUTOR.getActiveCount() > 0) {//wait while things not started
            Thread.sleep(1000);
        }

        // Start actions
//        for (AbstractTask<?> subTask : getSubTasks()) {
//            if (subTask instanceof IAction) {
//                subTask.start(ignoreAutostartProperty, false);
//            }
//        }
        log.info("Tasks startup sequence: ");
        super.doStart(ignoreAutostartProperty, true);
    }

    public File instanceConfigFile() {
        return instanceConfigFile;
    }

    public void instanceConfigFile(File instanceConfigFile) {
        this.instanceConfigFile = instanceConfigFile;
    }

    public void addAllThings(Collection<Thing<?>> things) {
        for (Thing<?> thing : things) {
            this.thingsMap.put(thing.getUuid(), thing);
        }
    }

    public Map<UUID, Thing<?>> getThingsMap() {
        return thingsMap;
    }

    public List<Thing<?>> getThings() {
        return new ArrayList<>(thingsMap.values());
    }

    public <T extends Thing<?>> T getThing(UUID uuid) {
        return (T) thingsMap.get(uuid);
    }

    @Override
    public UUID getSourceThingUuid() {
        return null;
    }

    @Override
    public Map<String, Class<?>> inSpec() {
        return null;
    }

    @Override
    protected void fireRestart() {
        //empty - disable restart of the instance on falures in initialization time
    }

    @Getter
    @Setter
    public static class Config extends AbstractConfiguration {

        @UIComponent(index = 1, type = ComponentType.text_input, required = true)
        public String title;
    }
}
