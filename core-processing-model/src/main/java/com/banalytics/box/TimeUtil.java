package com.banalytics.box;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

public class TimeUtil {
    public static ZoneId agentTimeZone() {
        return ZoneId.of(System.getProperty("user.timezone"));
    }

    public static LocalDateTime currentTimeInServerTz() {
        return LocalDateTime.now(ZoneId.of(System.getProperty("user.timezone")));
    }

    public static LocalDateTime fromMillisToServerTz(long millis) {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(millis),
                ZoneId.of(System.getProperty("user.timezone")));
    }

    public static LocalDateTime fromSecondsToServerTz(long seconds) {
        return LocalDateTime.ofInstant(Instant.ofEpochSecond(seconds),
                ZoneId.of(System.getProperty("user.timezone")));
    }


}
