package com.banalytics.box.module.toys.quadrocopter;

import com.banalytics.box.module.AbstractThing;
import com.banalytics.box.module.BoxEngine;
import com.banalytics.box.module.toys.quadrocopter.model.Quadrocopter;
import com.fazecast.jSerialComm.SerialPort;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class QuadrocopterThing extends AbstractThing<QuadrocopterThingConfig> {

    @Override
    public String getTitle() {
        return configuration.title;
    }

    private SerialPort port;

    public QuadrocopterThing(BoxEngine engine) {
        super(engine);
    }

    @Override
    protected void doInit() throws Exception {
        port = SerialPort.getCommPort(configuration.getSerialPort());
        if (!port.setBaudRate(1000000)) {
            if (!port.setBaudRate(500000)) {
                if (!port.setBaudRate(250000)) {
                    if (!port.setBaudRate(115200)) {
                        if (!port.setBaudRate(57600)) {
                            if (!port.setBaudRate(38400)) {
                                if (!port.setBaudRate(28800)) {
                                    throw new Exception("Very slow port");
                                }
                            }
                        }
                    }
                }
            }
        }
        log.info("Actual port speed: {} bod", port.getBaudRate());
    }

    private Quadrocopter quadrocopter;

    public Quadrocopter getQuadrocopter() {
        return quadrocopter;
    }


    @Override
    protected void doStart() throws Exception {
        if (!port.isOpen()) {
            if (!port.openPort()) {
                throw new Exception("Can't open serial port: " + port);
            }
            Thread.sleep(300);
        }
        log.info("Port opened: {}", port.getPortDescription());
        this.quadrocopter = new Quadrocopter(
                this.port,
                configuration.checkCommandResponseInterval,
                configuration.stateRequestCycleTickInterval,
                configuration.imuRequestTik,
                configuration.analogRequestTik,
                configuration.attitudeRequestTik,
                configuration.altitudeRequestTik,
                configuration.engineStop,
                configuration.engineTest,
                configuration.engineStart,
                configuration.engineMax,
                configuration.trimmerPower,
                configuration.pidXTrimmerValues,
                configuration.pidYTrimmerValues,
                configuration.pidZTrimmerValues,
                configuration.disarmXYAngle
        );
//        quadrocopter.reboot();
        this.quadrocopter.start();
    }

    @Override
    protected void doStop() throws Exception {
        quadrocopter.stop();
//        if (port.isOpen()) {
//            if (!port.closePort()) {
//                log.error("Issue with closing the serial port: " + port);
//            }
//        }
    }
}
