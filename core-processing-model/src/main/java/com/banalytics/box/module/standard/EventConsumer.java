package com.banalytics.box.module.standard;

import com.banalytics.box.api.integration.webrtc.channel.events.AbstractEvent;

import java.util.Set;
import java.util.UUID;

public interface EventConsumer {
    void consume(Recipient target, AbstractEvent event);

    UUID getUuid();

    String getTitle();

    Set<String> accountNames(Set<String> accountIds);

    public static record Recipient(Set<String> accounts) {
        public boolean isAllowed(String account) {
            return accounts == null || accounts.isEmpty() || accounts.contains(account);
        }

        public boolean isEmpty() {
            return accounts == null || accounts.isEmpty();
        }
    }
}
