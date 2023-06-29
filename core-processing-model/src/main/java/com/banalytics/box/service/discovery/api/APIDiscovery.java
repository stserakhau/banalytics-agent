package com.banalytics.box.service.discovery.api;


import com.banalytics.box.service.discovery.model.Device;
import com.banalytics.box.service.discovery.model.PortEnum;

public interface APIDiscovery {
    void discovery(Device device, PortEnum port);
}
