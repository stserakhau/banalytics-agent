package com.banalytics.box.module.events.model;

import com.banalytics.box.api.integration.webrtc.channel.events.AbstractEvent;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class Rule {
    private UUID uuid;

    private boolean enabled;

    private String title;

    private Trigger trigger = new Trigger();

    private List<Action> actions = new ArrayList<>();

    @JsonIgnore
    public boolean isScheduledRule() {
        List<String> crons = trigger.getCronExpressions();
        if (crons.isEmpty()) {
            return false;
        }

        return
                trigger.getEventSourceNodeUUIDs().isEmpty()
                        && trigger.getEventSourcesClassNames().isEmpty()
                        && trigger.getEventTypesConfigs().isEmpty()
                        && crons.get(0).startsWith("0 ");
    }

    public boolean triggered(AbstractEvent event) {
        if (!enabled) {
            return false;
        }
        return trigger.triggered(event);
    }
}
