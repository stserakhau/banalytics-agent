package com.banalytics.box.module.toys.quadrocopter.model.command;

import com.fazecast.jSerialComm.SerialPort;

public interface Command<T> {
    short MSP_SET_REBOOT = 68;
    short MSP_RAW_IMU = 102;
    short MSP_ATTITUDE = 108;
    short MSP_ALTITUDE = 109;
    short MSP_ANALOG = 110;
    short MSP_SET_MOTOR = 214;

    public void execute(SerialPort port, T param);
}
