package com.banalytics.box.api.integration.suc;

import lombok.*;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class Module {
    private static final Pattern ABC_REGEX = Pattern.compile("([0-9]).([0-9]).([0-9])");

    String groupId;

    String artifactId;

    String version;

    ModuleType type;

    LocalDateTime uploadTime;

    public String a() {
        Matcher m = ABC_REGEX.matcher(version);
        if (m.find()) {
            return m.group(1);
        } else {
            throw new RuntimeException("Invalid version: " + version);
        }
    }

    public String b() {
        Matcher m = ABC_REGEX.matcher(version);
        if (m.find()) {
            return m.group(2);
        } else {
            throw new RuntimeException("Invalid version: " + version);
        }
    }

    public String c() {
        Matcher m = ABC_REGEX.matcher(version);
        if (m.find()) {
            return m.group(3);
        } else {
            throw new RuntimeException("Invalid version: " + version);
        }
    }

    public String fileName() {
        if ("com.banalytics.box".equals(groupId) && "core".equals(artifactId)) {
            return "banalytics-box.jar";
        }
        return groupId + "-" + artifactId + "-" + version + ".jar";
    }

    public Map.Entry<String, String> toEntry() {
        return Map.entry(
                groupId + ":" + artifactId + ":" + version,
                type + ":" + uploadTime.toEpochSecond(ZoneOffset.UTC)
        );
    }

//    public static Module fromEntry(String uri, String value) {
//        String[] parts1 = uri.split(":");
//        String[] parts2 = value.split(":");
//        return new Module(
//                parts1[0],
//                parts1[1],
//                parts1[2],
//                ModuleType.valueOf(parts2[0]),
//                LocalDateTime.ofEpochSecond(Long.parseLong(parts2[1]), 0, ZoneOffset.UTC)
//        );
//    }
}
