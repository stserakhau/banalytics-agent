package com.banalytics.box.module.agent;

import com.banalytics.box.api.integration.webrtc.channel.events.AbstractEvent;
import com.banalytics.box.module.AbstractThing;
import com.banalytics.box.module.BoxEngine;
import com.banalytics.box.module.State;
import com.banalytics.box.module.standard.EventConsumer;
import com.banalytics.box.module.webrtc.PortalWebRTCIntegrationConfiguration;
import com.banalytics.box.module.webrtc.PortalWebRTCIntegrationThing;
import com.banalytics.box.module.webrtc.client.RTCClient;
import com.banalytics.box.service.SystemThreadsService;
import lombok.extern.slf4j.Slf4j;

import java.util.Set;
import java.util.TimerTask;

@Slf4j
public class AgentConnectorThing extends AbstractThing<AgentConnectorConfiguration> implements EventConsumer {

    public AgentConnectorThing(BoxEngine engine) {
        super(engine);
    }

    private PortalWebRTCIntegrationThing portalWebRTCIntegrationThing;

    private RTCClient rtcClient;

    @Override
    protected void doInit() throws Exception {
        this.portalWebRTCIntegrationThing = engine.getThingAndSubscribe(PortalWebRTCIntegrationConfiguration.WEB_RTC_UUID, this);
    }

    @Override
    protected void doStart() throws Exception {
        this.portalWebRTCIntegrationThing.sendReady(this.configuration.agentUuid);

        //todo implement auth protocol with password & token
        //todo on offer should n't request auth
        //todo main case - message delivery (command execution not need)

        waitConnection();
    }

    private void waitConnection() {
        log.info("Connecting to agent > {}", configuration.agentUuid);
        SystemThreadsService.SYSTEM_TIMER.schedule(new TimerTask() {
            final int repeatTimes = 10;

            int counter = 0;

            @Override
            public void run() {
                log.info("Looking for connection: {}", configuration.agentUuid);
                rtcClient = portalWebRTCIntegrationThing.findAgent(configuration.agentUuid);
                if (rtcClient != null) {
                    log.info("Connected to agent: {}", rtcClient.environmentUUID);
                    cancel();
                    return;
                }
                counter++;

                if (counter >= repeatTimes) {
                    log.info("Connection cancelled. Agent not respond long time: {}", configuration.agentUuid);
                    cancel();
                    stop();
                }
            }
        }, 1000, 3000);
    }

    @Override
    protected void doStop() throws Exception {
        this.portalWebRTCIntegrationThing.sendBye(this.configuration.agentUuid);
    }

    @Override
    public void destroy() {
        if (this.portalWebRTCIntegrationThing != null) {
            this.portalWebRTCIntegrationThing.unSubscribe(this);
        }
        this.portalWebRTCIntegrationThing = null;
    }

    @Override
    public String getTitle() {
        return this.configuration.title;
    }

    @Override
    public void consume(Recipient target, AbstractEvent event) {
        if (state == State.RUN) {
            try {
                rtcClient.sendEvent(event);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
    }

    @Override
    public Set<String> accountNames(Set<String> accountIds) {
        return Set.of();
    }
}
