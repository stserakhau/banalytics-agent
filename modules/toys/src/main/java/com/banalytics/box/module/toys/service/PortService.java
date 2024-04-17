package com.banalytics.box.module.toys.service;

import com.fazecast.jSerialComm.SerialPort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class PortService {

    public List<String> uiPorts() {
        List<String> ports = new ArrayList<>();

        for (SerialPort port : SerialPort.getCommPorts()) {
            ports.add(port.getSystemPortName() + "~" + port.getPortDescription());
        }

        return ports;
    }

    public SerialPort[] availablePorts() {
        return SerialPort.getCommPorts();
    }

    public SerialPort getPort(String portDescriptor) {
        return SerialPort.getCommPort(portDescriptor);
    }
}
