package com.banalytics.box.module.toys.quadrocopter.model.command;

import com.fazecast.jSerialComm.SerialPort;
import lombok.ToString;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.function.Consumer;

import static com.banalytics.box.module.toys.quadrocopter.model.utils.PortUtils.send_message;

@ToString
public class Engine implements Consumer<ByteBuffer>, Command<Void> {
    public final static int N = 4;

    public short prevE1;
    public short prevE2;
    public short prevE3;
    public short prevE4;

    public short e1;
    public short e2;
    public short e3;
    public short e4;

    private final byte[] data = new byte[8];

    private volatile boolean started = false;

    public boolean isStarted() {
        return started;
    }

    @Override
    public synchronized void execute(SerialPort port, Void param) {
        if (!started) {
            return;
        }

        if ((prevE1 - e1 == 0) && (prevE2 - e2 == 0) && (prevE3 - e3 == 0) && (prevE4 - e4 == 0)) {
//            System.out.println("Skip send engine command");
            return;
        }
        prevE1 = e1;
        prevE2 = e2;
        prevE3 = e3;
        prevE4 = e4;

        ByteBuffer bb = ByteBuffer.wrap(data);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        bb.putShort(e1);
        bb.putShort(e2);
        bb.putShort(e3);
        bb.putShort(e4);

        send_message(port, MSP_SET_MOTOR, data);

        /*send_message(port, MSP_SET_MOTOR, new byte[]{// MOTOR_DATA
                0x00, 0x00, //low byte / hi byte for motor 1
                0x00, 0x00, //low byte / hi byte for motor 2
                0x00, 0x00, //low byte / hi byte for motor 3
                0x00, 0x00  //low byte / hi byte for motor 4
        });*/
    }

    @Override
    public void accept(ByteBuffer byteBuffer) {
    }

    public synchronized void start(SerialPort port, short startValue) {
        started = true;
        e1 = e2 = e3 = e4 = startValue;
        execute(port, null);
    }

    public synchronized void stop(SerialPort port, short stopValue) {
        e1 = e2 = e3 = e4 = stopValue;
        execute(port, null);
        started = false;
    }
}
