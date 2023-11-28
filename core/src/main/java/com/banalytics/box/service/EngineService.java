package com.banalytics.box.service;

import com.banalytics.box.TimeUtil;
import com.banalytics.box.api.integration.form.FormModel;
import com.banalytics.box.api.integration.webrtc.channel.events.AbstractEvent;
import com.banalytics.box.module.*;
import com.banalytics.box.module.cloud.portal.PortalIntegrationConfiguration;
import com.banalytics.box.module.cloud.portal.PortalIntegrationThing;
import com.banalytics.box.module.utils.form.FormUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.map.PassiveExpiringMap;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.reflections.Reflections;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.info.BuildProperties;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import javax.annotation.Resources;
import java.io.File;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.banalytics.box.service.SystemThreadsService.STARTUP_EXECUTOR;
import static com.banalytics.box.service.SystemThreadsService.SYSTEM_TIMER;

@RequiredArgsConstructor
@Slf4j
@Component
public class EngineService implements BoxEngine, InitializingBean {
    @Value("${config.instance.root}")
    private File homeDir;

    @Value("${config.instance.root}/config")
    private File configRoot;

//    @Value("classpath:/i18n/*.json")
//    @Value("file:/resources/i18n/*.json")
//    public Resource[] i18nResources;

    private final BeanFactory beanFactory;

    private final BuildProperties buildProperties;

    private final TaskService taskService;
    private final LocalUserService localUserService;
    private final JpaService jpaService;

    Set<Class<? extends AbstractEvent>> eventTypeClasses;

    private final TimerTask garbageCollectorTask = new TimerTask() {
        @Override
        public void run() {
            SYSTEM_TIMER.purge();
            System.gc();
        }
    };


//    @Override
//    public Resource[] i18nResources() {
//        return this.i18nResources;
//    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Reflections r = new Reflections("com.banalytics.box.api.integration.webrtc.channel.events");
        eventTypeClasses = r.getSubTypesOf(AbstractEvent.class);

        SYSTEM_TIMER.schedule(garbageCollectorTask, 1200000, 120000);
    }

    private UUID environmentUUID;

    public UUID getEnvironmentUUID() {
        if (environmentUUID == null) {
            PortalIntegrationThing portalIntegrationThing = taskService.getThing(PortalIntegrationConfiguration.THING_UUID);
            environmentUUID = portalIntegrationThing.getEnvironmentUUID();
        }
        return environmentUUID;
    }

    @Override
    public Instance getPrimaryInstance() {
        return taskService.getPrimaryInstance();
    }

    @Override
    public <T extends Thing<?>> T getThing(UUID uuid) {
        return taskService.getThing(uuid);
    }

    @Override
    public List<Thing<?>> findThings(Class<?>... standards) throws Exception {
        List<Thing<?>> result = new ArrayList<>();
        for (Thing<?> thing : taskService.getThings()) {
            for (Class<?> standardInterface : standards) {
                if (standardInterface.isAssignableFrom(thing.getClass())) {
                    result.add(thing);
                    break;
                }
            }
        }
        return result;
    }

    public List<Thing<?>> findThingsByStandard(Class<?>... interfaces) {
        return taskService.findThingsByStandard(interfaces);
    }

    @Override
    public ITask<?> findTask(UUID taskUuid) {
        return taskService.findTaskByUuid(taskUuid);
    }

    @Override
    public <T> List<T> findTasksByInterfaceSupport(Class<T> interfaceClass) {
        List<T> result = new ArrayList<>();
//        Thing<?> thing = taskService.getThing(thingUuid);
//        thing.getSubscribers().forEach(subscriber -> {
//            if (subscriber instanceof AbstractListOfTask) {
        AbstractListOfTask<?> instance = getPrimaryInstance();
        Collection<AbstractTask<?>> tasksHierarchy = instance.selfAndSubTasks();
        for (AbstractTask<?> task : tasksHierarchy) {
            if (interfaceClass.isAssignableFrom(task.getClass())) {
                result.add((T) task);
            }
        }
//            }
//        });

        return result;
    }

    @Override
    public File applicationHomeFolder() {
        return homeDir;
    }

    @Override
    public File applicationConfigFolder() {
        return configRoot;
    }

    @Override
    public BuildProperties getBuildProperties() {
        return buildProperties;
    }

    @Override
    public <T> List<T> findThingInstances(Class<T> instanceClass) throws Exception {
        List<T> result = new ArrayList<>();
        for (Thing<?> thing : taskService.getThings()) {
            if (instanceClass.isAssignableFrom(thing.getClass())) {
                result.add((T) thing);
            }
        }
        return result;
    }

    @Override
    public Collection<Class<? extends ITask>> findTaskClassesByInterface(Class<?> iface) {
        Collection<Class<? extends ITask>> result = new ArrayList<>();
        for (Class<? extends ITask> taskCls : taskService.supportedTaskClasses()) {
            if (iface.isAssignableFrom(taskCls)) {
                result.add(taskCls);
            }
        }
        return result;
    }

    @Override
    public Collection<? extends Thing<?>> findThings() throws Exception {
        return taskService.getThings();
    }

    @Override
    public Collection<AbstractTask<?>> instances() {
        return taskService.instances();
    }

    @Override
    public List<AbstractTask<?>> findSubTasks(UUID parentTaskUuid) {
        ITask<?> parentTask = taskService.findTaskByUuid(parentTaskUuid);
        if (parentTask == null) {
            return Collections.emptyList();
        }
        if (parentTask instanceof AbstractListOfTask parent) {
            return parent.getSubTasks();
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public FormModel describeClass(String className) {
        try {
            Class<?> cls = Class.forName(className);
            if (AbstractEvent.class.isAssignableFrom(cls)) {
                return FormUtils.describe(
                        beanFactory,
                        cls.getDeclaredConstructor().newInstance()
                );
            }
            final IUuid config;
            if (AbstractTask.class.isAssignableFrom(cls)) {
                Class<AbstractTask<?>> clazz = (Class<AbstractTask<?>>) cls;
                AbstractTask<?> task = ITask.blankOf(clazz, null, null);
                config = task.getConfiguration();
            } else if (AbstractThing.class.isAssignableFrom(cls)) {
                Class<AbstractThing<?>> clazz = (Class<AbstractThing<?>>) cls;
                Thing<?> thing = Thing.blankOf(clazz, null);
                config = thing.getConfiguration();
            } else {
                throw new RuntimeException("Class " + className + " doesn't support configuration");
            }

            return FormUtils.describe(
                    beanFactory,
                    config
            );
        } catch (Throwable e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public AbstractTask<?> saveOrUpdateTask(UUID parentTaskUuid, UUID taskUuid, String taskClass, Map<String, Object> configuration) throws Exception {
        return taskService.saveOrUpdateTask(parentTaskUuid, taskUuid, taskClass, configuration);
    }

    @Override
    public void startTask(UUID taskUuid) throws Exception {
        taskService.startTask(taskUuid);
    }

    @Override
    public void stopTask(UUID taskUuid) throws Exception {
        taskService.stopTask(taskUuid);
    }

    @Override
    public void startThing(UUID thingUuid) throws Exception {
        taskService.startThing(thingUuid);
    }

    @Override
    public void stopThing(UUID thingUuid) throws Exception {
        taskService.stopThing(thingUuid);
    }

    @Override
    public Thing<?> saveOrUpdateThing(UUID thingUuid, String thingClass, Map<String, Object> configuration) throws Exception {
        return taskService.saveOrUpdateThing(thingUuid, thingClass, configuration);
    }

    @Override
    public void deleteThing(UUID thingUuid) throws Exception {
        taskService.deleteThing(thingUuid);
    }

    @Override
    public void deleteTask(UUID taskUuid) throws Exception {
        taskService.deleteTask(taskUuid);
    }

    @Override
    public Collection<Class<?>> supportedThings() {
        return taskService.supportedThings();
    }

    @Override
    public Set<Class<? extends ITask>> supportedTaskClasses() {
        Instance inst = taskService.getPrimaryInstance();
        Collection<ITask<?>> allTasks = inst.subtasksAndMe();

        Set<Class<? extends ITask>> result = new HashSet<>();

        for (ITask<?> task : allTasks) {
            if (task.hidden()) {
                continue;
            }
            result.add(task.getClass());
        }

        return result;
//        return taskService.supportedTaskClasses();
    }

    @Override
    public Set<Class<? extends AbstractEvent>> eventTypeClasses() {
        return eventTypeClasses;
    }

    public Set<String> eventTypeClassesStr() {
        return eventTypeClasses.stream().map(Class::getName).collect(Collectors.toSet());
    }

/*    @Override
    public Collection<String> supportedSubtasks(UUID parentTaskUuid) {
        if (parentTaskUuid == null) {
            return List.of(Instance.class.getName());
        }

        ITask<?> task = taskService.findTaskByUuid(parentTaskUuid);
        if (task.getState() == State.RUN) {
            return taskService.findSupportedSubtasksForTask(task);
        } else {
            return List.of();
        }
    }*/

    @Override
    public Collection<AbstractAction<?>> findActionTasks() {
        return taskService.findActionTasks();
    }

    @Override
    public <T extends Thing<?>> T getThingAndSubscribe(UUID uuid, InitShutdownSupport initShutdownSupport) throws Exception {
        T thing = taskService.getThing(uuid);
        if (thing == null) {
            throw new Exception("thing.error.notFound");
        }
        thing.subscribe(initShutdownSupport);
        return thing;
    }

    @Override
    public <T> T getBean(Class<T> beanClass) {
        return beanFactory.getBean(beanClass);
    }

    @Override
    public <T> T getBean(String beanName) {
        return (T) beanFactory.getBean(beanName);
    }

    private final List<Consumer<AbstractEvent>> eventConsumers = new CopyOnWriteArrayList<>();

    @Override
    public void addEventConsumer(Consumer<AbstractEvent> eventConsumer) {
        this.eventConsumers.add(eventConsumer);
        log.info("Event consumer added: {}", eventConsumer);
    }

    @Override
    public void removeEventConsumer(Consumer<AbstractEvent> eventConsumer) {
        this.eventConsumers.remove(eventConsumer);
        log.info("Event consumer removed: {}", eventConsumer);
    }

    @Override
    public void fireEvent(AbstractEvent event) {
        try {
            if (event.getEnvironmentUuid() == null) {//if environment not defined it's local environment
                event.setEnvironmentUuid(getEnvironmentUUID());
            }

            for (Consumer<AbstractEvent> consumer : this.eventConsumers) {
                consumer.accept(event);
            }
        } catch (Throwable e) {
            log.error(e.getMessage(), e);
        }
    }

    private final List<Runnable> postInitializingHook = new ArrayList<>();

    @Override
    public void addPostInitializingHook(Runnable hook) {
        postInitializingHook.add(hook);
    }

    @Override
    public Object serviceCall(String serviceName, String methodName, String arg) throws Exception {
        Object ref = beanFactory.getBean(serviceName);
        if (arg.contains(",")) {
            String[] args = arg.split(",");
            return MethodUtils.invokeMethod(ref, methodName, args);
        } else {
            return MethodUtils.invokeMethod(ref, methodName, arg);
        }
    }

    @Override
    public void persistPrimaryInstance() throws Exception {
        taskService.persistPrimaryInstance();
    }

    /**
     * On application started - initialize things & tasks
     */
    @EventListener
    public void onApplicationEvent(ContextRefreshedEvent event) throws Exception {
        taskService.setEngine(this);

        List<Instance> loadedInstances = taskService.load();

        taskService.startInstances(loadedInstances);

        for (Runnable runnable : postInitializingHook) {
            runnable.run();
        }
        postInitializingHook.clear();

        //On close jvm shutdown things & Tasks
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("Shutdown Hook Started");
            taskService.shutdown();
            log.info("Shutdown Hook Finished");
        }));
    }

    @Override
    public void startBillableFeatures() throws Exception {
        while (STARTUP_EXECUTOR.getActiveCount() > 0) {//wait while things not started
            Thread.sleep(1000);
        }
        taskService.startBillableTasks();
        taskService.startBillableThings();
    }

    @Override
    public void stopBillableFeatures() throws Exception {
        while (STARTUP_EXECUTOR.getActiveCount() > 0) {//wait while things not started
            Thread.sleep(1000);
        }
        taskService.stopBillableTasks();
        taskService.stopBillableThings();
    }

    @Override
    public JpaService getJpaService() {
        return jpaService;
    }

    @Override
    public File getModelPath(String modelsRoot, String subModelName) throws Exception {
        return taskService.getModelFolder(modelsRoot, subModelName);
    }

    @Override
    public boolean verifyPassword(String password) {
        return localUserService.verifyPassword(password);
    }

    @Override
    public void changePassword(String oldPassword, String newPassword) {
        localUserService.changePassword(oldPassword, newPassword);
    }

    @Override
    public void resetPassword(String newPassword) {
        localUserService.resetPassword(newPassword);
    }

    @Override
    public AbstractTask<?> buildTask(String clazz, Map<String, Object> configuration, AbstractListOfTask<?> parent) throws Exception {
        return taskService.buildTask(clazz, configuration, parent);
    }
}
