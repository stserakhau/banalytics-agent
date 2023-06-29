package com.banalytics.box.module.telegram.handlers;

import java.util.UUID;

public class Utils {
    public static String subscriptionCode(UUID thingUuid, long chatId) {
        return "subscription-" + thingUuid + chatId;
    }
}
