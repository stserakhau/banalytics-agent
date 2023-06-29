package com.banalytics.box.module;

public interface NetworkThing {
    String ipAddress();

    String macAddress();

    void onIpChanged(String newIp);

    void onMacFound(String mac);
}
