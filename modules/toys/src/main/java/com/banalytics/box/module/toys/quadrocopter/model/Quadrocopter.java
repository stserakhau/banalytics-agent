package com.banalytics.box.module.toys.quadrocopter.model;

//import com.banalytics.box.module.toys.pid.MiniPID;
//import com.banalytics.box.module.toys.pid.PID;

import com.banalytics.box.module.toys.pid.PidXYZQuadro;
import com.banalytics.box.module.toys.quadrocopter.model.command.*;
import com.banalytics.box.module.toys.quadrocopter.model.utils.PortUtils;
import com.fazecast.jSerialComm.SerialPort;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.text.DecimalFormat;

import static com.banalytics.box.module.toys.quadrocopter.model.command.Command.*;
import static com.banalytics.box.module.toys.quadrocopter.model.utils.PortUtils.EMPTY;

@Slf4j
public class Quadrocopter {

    private final IMU imu = new IMU(() -> {
    });
    private final Analog analog = new Analog();
    private final Attitude attitude = new Attitude(this::flushState);
    private final Altitude altitude = new Altitude();

    private final Engine engine = new Engine();

    private final SerialPort port;
    private final int checkCommandResponseInterval;
    private final int stateRequestCycleTickInterval;
    private final int imuRequestTik;
    private final int analogRequestTik;
    private final int attitudeRequestTik;
    private final int altitudeRequestTik;

    private final int engineStop;
    private final int engineTest;
    private final int engineStart;
    private final int engineMax;
    private final int maxTrimmerPower;

    private final int engineRange;
    private final int disarmXYAngle;

    final PidXYZQuadro pidXYZQuadro;

    public Quadrocopter(
            SerialPort port,
            int checkCommandResponseInterval,
            int stateRequestCycleTickInterval,
            int imuRequestTik,
            int analogRequestTik,
            int attitudeRequestTik,
            int altitudeRequestTik,
            int engineStop,
            int engineTest,
            int engineStart,
            int engineMax,
            int maxTrimmerPower,
            String pidXTrimmerValues,
            String pidYTrimmerValues,
            String pidZTrimmerValues,
            int disarmXYAngle
    ) {
        this.port = port;
        this.checkCommandResponseInterval = checkCommandResponseInterval;
        this.stateRequestCycleTickInterval = stateRequestCycleTickInterval;
        this.imuRequestTik = imuRequestTik;
        this.analogRequestTik = analogRequestTik;
        this.attitudeRequestTik = attitudeRequestTik;
        this.altitudeRequestTik = altitudeRequestTik;
        this.engineStop = engineStop;
        this.engineTest = engineTest;
        this.engineStart = engineStart;
        this.engineMax = engineMax;
        this.engineRange = engineMax - engineStart;
        this.maxTrimmerPower = maxTrimmerPower;
        this.disarmXYAngle = disarmXYAngle;
        pidXYZQuadro = new PidXYZQuadro(
                pidXTrimmerValues,
                pidYTrimmerValues,
                pidZTrimmerValues,
                new PidXYZQuadro.Range(-90, 90),
                new PidXYZQuadro.Range(-180, 180),
                new PidXYZQuadro.Range(-360, 360)
        );
//        pidX = new ClassicPID(pidXValues);
////        pidX.setOutputRampRate();
////        pidX.setOutputFilter();
//        pidY = new ClassicPID(pidYValues);
//        pidZ = new ClassicPID(pidZValues);
//
//        pidX.setRange(-maxTrimmerXPower, maxTrimmerXPower);
//        pidY.setRange(-maxTrimmerYPower, maxTrimmerYPower);
//        pidZ.setRange(-10, 10);
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
//                } catch (SerialPortTimeoutException e) {
//                    e.printStackTrace();
//                    try {
//                        Thread.sleep(readTimeoutMillis);
//                    } catch (InterruptedException ex) {
//                        e.printStackTrace();
//                    }
                } catch (IOException e) {
                    log.error(e.getMessage(), e);
                    break;
                } catch (InterruptedException e) {
                    break;
                }
//                System.out.println("==== " + PortUtils.reqCounter.get() + " / " + PortUtils.resCounter.get());
            }
        }
    });

    private final Thread stateRequestor = new Thread(new Runnable() {
        @Override
        public void run() {
            testPower((short) engineStop);   //engine stop
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
                    break;
                }
                counter++;
            }
        }
    });

    public void start() throws InterruptedException {
        engine.start(port, (short) engineStart);

        portResponseListener.setPriority(Thread.MAX_PRIORITY);
        stateRequestor.setPriority(Thread.MAX_PRIORITY);

        portResponseListener.start();
        stateRequestor.start();

//        test();
    }

//    public void reboot() throws Exception{
//        send_message(port, MSP_SET_REBOOT, new byte[0]);
//        Thread.sleep(5000);
//        if(!port.isOpen()) {
//            if(port.openPort()) {
//                log.info("Reboot successful");
//            }
//        }
//    }

    private void test() throws InterruptedException {
        testPower((short) engineTest);   //engine test mode
        Thread.sleep(500);

//        testPower((short) engineStart);  //engine most slow
//        Thread.sleep(2000);

        testPower((short) engineStop);   //engine stop
    }

    public void disarm() {
        System.out.println("!!! DISARMED !!!");
        stop();
    }
    public void stop() {
        engine.stop(port, (short) engineStop);
        portResponseListener.interrupt();
        stateRequestor.interrupt();
        System.out.println("!!! STOPPED !!!");
    }

    /**
     * @param dRoll   (x) -1..0..1 скорость приращения крена (вращение вокруг оси камеры влево/вправо)
     * @param dPitch  (y) -1..0..1 скорость приращения угла наклона  (вверх/вниз)
     * @param dRotate (z) -1..0..1 скорость вращение вокруг перпендикулоярной оси
     * @param power   0..1 подача энергии на двигатели (полный стоп, полный газ)
     */
//    double targetRoll = 0;
//    double targetPitch = 0;
//    double targetHeading = 0;

    /**
     * engineRange * [gamepadPosision]
     */
    double targetPower = 0;

    DecimalFormat DF = new DecimalFormat("#0.00");

    public void targetPitch(double targetPitch) {
        pidXYZQuadro.setTargetPitch(90 * targetPitch);
    }

    public void targetHeading(double targetHeading) {
        pidXYZQuadro.setTargetHeading(180 * targetHeading);
    }

    public void targetRoll(double targetRoll) {
        pidXYZQuadro.setTargetRollSpeed(30 * targetRoll);
    }

    /**
     * @param gamepadPosition - value in range 0..1
     */
    public void powerPosition(double gamepadPosition) {
        if (gamepadPosition < 0) {
            gamepadPosition = 0;
        }
        this.targetPower = engineRange * gamepadPosition;
    }

    public void testPower(short power) {
        engine.e1 = power;
        engine.e2 = power;
        engine.e3 = power;
        engine.e4 = power;

        engine.execute(port, null);
    }

    public void flushState() {
        checkDisarmedState();

        if (!engine.isStarted()) {
            return;
        }

        int commonPower = (int) (engineStart + targetPower);
        //                           ^^
        //                        e4 || e2
        //                        e3 || e1
        //                           --

        double[] eVect = pidXYZQuadro.engineVectors(attitude.pitch, attitude.heading, attitude.roll);

        double e1Power = commonPower + eVect[0] * maxTrimmerPower;
        double e2Power = commonPower + eVect[1] * maxTrimmerPower;
        double e3Power = commonPower + eVect[2] * maxTrimmerPower;
        double e4Power = commonPower + eVect[3] * maxTrimmerPower;

        int _max = this.engineMax;
        e1Power = e1Power > _max ? _max : e1Power;
        e2Power = e2Power > _max ? _max : e2Power;
        e3Power = e3Power > _max ? _max : e3Power;
        e4Power = e4Power > _max ? _max : e4Power;

        int _min = this.engineStart;
        e1Power = e1Power < _min ? _min : e1Power;
        e2Power = e2Power < _min ? _min : e2Power;
        e3Power = e3Power < _min ? _min : e3Power;
        e4Power = e4Power < _min ? _min : e4Power;

        if (true) {
//            System.out.println(DF.format(analog.amperage) + "\t\t" + DF.format(analog.mAhdrawn) + "\t\t" + DF.format(analog.voltage) + "\t\t" + DF.format(analog.voltage2) + "\t\t" + DF.format(analog.rssi) );
//            System.out.println(DF.format(commonPower) + "\t\t^^\t\t" + DF.format(balanceXPower) + "\t\t" + DF.format(balanceYPower));
//            System.out.println(DF.format(attitude.heading) + "\t\t^^\t\t" + DF.format(additinalXPower));
//            System.out.println(DF.format(attitude.pitch) + "\t" + DF.format(commonPower));
            System.out.println(
                    DF.format(attitude.heading) + "\t" + DF.format(attitude.pitch) + "\t" + DF.format(attitude.roll)
                            + "\t" + DF.format(e1Power) + "\t" + DF.format(e2Power) + "\t" + DF.format(e3Power) + "\t" + DF.format(e4Power));
//            System.out.println(DF.format(attitude.pitch) + "\t\t^^\t\t" + DF.format(additinalYPower));
//            System.out.println("\t" + DF.format(e1Power) + "\t||\t" + DF.format(e3Power));
//            System.out.println("\t" + DF.format(e2Power) + "\t||\t" + DF.format(e4Power));
//            System.out.println("\t\t--");
        }

        engine.e1 = (short) e1Power;
        engine.e2 = (short) e2Power;
        engine.e3 = (short) e3Power;
        engine.e4 = (short) e4Power;

        engine.execute(port, null);
        /* */
    }

    public void checkDisarmedState() {
        if (Math.abs(attitude.heading) < disarmXYAngle && Math.abs(attitude.pitch) < disarmXYAngle) {
            return;
        }
        disarm();
    }
}
