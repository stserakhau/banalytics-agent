package com.banalytics.box.module.toys.quadrocopter.model.command;

import com.fazecast.jSerialComm.SerialPort;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.nio.ByteBuffer;
import java.util.function.Consumer;

import static com.banalytics.box.module.toys.quadrocopter.model.utils.PortUtils.send_message;

@ToString

public class Attitude implements Consumer<ByteBuffer>, Command<Void> {
    final Runnable onChangeCallback;

    public Attitude(Runnable onChangeCallback) {
        this.onChangeCallback = onChangeCallback;
    }

    /**
     * (x) крен -180..0..180
     */
    public double heading;

    /**
     * (y) угол наклона  90(смотрит вниз)..-90(мотрит вверх)
     */
    public double pitch;

    /**
     * (z) (курс) compass 0..360
     */
    public double roll;

    @Override
    public void execute(SerialPort port, Void param) {
        send_message(port, MSP_ATTITUDE, new byte[0]);
    }

    @Override
    public void accept(ByteBuffer data) {
        this.heading = data.getShort(5) / 10.0;
        this.pitch = data.getShort(7) / 10.0;
        this.roll = data.getShort(9);

        this.onChangeCallback.run();
    }
}
