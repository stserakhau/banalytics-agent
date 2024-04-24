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
        port.setBaudRate(115200);
        port.setNumDataBits(8);
        port.setNumStopBits(1);
        port.setParity(SerialPort.NO_PARITY);
        port.setFlowControl(SerialPort.FLOW_CONTROL_DISABLED);
        //            serialPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 0, 0);
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
                throw new Exception("Can't open port.\n Error code: %s\nError location %s".formatted(
                        port.getLastErrorCode(),
                        port.getLastErrorLocation()
                ));
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
                configuration.engineStart,
                configuration.engineMin,
                configuration.engineRange
        );
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
