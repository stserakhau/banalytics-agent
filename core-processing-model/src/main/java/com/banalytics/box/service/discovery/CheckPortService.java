package com.banalytics.box.service.discovery;

import com.banalytics.box.service.discovery.api.APIDiscovery;
import com.banalytics.box.service.discovery.model.Device;
import com.banalytics.box.service.discovery.model.PortEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Service
@Slf4j
@RequiredArgsConstructor
public class CheckPortService {
    private final DefaultListableBeanFactory beanFactory;

    public void checkPorts(Device device) {
        if (device.getPortList() != null) {// skip check port if already exists
            return;
        }

        Set<PortEnum> portList = new HashSet<>();
        device.setPortList(portList);
        String hostIP = device.getIp();
        try {
            final InetAddress address = InetAddress.getByName(hostIP);
            for (PortEnum port : PortEnum.values()) {
                if (isRemotePortInUse(address, port.port())) {
                    portList.add(port);
                    log.info("Port found {}:{}", address, port);
                }
            }
        } catch (UnknownHostException e) {
            log.error(e.getMessage(), e);
            return;
        }

        for (PortEnum port : portList) {
            if (!ArrayUtils.isEmpty(port.apiDiscoveries())) {
                for (String discoveryBean : port.apiDiscoveries()) {
                    try {
                        Class<APIDiscovery> apiDiscoveryClass = (Class<APIDiscovery>) Class.forName(discoveryBean);
                        Map<String, APIDiscovery> apis = beanFactory.getBeansOfType(apiDiscoveryClass);
                        String className = apiDiscoveryClass.getSimpleName();
                        final String beanName = className.substring(0, 1).toLowerCase()
                                + className.substring(1);

                        APIDiscovery apiDiscovery = apis.get(beanName);
                        apiDiscovery.discovery(device, port);
                    } catch (ClassNotFoundException e) {
                        log.warn("Module with discovery tool for class {} not installed", discoveryBean);
                    }
                }
            }
        }
    }

    private boolean isRemotePortInUse(InetAddress inetAddress, int portNumber) {
        Socket sock = null;
        try {
            // Socket try to open a REMOTE port
            sock = new Socket(inetAddress, portNumber);
            // remote port can be opened, this is a listening port on remote machine
            // this port is in use on the remote machine !
            return true;
        } catch (Exception e) {
            // remote port is closed, nothing is running on
            log.error("Open sock error: {}", e.getMessage());
            return false;
        } finally {
            if (sock != null) {
                try {
                    sock.close();
                } catch (IOException e) {
                    log.error("Close sock error.", e);
                }
            }
        }
    }
}
