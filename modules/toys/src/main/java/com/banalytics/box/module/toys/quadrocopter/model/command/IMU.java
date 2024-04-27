package com.banalytics.box.module.toys.quadrocopter.model.command;

import com.fazecast.jSerialComm.SerialPort;
import lombok.ToString;

import java.nio.ByteBuffer;
import java.util.function.Consumer;

import static com.banalytics.box.module.toys.quadrocopter.model.utils.PortUtils.send_message;

@ToString
public class IMU implements Consumer<ByteBuffer>, Command<Void> {

    public double accX;
    public double accY;
    public double accZ;

    public double gyroX;
    public double gyroY;
    public double gyroZ;

    public double magX;
    public double magY;
    public double magZ;

    @Override
    public void execute(SerialPort port, Void param) {
        send_message(port, MSP_RAW_IMU, new byte[0]);
    }

    @Override
    public void accept(ByteBuffer data) {
        this.accX = data.getShort(5) / 10.0;
        this.accY = data.getShort(7) / 10.0;
        this.accZ = data.getShort(9) / 10.0;

        this.gyroX = data.getShort(11) / 10.0;
        this.gyroY = data.getShort(13) / 10.0;
        this.gyroZ = data.getShort(15) / 10.0;

        this.magX = data.getShort(17);
        this.magY = data.getShort(19);
        this.magZ = data.getShort(21);
    }
}
