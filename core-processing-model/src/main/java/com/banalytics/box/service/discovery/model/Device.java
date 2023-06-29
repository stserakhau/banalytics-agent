package com.banalytics.box.service.discovery.model;

import com.banalytics.box.service.discovery.model.api.APIEnum;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@NoArgsConstructor
@ToString
@Getter
@Setter
public class Device {
    private String mac;
    private String ip;

    private Set<PortEnum> portList;

    private Map<APIEnum, Set<String>> apiCapabilities = new HashMap<>();

    public Device(String ip) {
        this.ip = ip;
    }
}
