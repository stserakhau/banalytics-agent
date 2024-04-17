package com.banalytics.box.module.toys.quadrocopter.model;

import com.fazecast.jSerialComm.SerialPort;

public class QuadTestRunner {
    public static void main(String[] args) throws Exception {
        SerialPort f405Port = findPort("F405");
//        f405Port.clearBreak();
//        f405Port.clearDTR();
//        f405Port.clearRTS();
//        f405Port.closePort();
        f405Port.openPort();

//        QuadrocopterPID q = new QuadrocopterPID(
//                f405Port,
//                50,
//                10,
//                5,
//                100,
//                10,
//                10,
//                0,
//                1008,
//                1009,
//                2000,
//                50,
//                50,
//                true
//        );
//
//        q.start();
//
//        q.targetPitch(0);q.targetHeading(0);q.targetRoll(0);
//        q.targetPower(0);
//        q.flushState();

//
//        short min = 1010;
//        short max = 2000;
//        short step = 1;
//        for(short power = min; power <= max; power += step) {
//            System.out.println(power);
//            q.vector(0, 0, 0, power);
//            Thread.sleep(10);
//        }
//        for(short power = max; power >= min; power -= step) {
//            System.out.println(power);
//            q.vector(0, 0, 0, power);
//            Thread.sleep(10);
//        }
//
//        q.vector(0, 0, 0, (short) 0);

        Thread.sleep(600000);

//        q.stop();

        f405Port.closePort();
    }

    public static SerialPort findPort(String part) throws Exception {
        SerialPort[] ports = SerialPort.getCommPorts();

        for (SerialPort port : ports) {
            if (port.getPortDescription().contains(part)) {
                return port;
            }
        }
        throw new Exception("Port not found: " + part);
    }
}
