package com.banalytics.box.module;

import com.banalytics.box.api.integration.webrtc.channel.NodeState;
import com.banalytics.box.api.integration.webrtc.channel.events.AbstractEvent;
import com.banalytics.box.api.integration.webrtc.channel.events.StatusEvent;
import com.banalytics.box.module.constants.RestartOnFailure;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.Set;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static com.banalytics.box.module.State.*;
import static com.banalytics.box.module.utils.Utils.nodeType;
import static com.banalytics.box.service.SystemThreadsService.SYSTEM_TIMER;

@Slf4j
public abstract class AbstractThing<CONFIGURATION extends IConfiguration> implements Thing<CONFIGURATION>, EventProducer {
    protected State state = STOPPED;
    protected String stateDescription;

    public State getState() {
        return state;
    }

    @Override
    public String getStateDescription() {
        return stateDescription;
    }

    public final Class<CONFIGURATION> configClass = getType(0);

    protected CONFIGURATION configuration;

    protected final BoxEngine engine;

    @Override
    public UUID getUuid() {
        return configuration.getUuid();
    }

    {
        try {
            configuration = configClass.getDeclaredConstructor().newInstance();
            if (!Singleton.class.isAssignableFrom(configuration.getClass())) {
                UUID uuid = UUID.randomUUID();
                if (!(this instanceof Singleton)) {
                    configuration.setUuid(uuid);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public AbstractThing(BoxEngine engine) {
        this.engine = engine;
    }

    @Override
    public String getSelfClassName() {
        return getClass().getName();
    }

    public CONFIGURATION getConfiguration() {
        return configuration;
    }

    @Override
    public void configuration(CONFIGURATION configuration) {
        this.configuration = configuration;
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

    private TimerTask restartThing;

    public void onProcessingException(Throwable e) {
        state = ERROR;
        log.error("Error: " + getUuid() + " : " + getClass(), e);
        this.stateDescription = e.getMessage();
        sendThingState(e.getMessage());

        final RestartOnFailure restartOnFailure = configuration.getRestartOnFailure();
        if (restartOnFailure != RestartOnFailure.STOP_ON_FAILURE) {
            if (restartThing == null) {
                restartThing = new TimerTask() {
                    @Override
                    public void run() {
                        restart();
                    }
                };
                SYSTEM_TIMER.schedule(restartThing, restartOnFailure.restartDelayMillis);
                sendThingState("Scheduled restart via " + restartOnFailure.restartDelayMillis + " milliseconds");
            }
        }
    }

    private final Map<InitShutdownSupport, Object> subscribers = new ConcurrentHashMap<>();

    public Set<InitShutdownSupport> getSubscribers() {
        return subscribers.keySet();
    }

    public void subscribe(InitShutdownSupport initShutdownSupport) {
        subscribers.put(initShutdownSupport, new Object());
        log.info("Added subscriber '{}': total {}", initShutdownSupport.getTitle(), this.subscribers.size());
    }

    public void unSubscribe(InitShutdownSupport initShutdownSupport) {
        this.subscribers.remove(initShutdownSupport);
        log.info("Removed subscriber '{}': total {}", initShutdownSupport.getTitle(), this.subscribers.size());
    }


    public final void init() {
        this.stateDescription = null;
        try {
            doInit();
        } catch (Exception e) {
            onProcessingException(e);
        }
    }

    protected abstract void doInit() throws Exception;

    public final void start(boolean ignoreAutostartProperty, boolean startChildren) {
        if (state == RUN) {
            return;
        }
        if (!ignoreAutostartProperty && !configuration.isAutostart()) {
            sendThingState();
            return;
        }

        log.info("Start: {} : {}", getClass(), getUuid());
        state = STARTING;
        sendThingState();
        try {
            doStart();
            state = RUN;
            sendThingState();

            if (startChildren) {
                for (InitShutdownSupport subscriber : subscribers.keySet()) {
                    subscriber.start(ignoreAutostartProperty, true);
                }
            }
            log.info("Things initialized: {} : {}", getClass(), getUuid());
        } catch (Exception e) {
            onProcessingException(e);
        }
    }

    protected abstract void doStart() throws Exception;

    public final void stop() {
        log.info("Stop: {}", state);
        if (state != RUN) {
            sendThingState();
            return;
        }
        state = STOPPING;
        sendThingState();
        try {
            log.info("Shutdown subscribers: {}", subscribers);
            for (InitShutdownSupport subscriber : subscribers.keySet()) {
                subscriber.stop();
            }
            doStop();
            state = STOPPED;
            sendThingState();
        } catch (Exception e) {
            onProcessingException(e);
        }
    }

    public final void restart() {
        log.info("Restart");
        stop();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            onProcessingException(e);
        }
        start(true, true);
    }

    protected abstract void doStop() throws Exception;


    private void sendThingState() {
        sendThingState("");
    }

    private void sendThingState(String message) {
        engine.fireEvent(new StatusEvent(
                nodeType(this.getClass()),
                this.getUuid(),
                getSelfClassName(),
                getTitle(),
                NodeState.valueOf(getState().name()),
                message
        ));
    }

    @Override
    public Set<Class<? extends AbstractEvent>> produceEvents() {
        return Set.of(StatusEvent.class);
    }
}
