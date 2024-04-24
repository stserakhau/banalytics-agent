package com.banalytics.box.module.toys.quadrocopter.model;

import com.banalytics.box.module.toys.quadrocopter.model.command.*;
import com.banalytics.box.module.toys.quadrocopter.model.utils.PortUtils;
import com.fazecast.jSerialComm.SerialPort;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import static com.banalytics.box.module.toys.quadrocopter.model.command.Command.*;
import static com.banalytics.box.module.toys.quadrocopter.model.utils.PortUtils.EMPTY;

@Slf4j
public class Quadrocopter {

    private final IMU imu = new IMU(() -> {
    });
    private final Analog analog = new Analog();
    private final Attitude attitude = new Attitude(this::flushState);
    private final Altitude altitude = new Altitude();

    private final SerialPort port;
    private final int checkCommandResponseInterval;
    private final int stateRequestCycleTickInterval;
    private final int imuRequestTik;
    private final int analogRequestTik;
    private final int attitudeRequestTik;
    private final int altitudeRequestTik;

    private final int engineStart;
    private final int engineMin;
    private final int engineRange;


    public Quadrocopter(
            SerialPort port,
            int checkCommandResponseInterval,
            int stateRequestCycleTickInterval,
            int imuRequestTik,
            int analogRequestTik,
            int attitudeRequestTik,
            int altitudeRequestTik,
            int engineStart,
            int engineMin,
            int engineRange
    ) {
        this.port = port;
        this.checkCommandResponseInterval = checkCommandResponseInterval;
        this.stateRequestCycleTickInterval = stateRequestCycleTickInterval;
        this.imuRequestTik = imuRequestTik;
        this.analogRequestTik = analogRequestTik;
        this.attitudeRequestTik = attitudeRequestTik;
        this.altitudeRequestTik = altitudeRequestTik;
        this.engineStart = engineStart;
        this.engineMin = engineMin;
        this.engineRange = engineRange;

        this.throttle = (short) engineStart;
    }

    private final Thread portResponseListener = new Thread(new Runnable() {
        @Override
        public void run() {
            InputStream data = port.getInputStream();
            ByteArrayOutputStream baos = new ByteArrayOutputStream(1024);
            byte[] appendix = EMPTY;
            while (!portResponseListener.isInterrupted()) {
                try {
                    int available;
                    while ((available = data.available()) == 0) {
                        Thread.sleep(checkCommandResponseInterval);
                    }
                    if (available == -1) {//todo on stop/crash vm
                        break;
                    }
                    byte[] buffer = new byte[available];
                    data.read(buffer);

                    baos.reset();
                    baos.writeBytes(appendix);
                    baos.writeBytes(buffer);
                    PortUtils.SplitResult responses = PortUtils.split(baos.toByteArray());
                    appendix = responses.appendix();
                    for (byte[] resp : responses.parts()) {
                        ByteBuffer buf = ByteBuffer.wrap(resp);
                        buf.order(ByteOrder.LITTLE_ENDIAN);
                        byte type = buf.get(1);
                        switch (type) {
                            case 88 -> {
                                short code = buf.getShort(4);
                                PortUtils.receivedResponse(code);
                                switch (code) {
                                    // future commands
                                }
                            }
                            case 77 -> {
                                byte byteCode = buf.get(4);
                                ByteBuffer bb = ByteBuffer.allocate(2);
                                bb.order(ByteOrder.LITTLE_ENDIAN);
                                bb.put(byteCode);
                                bb.put((byte) 0);
                                short code = bb.getShort(0);
                                PortUtils.receivedResponse(code);
                                switch (code) {
                                    case MSP_RAW_IMU -> {
                                        imu.accept(buf);
                                    }
                                    case MSP_ATTITUDE -> {
                                        attitude.accept(buf);
                                    }
                                    case MSP_ALTITUDE -> {
                                        altitude.accept(buf);
                                    }
                                    case MSP_ANALOG -> {
                                        analog.accept(buf);
                                    }
                                    case MSP_SET_REBOOT -> {
                                        log.warn("Dron reboot");
                                    }
                                    case MSP_SET_MOTOR -> {
                                        //nothing it's acceptance of operation
                                    }
                                }
                            }
                        }
                    }
                } catch (IOException e) {
                    log.error(e.getMessage(), e);
                    break;
                } catch (InterruptedException e) {
                    log.info("Response listener thread stopped");
                    break;
                }
            }
        }
    });

    private final Thread stateRequestor = new Thread(new Runnable() {
        @Override
        public void run() {
            int counter = 0;
            while (!stateRequestor.isInterrupted()) {
                try {
                    Thread.sleep(stateRequestCycleTickInterval);
                    if (counter % imuRequestTik == 0) {
                        imu.execute(port, null);
                    }
                    if (counter % analogRequestTik == 0) {
                        analog.execute(port, null);
                    }
                    if (counter % attitudeRequestTik == 0) {
                        attitude.execute(port, null);
                    }
                    if (counter % altitudeRequestTik == 0) {
                        altitude.execute(port, null);
                    }
                } catch (InterruptedException e) {
                    log.info("State requestor thread stopped");
                    break;
                }
                counter++;
            }
        }
    });


    /**
     * @param targetPitch - value in range -1..0..1
     */
    public void targetPitch(double targetPitch) {
        this.pitch = (short) (1500 + targetPitch * 500);
    }

    /**
     * @param targetHeading - value in range -1..0..1
     */
    public void targetHeading(double targetHeading) {
        this.roll = (short) (1500 + targetHeading * 500);
    }

    /**
     * @param targetRoll - value in range -1..0..1
     */
    public void targetRoll(double targetRoll) {
        this.yaw = (short) (1500 + targetRoll * 500);
    }

    /**
     * @param targetThrottle - value in range 0..1
     */
    public void powerPosition(double targetThrottle) {
        this.throttle = (short) (engineMin + targetThrottle * engineRange);
    }

    short roll = 1500;
    short pitch = 1500;
    short throttle; // initializes with engineStart value
    short yaw = 1500;

    short[] aux = new short[14];

    private final Thread controlThread = new Thread(new Runnable() {
        @Override
        public void run() {
            try {
                for (int i = 0; i < 50; i++) {
                    sendMSPSetRawRC(port, (short) 1500, (short) 1500, (short) 900, (short) 1500, aux);//reset aux
                    Thread.sleep(50);
                }
                aux[0] = 2000; // arm mode
                for (int i = 0; i < 10; i++) {
                    sendMSPSetRawRC(port, (short) 1500, (short) 1500, (short) engineStart, (short) 1500, aux);//reset aux
                    Thread.sleep(50);
                }
                while (!controlThread.isInterrupted()) {
                    sendMSPSetRawRC(port, roll, pitch, throttle, yaw, aux);
                    Thread.sleep(50);
                }
            } catch (InterruptedException e) {
                log.info("Control thread stopped");
            }
        }
    });

    public void start() throws InterruptedException {
        portResponseListener.setPriority(Thread.MAX_PRIORITY);
        stateRequestor.setPriority(Thread.MAX_PRIORITY);
        controlThread.setPriority(Thread.MAX_PRIORITY);

        portResponseListener.start();
        stateRequestor.start();

        throttle = (short) engineStart;

        for (int i = 0; i < aux.length; i++) {
            aux[i] = 900;
        }


        controlThread.start();
    }

    public void stop() throws Exception {
        portResponseListener.interrupt();
        stateRequestor.interrupt();
        controlThread.interrupt();

        aux[0] = 900;
        sendMSPSetRawRC(port, (short) 1500, (short) 1500, (short) 900, (short) 1500, aux);
        Thread.sleep(1000);
        port.closePort();
        System.out.println("!!! STOPPED !!!");
    }

    public void flushState() {

    }


    private static void sendMSPSetRawRC(SerialPort port,
                                        short roll, short pitch, short throttle, short yaw,
                                        short[] aux
    ) {
        System.out.println("==== Current r/p/t/y = %s / %s / %s / %s : ".formatted(roll, pitch, throttle, yaw));
        final byte MSP_SET_RAW_RC = (byte) 200;

        byte[] data = new byte[8 + aux.length * 2];
        ByteBuffer bb = ByteBuffer.wrap(data);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        bb.putShort(roll);
        bb.putShort(pitch);
        bb.putShort(throttle);
        bb.putShort(yaw);
        for (short value : aux) {
            bb.putShort(value);
        }
//        System.out.println(Arrays.toString(data));
        PortUtils.send_message(port, MSP_SET_RAW_RC, data);
    }
}
