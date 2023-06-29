package com.banalytics.box.service.discovery;

import com.banalytics.box.service.discovery.model.Device;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Repository
public class DeviceStorage {
    private final Map<String, Device> ipDeviceMap = new ConcurrentHashMap<>();

    public Collection<Device> findAll() {
        return ipDeviceMap.values().stream().sorted(Comparator.comparing(Device::getIp)).collect(Collectors.toList());
    }

    public Device findByIp(String hostIP) {
        return ipDeviceMap.get(hostIP);
    }

    public Device createWithIP(String hostIP) {
        Device device = new Device(hostIP);
        ipDeviceMap.put(hostIP, device);
        return device;
    }

    public void remove(String hostIP) {
        ipDeviceMap.remove(hostIP);
    }
}
