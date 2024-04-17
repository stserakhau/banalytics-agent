package com.banalytics.box.module.toys.quadrocopter.model.command;

import com.fazecast.jSerialComm.SerialPort;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.nio.ByteBuffer;
import java.util.function.Consumer;

import static com.banalytics.box.module.toys.quadrocopter.model.utils.PortUtils.send_message;

@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Altitude implements Consumer<ByteBuffer>, Command<Void> {
    public int altitude; // altitude change ~ in centimeters

    @Override
    public void execute(SerialPort port, Void param) {
        send_message(port, MSP_ALTITUDE, new byte[0]);
    }

    @Override
    public void accept(ByteBuffer data) {
        altitude = data.getInt(5);
//        System.out.println(this);
    }
}
