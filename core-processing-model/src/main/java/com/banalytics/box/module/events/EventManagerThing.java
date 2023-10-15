package com.banalytics.box.module.events;

import com.banalytics.box.TimeUtil;
import com.banalytics.box.api.integration.webrtc.channel.events.AbstractEvent;
import com.banalytics.box.model.task.EnvironmentNode;
import com.banalytics.box.module.*;
import com.banalytics.box.module.events.model.Action;
import com.banalytics.box.module.events.model.Rule;
import com.banalytics.box.module.events.model.Trigger;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronTrigger;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.banalytics.box.api.integration.webrtc.channel.environment.ThingApiCallReq.PARAM_METHOD;
import static com.banalytics.box.module.Thing.StarUpOrder.DATA_EXCHANGE;

@Slf4j
@Order(DATA_EXCHANGE + 100)
public class EventManagerThing extends AbstractThing<EventManagerThingConfig> implements Consumer<AbstractEvent> {
    private final TypeReference<List<Rule>> TYPE_RULES_LIST = new TypeReference<List<Rule>>() {
    };
    private final ObjectMapper om = new ObjectMapper();

    private File eventManagerRulesFile;

    private List<Rule> rules = new ArrayList<>();

    public EventManagerThing(BoxEngine engine) {
        super(engine);
    }

    @Override
    public String getTitle() {
        return configuration.getTitle();
    }

    private void persistRules() throws IOException {
        om.writeValue(eventManagerRulesFile, this.rules);
        for (Rule rule : this.rules) {
            rule.getTrigger().reset();
        }
    }

    private void loadRules() throws IOException {
        rules = om.readValue(eventManagerRulesFile, TYPE_RULES_LIST);
    }

    private ExecutorService ruleExecutorService;

    private TaskScheduler taskScheduler;
    private final Map<UUID, List<ScheduledFuture<?>>> ruleScheduledTasksMap = new ConcurrentHashMap<>();

    @Override
    protected void doInit() throws Exception {
        taskScheduler = engine.getBean(TaskScheduler.class);
    }

    @Override
    public void destroy() {
        if (eventManagerRulesFile.exists()) {
            eventManagerRulesFile.delete();
        }
    }

    @Override
    protected void doStart() throws Exception {
        ruleExecutorService = Executors.newSingleThreadExecutor();
        File applicationConfigFolder = engine.applicationConfigFolder();
        File instanceFolder = new File(applicationConfigFolder, "instances");
        this.eventManagerRulesFile = new File(instanceFolder, getUuid().toString() + ".rules");
        if (!eventManagerRulesFile.exists()) {
            if (eventManagerRulesFile.createNewFile()) {
                persistRules();//create default content
            } else {
                log.error("Can't create {} file", eventManagerRulesFile.getAbsolutePath());
            }
        } else {
            loadRules();
        }
        engine.addEventConsumer(this);

        for (Rule rule : rules) {
            rescheduleRule(rule, false);
        }
    }

    private void rescheduleRule(Rule rule, boolean drop) {
        if (!rule.isScheduledRule()) {
            return;
        }
        log.info("Scheduling rule {} / {}: reschedule", rule.getUuid(), rule.getTitle());
        List<ScheduledFuture<?>> scheduledFutures = ruleScheduledTasksMap.computeIfAbsent(rule.getUuid(), r -> new ArrayList<>());
        //if exists scheduled tasks cancel all
        if (!scheduledFutures.isEmpty()) {
            for (ScheduledFuture<?> scheduledFuture : scheduledFutures) {
                scheduledFuture.cancel(true);
            }
            log.info("Scheduling rule {} / {}: clear features {}", rule.getUuid(), rule.getTitle(), scheduledFutures.size());
            scheduledFutures.clear();
        }
        if (!rule.isEnabled() || drop) {
            log.info("Scheduling rule {} / {}: remove", rule.getUuid(), rule.getTitle());
            ruleScheduledTasksMap.remove(rule.getUuid());
            return;
        }
        // and re-schedule
        Runnable r = () -> {
            executeActionsBatch(rule.getActions(), null);
        };
        for (String expression : rule.getTrigger().getCronExpressions()) {
            String[] parts = expression.split(" ");
            if (parts.length > 6) {
                expression = String.join(" ", parts[0], parts[1], parts[2], parts[3], parts[4], parts[5]);
            }
            ScheduledFuture<?> scheduledTask = taskScheduler.schedule(
                    r,
                    new CronTrigger(expression, TimeUtil.agentTimeZone())
            );
            scheduledFutures.add(scheduledTask);
            log.info("Scheduled execution via: {} seconds", scheduledTask.getDelay(TimeUnit.SECONDS));
        }
        log.info("Scheduling rule {} / {}:\nscheduled features> {}\nscheduled actions> {}", rule.getUuid(), rule.getTitle(), scheduledFutures, rule.getActions());
    }

    @Override
    protected void doStop() throws Exception {
        for (Rule rule : rules) {
            rescheduleRule(rule, true);
        }
        ruleExecutorService.shutdown();
        engine.removeEventConsumer(this);
    }

    private static final String PARAM_RULE_TITLE = "title";
    private static final String PARAM_RULE_UUID = "uuid";
    private static final String PARAM_RULE_NODE_UUID = "nodeUuid";
    private static final String PARAM_RULE_ACTION_UUID = "actionUuid";
    private static final String PARAM_RULE_CLAZZ = "clazz";
    private static final String PARAM_RULE_CLAZZ_CONFIGURATION = "configuration";
    private static final String PARAM_EVENT_TYPE = "eventType";
    private static final String PARAM_RULE_CRON_EXPRS = "cronExpressions";

    @Override
    public Set<String> apiMethodsPermissions() {
        return Set.of(PERMISSION_READ, PERMISSION_UPDATE);
    }

    @Override
    public Object call(Map<String, Object> params) throws Exception {
        if (state != State.RUN) {
            throw new Exception("Thing uninitialized");
        }
        String method = (String) params.get(PARAM_METHOD);
        switch (method) {
            case "readRules" -> {
                return rules;
            }
            case "updateCreateRule" -> {
                String title = (String) params.get(PARAM_RULE_TITLE);
                Rule r = new Rule();
                r.setEnabled(false);
                r.setUuid(UUID.randomUUID());
                r.setTitle(title);
                this.rules.add(r);
                persistRules();
                return "success";
            }
            case "updateRuleTitle" -> {
                String ruleId = (String) params.get(PARAM_RULE_UUID);
                String title = (String) params.get(PARAM_RULE_TITLE);
                UUID uuid = UUID.fromString(ruleId);
                for (Rule r : this.rules) {
                    if (r.getUuid().equals(uuid)) {
                        r.setTitle(title);
                        persistRules();
                        break;
                    }
                }
                return "success";
            }
            case "updateDeleteRule" -> {
                String ruleId = (String) params.get(PARAM_RULE_UUID);
                UUID uuid = UUID.fromString(ruleId);
                for (Rule r : this.rules) {
                    if (r.getUuid().equals(uuid)) {
                        this.rules.remove(r);
                        rescheduleRule(r, true);
                        persistRules();
                        break;
                    }
                }
                return "success";
            }
            case "updateEnableRule" -> {
                String ruleId = (String) params.get(PARAM_RULE_UUID);
                UUID uuid = UUID.fromString(ruleId);
                for (Rule r : this.rules) {
                    if (r.getUuid().equals(uuid)) {
                        r.setEnabled(true);
                        rescheduleRule(r, false);
                        persistRules();
                        break;
                    }
                }
                return "success";
            }
            case "updateDisableRule" -> {
                String ruleId = (String) params.get(PARAM_RULE_UUID);
                UUID uuid = UUID.fromString(ruleId);
                for (Rule r : this.rules) {
                    if (r.getUuid().equals(uuid)) {
                        r.setEnabled(false);
                        rescheduleRule(r, true);
                        persistRules();
                        break;
                    }
                }
                return "success";
            }

            case "readEventSourceThingsTasks" -> {
                Collection<? extends Thing<?>> things = engine.findThings();
                Map<UUID, Map<String, ?>> thingsUuidTitleMap = new HashMap<>();
                for (Thing<?> thing : things) {
                    thingsUuidTitleMap.put(thing.getUuid(), Map.of(
                            "title", thing.getTitle(),
                            "className", thing.getSelfClassName()
                    ));
                }

                EnvironmentNode tasksTreeUuidTitles = EnvironmentNode.build(engine.getPrimaryInstance());

                return Map.of(
                        "thingsMap", thingsUuidTitleMap,
                        "tasksTree", tasksTreeUuidTitles
                );
            }
            case "updateAddEventSourceNode" -> {
                String ruleId = (String) params.get(PARAM_RULE_UUID);
                UUID uuid = UUID.fromString(ruleId);
                UUID thingUuid = UUID.fromString((String) params.get(PARAM_RULE_NODE_UUID));
                for (Rule r : this.rules) {
                    if (r.getUuid().equals(uuid)) {
                        r.getTrigger().getEventSourceNodeUUIDs().add(thingUuid);
                        rescheduleRule(r, false);
                        persistRules();
                        return r.getTrigger().getEventSourceNodeUUIDs();
                    }
                }
                throw new Exception("rule.error.notFound");
            }
            case "updateDeleteEventSourceNode" -> {
                String ruleId = (String) params.get(PARAM_RULE_UUID);
                UUID uuid = UUID.fromString(ruleId);
                UUID thingUuid = UUID.fromString((String) params.get(PARAM_RULE_NODE_UUID));
                for (Rule r : this.rules) {
                    if (r.getUuid().equals(uuid)) {
                        r.getTrigger().getEventSourceNodeUUIDs().remove(thingUuid);
                        rescheduleRule(r, false);
                        persistRules();
                        return r.getTrigger().getEventSourceNodeUUIDs();
                    }
                }
                throw new Exception("rule.error.notFound");
            }

            case "readEventSourceClasses" -> {
                return Map.of(
                        "thingClasses", engine.supportedThings().stream().map(Class::getName).collect(Collectors.toList()),
                        "taskClasses", engine.supportedTaskClasses().stream()
                                .filter(aClass -> Instance.class != aClass)
                                .map(Class::getName).collect(Collectors.toList())
                );
            }
            case "updateAddEventSourceClass" -> {
                String ruleId = (String) params.get(PARAM_RULE_UUID);
                UUID uuid = UUID.fromString(ruleId);
                String clazz = (String) params.get(PARAM_RULE_CLAZZ);
                for (Rule r : this.rules) {
                    if (r.getUuid().equals(uuid)) {
                        r.getTrigger().getEventSourcesClassNames().add(clazz);
                        rescheduleRule(r, false);
                        persistRules();
                        return r.getTrigger().getEventSourcesClassNames();
                    }
                }
                throw new Exception("rule.error.notFound");
            }
            case "updateDeleteEventSourceClass" -> {
                String ruleId = (String) params.get(PARAM_RULE_UUID);
                UUID uuid = UUID.fromString(ruleId);
                String clazz = (String) params.get(PARAM_RULE_CLAZZ);
                for (Rule r : this.rules) {
                    if (r.getUuid().equals(uuid)) {
                        r.getTrigger().getEventSourcesClassNames().remove(clazz);
                        rescheduleRule(r, false);
                        persistRules();
                        return r.getTrigger().getEventSourcesClassNames();
                    }
                }
                throw new Exception("rule.error.notFound");
            }


            case "readEventTypeClasses" -> {
                String ruleUuid = (String) params.get(PARAM_RULE_UUID);
                UUID uuid = UUID.fromString(ruleUuid);

                Set<Class<? extends AbstractEvent>> eventClasses = new HashSet<>();

                for (Rule r : this.rules) {
                    if (r.getUuid().equals(uuid)) {
                        Trigger t = r.getTrigger();
                        Set<UUID> nodeUuids = t.getEventSourceNodeUUIDs();
                        for (UUID nodeUuid : nodeUuids) {
                            ITask<?> task = engine.findTask(nodeUuid);
                            if (task instanceof EventProducer ep) {
                                eventClasses.addAll(ep.produceEvents());
                            } else {
                                Thing<?> thing = engine.getThing(nodeUuid);
                                if (thing instanceof EventProducer ep) {
                                    eventClasses.addAll(ep.produceEvents());
                                }
                            }
                        }

                        Set<String> classNames = t.getEventSourcesClassNames();
                        for (String className : classNames) {
                            Class<?> cls = Class.forName(className);
                            for (Thing<?> thing : engine.findThings(cls)) {
                                if (thing instanceof EventProducer ep) {
                                    eventClasses.addAll(ep.produceEvents());
                                }
                            }
                            for (Object at : engine.findTasksByInterfaceSupport(cls)) {
                                if (at instanceof EventProducer ep) {
                                    eventClasses.addAll(ep.produceEvents());
                                }
                            }
                        }
                    }
                }

                if (eventClasses.isEmpty()) {
                    eventClasses = engine.eventTypeClasses();
                }

                return eventClasses.stream().map(Class::getName).collect(Collectors.toList());
            }
            case "readDescribeEventType" -> {
                String eventType = (String) params.get(PARAM_EVENT_TYPE);
                return engine.describeClass(eventType);
            }
            case "updateAddEventTypeClass" -> {
                String ruleId = (String) params.get(PARAM_RULE_UUID);
                UUID uuid = UUID.fromString(ruleId);
                String clazz = (String) params.get(PARAM_RULE_CLAZZ);
                Map<String, Object> configuration = (Map<String, Object>) params.get(PARAM_RULE_CLAZZ_CONFIGURATION);
                for (Rule r : this.rules) {
                    if (r.getUuid().equals(uuid)) {
                        List<Trigger.EventTypeConfig> configs = r.getTrigger().getEventTypesConfigs();
                        boolean found = false;
                        for (Trigger.EventTypeConfig config : configs) {
                            if (config.getClassName().equals(clazz)) {
                                config.setConfiguration(configuration);
                                found = true;
                                break;
                            }
                        }
                        if (!found) {
                            configs.add(new Trigger.EventTypeConfig(clazz, configuration));
                        }
                        rescheduleRule(r, false);
                        persistRules();
                        return configs;
                    }
                }
                throw new Exception("rule.error.notFound");
            }
            case "updateDeleteEventTypeClass" -> {
                String ruleId = (String) params.get(PARAM_RULE_UUID);
                UUID uuid = UUID.fromString(ruleId);
                String clazz = (String) params.get(PARAM_RULE_CLAZZ);
                for (Rule r : this.rules) {
                    if (r.getUuid().equals(uuid)) {
                        List<Trigger.EventTypeConfig> configs = r.getTrigger().getEventTypesConfigs();
                        for (int i = 0; i < configs.size(); i++) {
                            Trigger.EventTypeConfig eventTypesCfg = configs.get(i);
                            if (eventTypesCfg.getClassName().equals(clazz)) {
                                configs.remove(i);
                                break;
                            }
                        }
                        rescheduleRule(r, false);
                        persistRules();
                        return configs;
                    }
                }
                throw new Exception("rule.error.notFound");
            }

            case "readActionTasks" -> {
                Collection<AbstractAction<?>> actionTasks = engine.findActionTasks();
                Map<UUID, Map<String, Object>> actions = new HashMap<>();
                for (AbstractAction actionTask : actionTasks) {
                    actions.put(
                            actionTask.getUuid(),
                            Map.of(
                                    "className", actionTask.getClass().getName(),
                                    "title", actionTask.getTitle(),
                                    "config", actionTask.getConfiguration()
                            )
                    );
                }
                return actions;
            }
            case "updateAddActionTask" -> {
                String ruleId = (String) params.get(PARAM_RULE_UUID);
                UUID uuid = UUID.fromString(ruleId);
                String actionId = (String) params.get(PARAM_RULE_ACTION_UUID);
                UUID taskUuid = UUID.fromString(actionId);
                for (Rule r : this.rules) {
                    if (r.getUuid().equals(uuid)) {
                        boolean actionExists = false;
                        for (Action a : r.getActions()) {
                            if (a.getTaskUuid().equals(taskUuid)) {
                                actionExists = true;
                                break;
                            }
                        }
                        if (!actionExists) {
                            Action a = new Action();
                            a.setTaskUuid(taskUuid);
                            r.getActions().add(a);
                            rescheduleRule(r, false);
                            persistRules();
                        }
                        return r.getActions();
                    }
                }
                throw new Exception("rule.error.notFound");
            }
            case "updateDeleteActionTask" -> {
                String ruleId = (String) params.get(PARAM_RULE_UUID);
                UUID uuid = UUID.fromString(ruleId);
                String actionId = (String) params.get(PARAM_RULE_ACTION_UUID);
                UUID taskUuid = UUID.fromString(actionId);
                for (Rule r : this.rules) {
                    if (r.getUuid().equals(uuid)) {
                        List<Action> actions = r.getActions();
                        for (Action a : actions) {
                            if (a.getTaskUuid().equals(taskUuid)) {
                                actions.remove(a);
                                break;
                            }
                        }
                        rescheduleRule(r, false);
                        persistRules();
                        return actions;
                    }
                }
                throw new Exception("rule.error.notFound");
            }
            case "updateCronExpressions" -> {
                String ruleId = (String) params.get(PARAM_RULE_UUID);
                UUID uuid = UUID.fromString(ruleId);
                for (Rule r : this.rules) {
                    if (r.getUuid().equals(uuid)) {
                        List<String> crons = (List<String>) params.get(PARAM_RULE_CRON_EXPRS);
                        List<String> exprs = r.getTrigger().getCronExpressions();
                        exprs.clear();
                        exprs.addAll(crons);
                        rescheduleRule(r, false);
                        persistRules();
                        break;
                    }
                }
                return "success";
            }
            default -> {
                throw new Exception("Method not supported: " + method);
            }
        }
    }

    @Override
    public void accept(AbstractEvent event) {
        if (state != State.RUN) {
            return;
        }
        for (Rule rule : rules) {
            if (rule.isScheduledRule()) {
                continue;
            }
            if (rule.triggered(event)) {
                executeActionsBatch(rule.getActions(), event);
            }
        }
    }

    private void executeActionsBatch(List<Action> actions, AbstractEvent event) {
        for (Action action : actions) {
            ITask<?> actionTask = engine.findTask(action.getTaskUuid());
            if (actionTask instanceof IAction a) {
                UUID evtUuid = event.getNodeUuid();
                if (evtUuid == null) {
                    log.warn("Event with null node uuid: {}", event);
                }
                if (evtUuid != null && evtUuid.equals(a.getUuid())) {//check cycles execution - if event node and target action is the same nodes - skip action execution
                    continue;
                }
                ruleExecutorService.submit(() -> {
                    try {
                        ExecutionContext ctx = new ExecutionContext();
                        if (event != null) {
                            ctx.setVar(AbstractEvent.class, event);
                        }
                        a.action(ctx);
                    } catch (Throwable e) {
                        //todo error event ???
                        log.error(e.getMessage(), e);
                    }
                });
            }
        }
    }
}
