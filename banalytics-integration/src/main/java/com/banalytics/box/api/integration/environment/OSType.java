package com.banalytics.box.api.integration.environment;

public enum OSType {
    COMMON("common"),
    WINDOWS_x86_64("windows-x86_64"),
    LINUX_x86_64("linux-x86_64"),
    MACOSX_x86_64("macosx-x86_64");

    public final String os;

    OSType(java.lang.String os) {
        this.os = os;
    }
}
