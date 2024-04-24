package com.banalytics.box.service;

import com.banalytics.box.LocalizedException;
import com.banalytics.box.api.integration.form.annotation.UIComponent;
import com.banalytics.box.api.integration.model.ComponentRelation;
import com.banalytics.box.api.integration.model.SubItem;
import com.banalytics.box.module.*;
import com.banalytics.box.module.cloud.portal.PortalIntegrationThing;
import com.banalytics.box.module.cloud.portal.suc.SoftwareUpgradeCenterThing;
import com.banalytics.box.module.events.EventHistoryThing;
import com.banalytics.box.module.network.DeviceDiscoveryThing;
import com.banalytics.box.module.storage.filesystem.ServerLocalFileSystemNavigator;
import com.banalytics.box.module.system.agent.JVMThing;
import com.banalytics.box.module.system.monitor.SystemMonitorThing;
import com.banalytics.box.module.webrtc.PortalWebRTCIntegrationThing;
import com.banalytics.box.module.webrtc.client.UserThreadContext;
import com.banalytics.box.service.helper.InstanceXMLHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.PropertyUtils;
import org.reflections.Reflections;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.ReflectionUtils;
import org.xml.sax.InputSource;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static com.banalytics.box.module.webrtc.client.channel.Constants.ALWAYS_REQUIRED_THINGS_UUID_SET;
import static com.banalytics.box.service.SystemThreadsService.STARTUP_EXECUTOR;
import static com.banalytics.box.service.SystemThreadsService.SYSTEM_TIMER;

@Slf4j
@RequiredArgsConstructor
@Service
public class TaskService implements InitializingBean {

    @Value("${config.instance.root}")
    private File banalyticsRoot;

    @Value("${config.instance.root}/config")
    private File configRoot;

    private File instancesRoot;

    private SAXParserFactory factory;

    private final Set<Class<? extends Thing>> availableThingsClasses = new HashSet<>();

    public final Map<Class<?>, Set<ComponentRelation>> componentsRelations = new HashMap<>();

    private final Map<Map<String, Class<?>>, Set<Class<? extends ITask>>> inSpecTaskMap = new HashMap<>();

    private boolean firstRun = false;

    private Instance primaryInstance;

    public Instance getPrimaryInstance() {
        return primaryInstance;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        factory = SAXParserFactory.newInstance();

        instancesRoot = new File(configRoot, "instances");
        if (!instancesRoot.exists()) {
            instancesRoot.mkdirs();
            firstRun = true;
        } else {
            String[] files = instancesRoot.list();
            if (files == null || files.length == 0) {
                firstRun = true;
            }
        }
        if (firstRun) {
            primaryInstance = new Instance(engine, null);
            primaryInstance.getConfiguration().setTitle("Primary");
            primaryInstance.addAllThings(List.of(
                    new PortalIntegrationThing(engine),
                    new SoftwareUpgradeCenterThing(engine),
                    new PortalWebRTCIntegrationThing(engine),
                    new ServerLocalFileSystemNavigator(engine),
                    new SystemMonitorThing(engine),
                    new EventHistoryThing(engine),
                    new DeviceDiscoveryThing(engine),
                    new JVMThing(engine)
            ));
            persistPrimaryInstanceImmediate();
        }

        //scan available tasks and build input specifications
        Reflections r = new Reflections("com.banalytics.box.module");
        {
            Map<Class<?>, Set<ComponentRelation>> itemSubItemsMap = new HashMap<>();
            Set<Class<? extends AbstractTask>> foundTasksSet = r.getSubTypesOf(AbstractTask.class);
            log.info("Found tasks:");
            for (Class<? extends AbstractTask> t : foundTasksSet) {
                if (t.isInterface()
                        || Modifier.isAbstract(t.getModifiers())
                        || t.getName().indexOf('$') > -1) {
                    continue;
                }
                log.info("  > " + t);

                Map<String, Class<?>> inSpec = ITask.blankOf(t, null, null).inSpec();

                inSpecTaskMap
                        .computeIfAbsent(
                                inSpec,
                                aClass -> new HashSet<>()
                        ).add(t);

                SubItem subItem = t.getDeclaredAnnotation(SubItem.class);
                if (subItem != null) {//task is sub item of another task
                    for (Class<?> cls : subItem.of()) {
                        Set<ComponentRelation> subItems = itemSubItemsMap.computeIfAbsent(cls, c -> new HashSet<>());
                        subItems.add(new ComponentRelation(t, subItem.group(), subItem.singleton()));
                    }
                }
            }

            itemSubItemsMap.forEach((item, subItems) -> {
                if (item.isInterface()
                        || Modifier.isAbstract(item.getModifiers())
                        || item.getName().indexOf('$') > -1) {
                    for(Class<?> cls : foundTasksSet) {
                        if(item.isAssignableFrom(cls)){
                            componentsRelations.put(cls, subItems);
                        }
                    }
                } else {
                    componentsRelations.put(item, subItems);
                }
            });
        }
        {
            Set<Class<? extends Thing>> set = r.getSubTypesOf(Thing.class);
            log.info("Found Things:");
            for (Class<? extends Thing> t : set) {
                if (t.isInterface()
                        || Modifier.isAbstract(t.getModifiers())
                        || t.getName().indexOf('$') > -1) {
                    continue;
                }
                log.info("  > " + t);

                availableThingsClasses.add(t);
            }
        }
    }

    public Collection<String> allTasksMap() {
        List<String> res = new ArrayList<>(30);
        bindTaskTreeToList(primaryInstance, res, false, 0);
        return res;
    }

    public Collection<String> nonActionTasksMap() {
        List<String> res = new ArrayList<>(30);
        bindTaskTreeToList(primaryInstance, res, true, 0);
        return res;
    }

    public Collection<String> nonConfigThingMap() {
        List<String> res = new ArrayList<>(30);
        for (Thing<?> thing : primaryInstance.getThings()) {
            res.add(thing.getUuid() + "~" + thing.getTitle());
        }
        return res;
    }

    private void bindTaskTreeToList(ITask<?> task, List<String> res, boolean excludeActions, int level) {
        if (excludeActions && task instanceof IAction) {
            return;
        }
        if (level > 0) {
            String key = task.getUuid().toString();
            StringBuilder title = new StringBuilder();
            title.append("\u00A0\u00A0".repeat(level - 1));
            title.append("~").append(task.getSelfClassName()).append("~ (").append(task.getTitle()).append(')');
            res.add(key + '~' + title);
        }
        if (task instanceof AbstractListOfTask<?> alt) {
            for (ITask<?> subTask : alt.getSubTasks()) {
                bindTaskTreeToList(subTask, res, excludeActions, level + 1);
            }
        }
    }

    public Collection<AbstractAction<?>> findActionTasks() {
        Collection<AbstractAction<?>> result = new HashSet<>();
        for (AbstractTask<?> task : applicationAllTasksMap.values()) {
            if (task instanceof AbstractAction a) {
                result.add(a);
            }
        }
        return result;
    }

    public List<Map<String, Object>> findActionTasksUI() {
        List<Map<String, Object>> result = new ArrayList<>();
        for (AbstractAction actionTask : findActionTasks()) {
            Map<String, Object> uiDetails = new HashMap<>();
            uiDetails.put("key", actionTask.getUuid());
            uiDetails.put("className", actionTask.getSelfClassName());
            uiDetails.put("title", actionTask.getTitle());
            uiDetails.putAll(actionTask.uiDetails());
            result.add(uiDetails);
        }
        return result;
    }

    /*public Collection<String> findSupportedSubtasksForTask(ITask<?> task) {
        if (task.getState() != State.RUN) {
            return Collections.emptyList();
        }
        Map<String, Class<?>> outSpec = task.runtimeOutSpec();

        Collection<String> tasks = new HashSet<>();

        if (outSpec == null) {
            Set<Class<? extends ITask>> supportedSubtasks = inSpecTaskMap.get(null);
            supportedSubtasks.forEach(t -> tasks.add(t.getName()));
            return tasks;
        }

        for (Map.Entry<Map<String, Class<?>>, Set<Class<? extends ITask>>> entry : inSpecTaskMap.entrySet()) {
            Map<String, Class<?>> in = entry.getKey();
            if (in == null) {
                continue;// skip null it was processed in previous block (root tasks)
            }
            int specSupportCnt = in.size();
            for (Map.Entry<String, Class<?>> e : in.entrySet()) {
                String varName = e.getKey();
                Class<?> varClass = e.getValue();
                if (StringUtils.isEmpty(varName)) {//if any variable name
                    for (Class<?> cls : in.values()) {
                        if (varClass.isAssignableFrom(cls)) {
                            specSupportCnt--;
                            break;
                        }
                    }
                } else {
                    Class<?> val = outSpec.get(varName);
                    if (val != null && e.getValue().isAssignableFrom(val)) {
                        specSupportCnt--;
                    }
                }
            }
            if (specSupportCnt == 0) {
                tasks.addAll(
                        entry.getValue().stream()
                                .map(Class::getName).toList()
                );
            }
        }

        return tasks;
    }*/

    /**
     * ========================================================
     * ============    INSTANCE       =========================
     * ========================================================
     */
    private final Map<UUID, AbstractTask<?>> uuidInstanceMap = new ConcurrentHashMap<>();

    public Collection<AbstractTask<?>> instances() {
        return Set.of(primaryInstance);
    }

    /**
     * ========================================================
     * ============   APPLICATION TASKS   =====================
     * ========================================================
     */
    private final Map<UUID, AbstractTask<?>> applicationRootTasks = new ConcurrentHashMap<>();
    private final Map<UUID, AbstractTask<?>> applicationAllTasksMap = new ConcurrentHashMap<>();

    public ITask<?> findTaskByUuid(UUID taskUuid) {
        return applicationAllTasksMap.get(taskUuid);
    }

    private BoxEngine engine;

    public void setEngine(BoxEngine engine) throws Exception {
        if (this.engine != null) {
            throw new Exception("Already initialized");
        }
        this.engine = engine;
    }

    public List<Instance> load() throws Exception {
        List<Instance> instancesList = new ArrayList<>();
        log.info("Instances loading started...");
        File[] instanceConfigs = instancesRoot
                .listFiles(pathname -> pathname.isFile() && pathname.getName().endsWith(".xml"));

        if (instanceConfigs.length > 1) {
            throw new Exception("Node doesn't support multi-instance mode. Future feature for clustering.");
        }

        for (File instanceFile : instanceConfigs) {
            log.info("Start instance initialization: {}", instanceFile.getAbsolutePath());
            Instance instance = null;
            try {
                primaryInstance = instance = loadInstance(instanceFile);
                instancesList.add(instance);
                log.info(">> Instance loaded: " + instance.getUuid());
                break;//support only one instance
            } catch (Throwable e) {
                log.error(">> Instance initialization failed: " + (instance == null ? "null" : instance.getUuid()), e);
                break;
            }
        }
        return instancesList;
    }

    public void startInstances(List<Instance> instancesList) throws Exception {
        {// start initialization Things first, Instances after (depends on Things)
            log.info("Instance initialization started...");
            for (Instance instance : instancesList) {
                instance.start(false, true);
            }

            while (STARTUP_EXECUTOR.getActiveCount() > 0) {
                log.info(">> Still in progress: {}", STARTUP_EXECUTOR.getActiveCount());
                Thread.sleep(1000);
            }
            log.info("Instances initialization done...");
        }
    }

    public void shutdown() {
        for (AbstractTask<?> it : instances()) {
            log.info("Instances shutdown started: {}", it);
            it.stop();
            log.info("Instances shutdown completed: {}", it);
        }
    }

    public void deleteTask(UUID taskUuid) throws Exception {
        AbstractTask<?> task = applicationAllTasksMap.remove(taskUuid);
        if (task == null) {
            throw new Exception("task.error.removed");
        }
        Set<UUID> removedUuids = task.selfDelete();

        boolean removeInstance = task instanceof Instance;
        if (removeInstance) {
            uuidInstanceMap.remove(taskUuid);
            removeInstance((Instance) task);
        } else {
            persistPrimaryInstance();
            removedUuids.forEach(applicationAllTasksMap::remove);
        }
    }

    private void removeInstance(Instance instance) {
        instance.instanceConfigFile().delete();
    }

    @Deprecated
    public ITask<?> updateTask(UUID taskUuid, Map<String, Object> configuration) throws Exception {
        AbstractTask<?> task = applicationAllTasksMap.get(taskUuid);
        Object taskConfig = task.getConfiguration();
        BeanWrapper taskW = new BeanWrapperImpl(taskConfig);
        for (Map.Entry<String, Object> property : configuration.entrySet()) {
            taskW.setPropertyValue(property.getKey(), property.getValue());
        }

        persistPrimaryInstance();

        task.restart();

        return task;
    }

    public AbstractTask<?> saveOrUpdateTask(UUID parentTaskUuid, UUID taskUuid, String taskClass, Map<String, Object> configuration) throws Exception {
        boolean isNew = taskUuid == null;
        AbstractListOfTask<?> parentTask = null;

        if (parentTaskUuid == null) {
            parentTaskUuid = primaryInstance.getUuid();
        }
        if (parentTaskUuid != null) {
            parentTask = (AbstractListOfTask<?>) applicationAllTasksMap.get(parentTaskUuid);
        }
        boolean restartNeed;
        AbstractTask<?> task;
        IConfiguration clonedConfig = null;
        if (isNew) {
            task = buildTask(taskClass, configuration, parentTask);

            validateUniquness(parentTask, task);

            applicationAllTasksMap.put(task.getUuid(), task);
            restartNeed = true;
        } else {
            task = applicationAllTasksMap.get(taskUuid);
            IConfiguration config = task.getConfiguration();
            clonedConfig = task.configClass.getDeclaredConstructor().newInstance();
            PropertyUtils.copyProperties(clonedConfig, config);
            restartNeed = updateNodeConfig(task.getConfiguration(), configuration);

            if (restartNeed) {
                task.stop();
            } else {//reload config only
                task.reloadConfig();
            }
        }
        try {
            task.configuration.validate();
        } catch (Throwable e) {
            if (clonedConfig != null) {
                PropertyUtils.copyProperties(task.configuration, clonedConfig);
            }
            throw e;
        }

        task.init();

        applicationRootTasks.put(task.getUuid(), task);
        uuidInstanceMap.put(task.getUuid(), task);

        if (isNew && parentTask != null) {
            //todo move add strategy to task implementation
            if (task.getClass().getName().equals("com.banalytics.box.module.bytedeco.task.watermark.WatermarkTask")) {
                parentTask.addSubTaskFirst(task);
            } else {
                parentTask.addSubTaskWithRule(task);
            }
        }
        persistPrimaryInstance();

        if (restartNeed) {
            STARTUP_EXECUTOR.submit(() -> {
                task.start(false, true);
            });
        }

        return task;
    }

    public AbstractTask<?> buildTask(String clazz, Map<String, Object> configuration, AbstractListOfTask<?> parent) throws Exception {
        Class<? extends AbstractTask<?>> taskClass = (Class<? extends AbstractTask<?>>) Class.forName(clazz);
        AbstractTask<?> task = ITask.blankOf(taskClass, this.engine, parent);
        Object taskConfig = task.getConfiguration();
        BeanWrapper taskW = new BeanWrapperImpl(taskConfig);
        for (Map.Entry<String, Object> property : configuration.entrySet()) {
            taskW.setPropertyValue(property.getKey(), property.getValue());
        }
        return task;
    }

    private Instance loadInstance(File instanceFile) throws Exception {
        InstanceXMLHandler instanceXMLHandler = new InstanceXMLHandler(this.engine);
        SAXParser saxParser = factory.newSAXParser();

        InputSource is = new InputSource(new InputStreamReader(new FileInputStream(instanceFile), StandardCharsets.UTF_8));
        saxParser.parse(is, instanceXMLHandler);
        Instance instance = instanceXMLHandler.getInstance();
        instance.instanceConfigFile(instanceFile);

        uuidInstanceMap.put(instance.getConfiguration().getUuid(), instance);
        applicationAllTasksMap.put(instance.getUuid(), instance);
        for (AbstractTask<?> task : instance.getSubTasks()) {
            if (!AbstractListOfTask.class.isAssignableFrom(task.getClass())) {
                applicationAllTasksMap.put(task.getUuid(), task);
            } else {
                applicationRootTasks.put(task.getUuid(), task);
                for (AbstractTask<?> subTask : ((AbstractListOfTask<?>) task).selfAndSubTasks()) {
                    applicationAllTasksMap.put(subTask.getUuid(), subTask);
                }
            }
        }

        return instance;
    }

    public synchronized void persistPrimaryInstance() {
        persistInstance1(primaryInstance, false);
    }

    private synchronized void persistPrimaryInstanceImmediate() {
        persistInstance1(primaryInstance, true);
    }

    private boolean persistInside = false;
    private TimerTask delayedPersistInstance;

    private void persistInstance1(final Instance instance, boolean immediate) {
        if (!persistInside && delayedPersistInstance != null) {
            delayedPersistInstance.cancel();
        }
        delayedPersistInstance = new TimerTask() {//dalay before persistence the configuration - take a chance to crash app if config unstable
            @Override
            public void run() {
                persistInside = true;
                try {
                    String instanceXML = InstanceXMLHandler.toXML(instance);
                    String instanceFileName = instance.getUuid() + "-" + System.currentTimeMillis() + ".xml";
                    File newInstanceFile = new File(instancesRoot, instanceFileName);
                    if (newInstanceFile.createNewFile()) {
                        try (FileWriter writer = new FileWriter(newInstanceFile)) {
                            writer.write(instanceXML);
                            writer.flush();
                        }
                        File oldInstanceFile = instance.instanceConfigFile();
                        instance.instanceConfigFile(newInstanceFile);
                        if (oldInstanceFile != null) {
                            if (!oldInstanceFile.delete()) {
                                throw new Exception("Old configuration wasn't removed: " + oldInstanceFile.getPath());
                            }
                        }
                    } else {
                        throw new RuntimeException("Can't create file: " + newInstanceFile.getAbsolutePath());
                    }
                } catch (Throwable e) {
                    log.error(e.getMessage(), e);
                }
            }
        };
        if (immediate) {
            delayedPersistInstance.run();
        } else {
            SYSTEM_TIMER.schedule(delayedPersistInstance, 5000);
        }
    }

    public Set<Class<? extends ITask>> supportedTaskClasses() {
        Set<Class<? extends ITask>> result = new HashSet<>();
        for (Set<Class<? extends ITask>> value : inSpecTaskMap.values()) {
            result.addAll(value);
        }
        return result;
    }

    public void deleteThing(UUID thingUuid) throws Exception {
        if (ALWAYS_REQUIRED_THINGS_UUID_SET.contains(thingUuid)) {
            throw new LocalizedException("thing.error.systemThingRequired");
        }
        Thing<?> thing = primaryInstance.getThing(thingUuid);
        if (thing == null) {
            throw new Exception("thing.error.removed");
        }
//        if (thing instanceof Singleton) {
//            throw new Exception("thing.error.actionUnavailable");
//        }
        if (!thing.getSubscribers().isEmpty()) {
            throw new LocalizedException("thing.error.linkedTasksExists");
        }
        primaryInstance.getThingsMap().remove(thingUuid);

        standardCache.clear();

        persistPrimaryInstance();

        thing.stop();
        thing.destroy();
    }

    private final Map<String, Map<UUID, String>> standardCache = new ConcurrentHashMap<>();

    /**
     * Method executes via reflections in form utils
     */
    public Map<UUID, String> findByStandard(String standard) {
        return standardCache.computeIfAbsent(standard, (st) -> {
            try {
                return findByStandard(Class.forName(standard));
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public Map<UUID, String> findByStandard(Class<?>... standards) {
        Map<UUID, String> res = new HashMap<>();
        for (Thing<?> thing : primaryInstance.getThings()) {
            if (!UserThreadContext.hasReadPermission(thing.getUuid())) {
                continue;
            }
            for (Class<?> standardInterface : standards) {
                if (standardInterface.isAssignableFrom(thing.getClass())) {
                    res.put(thing.getUuid(), thing.getTitle());
                    break;
                }
            }
        }
        return res;
    }

    public List<Thing<?>> findThingsByStandard(Class<?>[] interfaces) {
        List<Thing<?>> res = new ArrayList<>();
        for (Thing<?> thing : primaryInstance.getThings()) {
            for (Class<?> standardInterface : interfaces) {
                if (standardInterface.isAssignableFrom(thing.getClass())) {
                    res.add(thing);
                    break;
                }
            }
        }
        return res;
    }

    public Thing<?> saveOrUpdateThing(UUID thingUuid, String thingClass, Map<String, Object> configuration) throws Exception {
        boolean isNew = thingUuid == null;
        boolean restartNeed;
        final Thing<?> thing;
        Object clonedConfig = null;
        if (isNew) {
            thing = buildThing(thingClass, configuration);
            standardCache.clear();
            restartNeed = true;
        } else {
            if (!UserThreadContext.hasUpdatePermission(thingUuid)) {
                throw new Exception("updateDenied");
            }
            thing = primaryInstance.getThing(thingUuid);

            IConfiguration config = thing.getConfiguration();
            clonedConfig = BeanUtils.cloneBean(config);
            PropertyUtils.copyProperties(clonedConfig, config);


            restartNeed = updateNodeConfig(thing.getConfiguration(), configuration);
            if (restartNeed) {
                thing.stop();
            } else {//reload config only
                thing.reloadConfig();
            }
        }
        try {
            thing.getConfiguration().validate();
        } catch (Throwable e) {
            if (clonedConfig != null) {
                PropertyUtils.copyProperties(thing.getConfiguration(), clonedConfig);
            }
            throw e;
        }

        validateUniquness(thing);

        if (isNew) {
            UUID uuid = thing.getUuid();
            primaryInstance.getThingsMap().put(uuid, thing);
            thing.init();
        }
        thing.onSave();
        if (restartNeed) {
            Thread.sleep(1000);// waiting for completing grabber processes.
            STARTUP_EXECUTOR.submit(() -> {
                thing.start(false, true);

                if (isNew && thing instanceof AutoAddTasks aat) {
                    Collection<AbstractTask<?>> autoAddTasks = aat.autoAddTasks();
                    for (AbstractTask<?> autoTask : autoAddTasks) {
                        validateUniquness(autoTask.parent(), autoTask);
                    }
                    for (AbstractTask<?> autoTask : autoAddTasks) {
                        applicationAllTasksMap.put(autoTask.getUuid(), autoTask);
                        autoTask.start(true, true);
                        autoTask.parent().addSubTask(autoTask);
                    }
                }

                try {
                    persistPrimaryInstance();
                } catch (Throwable e) {
                    log.error(e.getMessage(), e);
                }
            });
        } else {
            persistPrimaryInstance();
        }

        return thing;
    }

    private static boolean updateNodeConfig(Object nodeConfig, Map<String, Object> configUpdate) {
        boolean restartNeed = false;
        BeanWrapper taskW = new BeanWrapperImpl(nodeConfig);
        for (Map.Entry<String, Object> property : configUpdate.entrySet()) {
            String propName = property.getKey();
            Object oldValue = taskW.getPropertyValue(propName);
            Object value = property.getValue();
            taskW.setPropertyValue(propName, value);
            Object newValue = taskW.getPropertyValue(propName);

            if (oldValue != null && !oldValue.equals(newValue)
                    || newValue != null && !newValue.equals(oldValue)) {
                Field field = ReflectionUtils.findField(nodeConfig.getClass(), propName);
                UIComponent fconf = field.getAnnotation(UIComponent.class);
                restartNeed |= fconf.restartOnChange();
            }
        }
        return restartNeed;
    }

    private void validateUniquness(Thing<?> thing) {
        if (thing.uniqueness() == null) {
            return;
        }
        for (Thing<?> th : primaryInstance.getThings()) {
            if (th.getClass() != thing.getClass()) {//skip not my types
                continue;
            }
            if (th.getUuid().equals(thing.getUuid())) {// skip if me
                continue;
            }

            if (th.uniqueness().equals(thing.uniqueness())) {
                throw new LocalizedException("thing.error.unique", th.uniqueness());
            }
        }
    }

    private void validateUniquness(AbstractListOfTask<?> parentContext, ITask<?> newTask) {
        if (newTask.uniqueness() == null) {
            return;
        }
        for (ITask<?> existedTask : parentContext.getSubTasks()) {
            if (existedTask.getClass() != newTask.getClass()) {//skip not my types
                continue;
            }
            if (existedTask.getUuid().equals(newTask.getUuid())) {// skip if me
                continue;
            }

            if (existedTask.uniqueness().equals(newTask.uniqueness())) {
                throw new LocalizedException("task.error.unique", existedTask.uniqueness());
            }
        }
    }

    private Thing<?> buildThing(String clazz, Map<String, Object> configuration) throws Exception {
        Class<? extends Thing<?>> thingClass = (Class<? extends Thing<?>>) Class.forName(clazz);
        Thing<?> thing = Thing.blankOf(thingClass, this.engine);
        Object thingConfig = thing.getConfiguration();
        BeanWrapper thingW = new BeanWrapperImpl(thingConfig);
        for (Map.Entry<String, Object> property : configuration.entrySet()) {
            Object value = property.getValue();
            if (value instanceof String strVal) {
                if (strVal.contains("$banalytics_home$")) {
                    String homePath = engine.applicationConfigFolder().getAbsolutePath();
                    value = strVal.replaceAll("\\$banalytics_home\\$", homePath.replaceAll("\\\\", "/"));
                }
            }
            thingW.setPropertyValue(property.getKey(), value);
        }
        return thing;
    }

    public <T extends Thing<?>> T getThing(UUID uuid) {
        return primaryInstance.getThing(uuid);
    }

    public Collection<? extends Thing<?>> getThings() {
        return primaryInstance.getThings();
    }

    /**
     * Returns all available thing classes excluding created singletons
     */
    public Collection<Class<?>> supportedThings() {
        Set<Class<?>> availableThings = new HashSet<>();

        skipThingType:
        for (Class<? extends Thing> thingsClass : availableThingsClasses) {
            if (Singleton.class.isAssignableFrom(thingsClass)) {
                for (Thing<?> thing : primaryInstance.getThings()) {
                    if (thingsClass == thing.getClass()) {
                        continue skipThingType;
                    }
                }
            }
            availableThings.add(thingsClass);
        }

        return availableThings;
    }

    public Set<String> listPossibleConfigValues(String propertyName) {
        Set<String> values = new TreeSet<>(String::compareTo);

        for (AbstractTask<?> task : applicationAllTasksMap.values()) {
            if (task instanceof PropertyValuesProvider pvp) {
                Set<String> vals = pvp.provideValues(propertyName);
                if (vals != null && !vals.isEmpty()) {
                    values.addAll(vals);
                }
            }
        }

        return values;
    }

    public void startTask(UUID taskUuid) throws Exception {
        ITask<?> task = findTaskByUuid(taskUuid);
        if (task == null) {
            throw new Exception("task.error.removed.reload");
        }
        IConfiguration config = task.getConfiguration();
        config.setAutostart(true);
        task.start(false, true);

        persistPrimaryInstance();
    }

    public void stopTask(UUID taskUuid) throws Exception {
        ITask<?> task = findTaskByUuid(taskUuid);
        if (task == null) {
            throw new Exception("task.error.removed.reload");
        }
        task.stop();
        IConfiguration config = task.getConfiguration();
        config.setAutostart(false);

        persistPrimaryInstance();
    }

    public void startThing(UUID thingUuid) throws Exception {
        Thing<?> thing = getThing(thingUuid);
        if (thing == null) {
            throw new Exception("thing.error.removed.reload");
        }
        IConfiguration config = thing.getConfiguration();
        config.setAutostart(true);
        thing.start(false, true);

        persistPrimaryInstance();
    }

    public void stopThing(UUID thingUuid) throws Exception {
        Thing<?> thing = getThing(thingUuid);
        if (thing == null) {
            throw new Exception("thing.error.removed.reload");
        }
        if (thing instanceof Singleton) {
            throw new Exception("thing.error.actionUnavailable");
        }
        IConfiguration config = thing.getConfiguration();
        config.setAutostart(false);
        thing.stop();

        persistPrimaryInstance();
    }

    public void startBillableTasks() {
        for (AbstractTask<?> t : applicationAllTasksMap.values()) {
            if (t.billable()) {
                t.start(false, true);
            }
        }
    }

    public void startBillableThings() {
        for (Thing<?> thing : getPrimaryInstance().getThings()) {
            if (thing.billable()) {
                thing.start(false, true);
            }
        }
    }

    public void stopBillableTasks() {
        for (AbstractTask<?> t : applicationAllTasksMap.values()) {
            if (t.billable()) {
                t.stop();
            }
        }
    }

    public void stopBillableThings() {
        for (Thing<?> thing : getPrimaryInstance().getThings()) {
            if (thing.billable()) {
                thing.stop();
            }
        }
    }

    public Collection<String> subModelsList(String modelName) {
        File modelsRoot = new File(banalyticsRoot, "models");
        if (!modelsRoot.exists()) {
            return Collections.emptyList();
        }
        File subModelsRoot = new File(modelsRoot, modelName);
        if (!subModelsRoot.exists()) {
            return Collections.emptyList();
        }

        ArrayList<String> subModelsNames = new ArrayList<>();

        for (File subModelFolder : subModelsRoot.listFiles()) {
            if (subModelFolder.isDirectory()) {
                subModelsNames.add(subModelFolder.getName());
            }
        }
        subModelsNames.sort(String::compareTo);
        return subModelsNames;
    }


    public File getModelFolder(String modelName, String subModelName) throws Exception {
        File modelsRoot = new File(banalyticsRoot, "models");
        if (!modelsRoot.exists()) {
            throw new Exception("Models folder not found.");
        }
        File subModelsRoot = new File(modelsRoot, modelName);
        if (!subModelsRoot.exists()) {
            throw new Exception("Model folder not found: " + modelName);
        }

        File subModelsFolder = new File(subModelsRoot, subModelName);
        if (!subModelsFolder.exists()) {
            throw new Exception("Model folder not found: " + subModelName);
        }

        return subModelsFolder;
    }
}
