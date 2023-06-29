package com.banalytics.box.module;

import com.banalytics.box.api.integration.webrtc.channel.events.AbstractEvent;

import java.util.Set;

public interface EventProducer {
    Set<Class<? extends AbstractEvent>> produceEvents();
}
