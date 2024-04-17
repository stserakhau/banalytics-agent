package com.banalytics.box.module;

import com.banalytics.box.LocalizedException;
import com.banalytics.box.api.integration.webrtc.channel.NodeState;
import com.banalytics.box.api.integration.webrtc.channel.events.AbstractEvent;
import com.banalytics.box.api.integration.webrtc.channel.events.StatusEvent;
import com.banalytics.box.module.constants.RestartOnFailure;
import com.banalytics.box.service.SystemThreadsService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.PropertyUtils;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.Set;
import java.util.TimerTask;
import java.util.UUID;

import static com.banalytics.box.module.State.*;
import static com.banalytics.box.module.utils.Utils.nodeType;
import static com.banalytics.box.service.SystemThreadsService.SYSTEM_TIMER;

@Slf4j
public abstract class AbstractTask<CONFIGURATION extends IConfiguration> implements ITask<CONFIGURATION>, EventProducer {
    public final Class<CONFIGURATION> configClass = getType(0);

    public final CONFIGURATION configuration;

    protected AbstractListOfTask<?> parent;
    public State state = State.STOPPED;//null means task removed
    public String stateDescription;

    protected BoxEngine engine;
    protected int taskInitializationDelay = 500;

    {
        try {
            configuration = configClass.getDeclaredConstructor().newInstance();
            UUID uuid = UUID.randomUUID();

            if (!(this instanceof Singleton)) {
                configuration.setUuid(uuid);
            }
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    private void sendTaskState() {
        sendTaskState("");
    }

    public void sendTaskState(String message) {
        engine.fireEvent(new StatusEvent(
                nodeType(this.getClass()),
                this.getUuid(),
                getSelfClassName(),
                getTitle(),
                NodeState.valueOf(getState().name()),
                message
        ));
    }

    public void onWarnException(Throwable e) {
        log.error("Operation failed: " + e.getMessage(), e);
        sendTaskState(e.getMessage());
    }

    public void onInitException(Throwable e) {
        this.state = State.INIT_ERROR;
        log.error("Operation failed {} ({}): {}", getSelfClassName(), getTitle(), e.getMessage());
        log.error("Exception details", e);
        this.stateDescription = e.getMessage();
        sendTaskState(e.getMessage());
    }

    public void onException(Throwable e) {
        this.state = State.ERROR;
        log.error("Operation failed {} ({}): {}", getSelfClassName(), getTitle(), e.getMessage());
        log.error("Exception details", e);
        this.stateDescription = e.getMessage();
        sendTaskState(e.getMessage());
    }

    private TimerTask restartTask;

    public void onProcessingException(Throwable e) {
        this.state = State.ERROR;
        this.stateDescription = e.getMessage();
        log.error("Task {} ({}) failed: {}", getSelfClassName(), getTitle(), e.getMessage());
        log.error("Exception details", e);
        sendTaskState(e.getMessage());

        fireRestart();
    }

    protected void fireRestart() {
        final RestartOnFailure restartOnFailure = configuration.getRestartOnFailure();
        if (restartOnFailure != RestartOnFailure.STOP_ON_FAILURE) {
            if (restartOnFailure.restartDelayMillis > 0) {
                if (restartTask == null) {
                    restartTask = new TimerTask() {
                        @Override
                        public void run() {
                            if (state == null) {
                                cancel();
                                restartTask = null;
                            } else {
                                restart();
                            }
                        }
                    };
                    SYSTEM_TIMER.schedule(restartTask, restartOnFailure.restartDelayMillis);
                    sendTaskState("Scheduled restart via " + restartOnFailure.restartDelayMillis + " milliseconds");
                } else {
                    log.warn("Restart already scheduled. {} ({})", getSelfClassName(), getTitle());
                }
            } else {
                SystemThreadsService.execute(this, this::restart);
            }
        } else {
            try {
                doStop();
            } catch (Throwable ex) {
                log.error(getSelfClassName() + "(" + getTitle() + "): " + ex.getMessage(), ex);
            }
        }
    }

    /**
     * Method executed on
     * <li>loading tasks from config</li>
     * <li>On creation new task</li>
     * <li>Before starting the task</li>
     */
    public void init() {
        this.stateDescription = null;
        try {
            doInit();
        } catch (Throwable e) {
            onInitException(e);
        }
    }

    protected boolean canStart() {
        Thing<?> sourceThing = getSourceThing();
        return sourceThing != null && sourceThing.getState() == RUN;
    }

    public synchronized void start(boolean ignoreAutostartProperty, boolean startChildren) {
        log.info("Start {} / {}", getUuid(), getTitle());
        if (state == INIT_ERROR || state == RUN || state == STARTING || state == null) {
            return;
        }
        init();

        if (!canStart()) {
            return;
        }

        if (!ignoreAutostartProperty && !configuration.isAutostart()) {
            sendTaskState();
            return;
        }

        state = State.STARTING;
        sendTaskState();
        try {
            doStart(ignoreAutostartProperty, startChildren);

            state = State.RUN;
            if (taskInitializationDelay > 0) {
                Thread.sleep(taskInitializationDelay);//delay for system initialization before stop stand available
            }
            sendTaskState();
        } catch (Throwable e) {
            log.error(getSelfClassName() + "(" + getTitle() + "): " + " initialization error.", e);
            onProcessingException(e);
        }
    }

    public synchronized void stop() {
        if (restartTask != null) {//clear restart task
            restartTask.cancel();
            restartTask = null;
        }
        log.info("Stop {} ({})", state, getUuid());
        if (state == STOPPED || state == STOPPING) {
            return;
        }
        this.state = STOPPING;
        sendTaskState();

        try {
            doStop();
            Thread.sleep(50);//delay to free system resources process before immediate start
            state = State.STOPPED;
            sendTaskState();
        } catch (Throwable e) {
            onException(e);
            log.error("Shutdown error:" + getSelfClassName() + "(" + getTitle() + ")", e);
        }
    }

    public final void restart() {
        log.info("Restart {} ({})", state, getUuid());
        //STOPPED, STARTING, RUN, STOPPING, ERROR
        if (state == STARTING || state == STOPPING || state == null) {
            return;
        }
        stop();
        try {// general wait to free resources
            Thread.sleep(500);
        } catch (Throwable e) {
            onException(e);
        }
        start(false, true);
    }

    @Override
    public void copyTo(ITask<CONFIGURATION> copy) {
        try {
            PropertyUtils.copyProperties(copy.getConfiguration(), this.configuration);
            ((AbstractTask<CONFIGURATION>) copy).engine = this.engine;
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public AbstractTask(BoxEngine engine, AbstractListOfTask<?> parent) {
        this.engine = engine;
        this.parent = parent;
    }

    @Override
    public final UUID getUuid() {
        return configuration.getUuid();
    }

    @Override
    public final CONFIGURATION getConfiguration() {
        return configuration;
    }

    @Override
    public final String getSelfClassName() {
        return getClass().getName();
    }

    @Override
    public final State getState() {
        return this.state;
    }

    @Override
    public final String getStateDescription() {
        return stateDescription;
    }

    @Override
    public final AbstractListOfTask<?> parent() {
        return parent;
    }

    @Override
    public final void parent(AbstractListOfTask<?> parent) {
        this.parent = parent;
    }

    @Override
    public final AbstractListOfTask<?> root() {
        if (parent == null) {
            return (AbstractListOfTask<?>) this;
        }
        return parent.root();
    }

//    private Map<String, Class<?>> runtimeOutSpec;

//    @Override
//    public final Map<String, Class<?>> runtimeOutSpec() {
//        return runtimeOutSpec;
//    }


    /**
     * Method executes task's work when task in RUN state.
     *
     * @return true if need continue to execute tasks in tree hierarchy
     */
    @Override
    public final boolean process(ExecutionContext executionContext) throws Exception {
        if (state == null) {
            return false;
        }
        switch (state) {
            case RUN:
                try {
                    boolean continueProcessing = doProcess(executionContext);
//                    if (continueProcessing && runtimeOutSpec == null) {
//                        runtimeOutSpec = executionContext.variablesTypes();
//                        Map<String, Class<?>> logicVariables = logicVariables();
//                        if (logicVariables != null) {
//                            runtimeOutSpec.putAll(logicVariables);
//                        }
//                    }
                    return continueProcessing;
                } catch (Throwable e) {
                    onException(e);
                }
            case STARTING:
//                runtimeOutSpec = null;//reset runtime spec on restart
            case STOPPED:
            case INIT_ERROR:
            case ERROR:
            default:
                return false; // block the execution tree
        }
    }

    /**
     * Method for implementation task's work.
     *
     * @return true if continue to execute tasks in tree hierarchy
     */
    protected boolean doProcess(ExecutionContext executionContext) throws Exception {
        return false;
    }

    private <T> Class<T> getType(int index) {
        Type type = getClass().getGenericSuperclass();
        if (type instanceof Class) {
            while (!(type instanceof ParameterizedType)) {
                type = ((Class) type).getGenericSuperclass();
            }
        }


        type = ((ParameterizedType) type).getActualTypeArguments()[index];
        if (type instanceof Class) {
            return (Class<T>) type;
        } else {
            return null;
        }
    }

    public Set<UUID> selfDelete() {
        stop();
        destroy();
        if (restartTask != null) {
            restartTask.cancel();
            restartTask = null;
        }
        state = null;
        if (parent != null) {
            parent.removeSubTask(this);
        }
        parent = null;
        return Set.of(getUuid());
    }

    /**
     * titles
     */

    @Override
    public UUID getSourceThingUuid() {
        return parent != null ? parent.getSourceThingUuid() : null;
    }

    @Override
    public Thing<?> getSourceThing() {
        UUID sourceThingUuid = getSourceThingUuid();
        if (sourceThingUuid != null) {
            return engine.getThing(sourceThingUuid);
        } else {
            return null;
        }
    }

    @Override
    public String getTitle() {
        Thing<?> source = getSourceThing();
        if (source != null) {
            return source.getTitle();
        } else {
            return "";
        }
    }

    @Override
    public Set<Class<? extends AbstractEvent>> produceEvents() {
        return Set.of(StatusEvent.class);
    }

    public Set<Class<? extends AbstractTask<?>>> shouldAddAfter() {
        return Set.of();
    }

    public Set<Class<? extends AbstractTask<?>>> shouldAddBefore() {
        return Set.of();
    }
}
