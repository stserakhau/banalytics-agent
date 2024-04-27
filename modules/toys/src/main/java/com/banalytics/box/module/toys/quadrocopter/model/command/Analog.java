package com.banalytics.box.module.toys.quadrocopter.model.command;

import com.fazecast.jSerialComm.SerialPort;
import lombok.ToString;

import java.nio.ByteBuffer;
import java.util.function.Consumer;

import static com.banalytics.box.module.toys.quadrocopter.model.utils.PortUtils.send_message;

@ToString
public class Analog implements Consumer<ByteBuffer>, Command<Void> {

    public double voltage;
    public double mAhdrawn;
    public double rssi;// 0-1023
    public double amperage; // A
    public double voltage2;

    @Override
    public void execute(SerialPort port, Void param) {
        send_message(port, MSP_ANALOG, new byte[0]);
    }

    @Override
    public void accept(ByteBuffer data) {
        this.voltage = data.get(5) / 10.0;
        this.mAhdrawn = data.getShort(6);
        this.rssi = data.getShort(8);// 0-1023
        this.amperage = data.getShort(10) / 100.0; // A
        this.voltage2 = data.getShort(12) / 100.0;

    }
}
