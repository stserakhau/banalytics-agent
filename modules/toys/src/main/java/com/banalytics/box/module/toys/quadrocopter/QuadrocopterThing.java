package com.banalytics.box.module.toys.quadrocopter;

import com.banalytics.box.api.integration.webrtc.channel.NodeDescriptor;
import com.banalytics.box.api.integration.webrtc.channel.events.position.GeoPositionEvent;
import com.banalytics.box.module.AbstractThing;
import com.banalytics.box.module.BoxEngine;
import com.banalytics.box.module.toys.quadrocopter.model.Quadrocopter;
import com.fazecast.jSerialComm.SerialPort;
import lombok.extern.slf4j.Slf4j;

import java.util.TimerTask;

import static com.banalytics.box.service.SystemThreadsService.SYSTEM_TIMER;

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


    private TimerTask droneStateTracker;

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
                configuration.engineStart,
                configuration.engineMin,
                configuration.engineRange
        );

        this.droneStateTracker = new TimerTask() {
            @Override
            public void run() {
                GeoPositionEvent gpe = new GeoPositionEvent(NodeDescriptor.NodeType.THING, QuadrocopterThing.this.getUuid(),
                        getSelfClassName(), getTitle());
                gpe.setAltitude(quadrocopter.altitude.altitude);
                gpe.setCourse(quadrocopter.attitude.roll);
                engine.fireEvent(gpe);
            }
        };
        SYSTEM_TIMER.schedule(this.droneStateTracker, 2000, 500);

        this.quadrocopter.start();
    }

    @Override
    protected void doStop() throws Exception {
        quadrocopter.stop();
        if (droneStateTracker != null) {
            droneStateTracker.cancel();
            droneStateTracker = null;
        }
//        if (port.isOpen()) {
//            if (!port.closePort()) {
//                log.error("Issue with closing the serial port: " + port);
//            }
//        }
    }
}
