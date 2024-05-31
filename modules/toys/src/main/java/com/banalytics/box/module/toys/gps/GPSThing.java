package com.banalytics.box.module.toys.gps;

import com.banalytics.box.api.integration.webrtc.channel.events.position.GeoPositionEvent;
import com.banalytics.box.module.AbstractThing;
import com.banalytics.box.module.BoxEngine;
import com.fazecast.jSerialComm.SerialPort;
import lombok.extern.slf4j.Slf4j;

import java.io.InputStream;

import static com.banalytics.box.module.utils.Utils.nodeType;

@Slf4j
public class GPSThing extends AbstractThing<GPSThingConfig> {
    @Override
    public String getTitle() {
        return configuration.title;
    }

    private SerialPort port;


    public GPSThing(BoxEngine engine) {
        super(engine);
    }

    private GeoPositionEvent geoPositionEvent;

    @Override
    protected void doInit() throws Exception {
        port = SerialPort.getCommPort(configuration.serialPort);

        geoPositionEvent = new GeoPositionEvent(
                nodeType(this.getClass()),
                this.getUuid(),
                getSelfClassName(),
                getTitle()
        );
    }

    private class GPSReaderJob implements Runnable {
        @Override
        public void run() {
            InputStream data = port.getInputStream();
            StringBuffer messageBuffer = new StringBuffer(512);
            while (!portResponseListener.isInterrupted()) {
                try {
                    int available;
                    while ((available = data.available()) == 0) {
                        Thread.sleep(50);
                    }
                    if (available == -1) {//todo on stop/crash vm
                        break;
                    }
                    byte[] buffer = new byte[available];
                    data.read(buffer);

                    for (int i = 0; i < buffer.length; i++) {
                        if (buffer[i] == '$') {
                            if (!messageBuffer.isEmpty()) {
                                String message = messageBuffer.toString();
                                fireMessage(message);
                                messageBuffer.setLength(0);
                            }
                        }
                        messageBuffer.append((char) buffer[i]);
                    }

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    break;
                }
            }
        }

        private void fireMessage(String message) {
            if (message.startsWith("$GNGGA")) {
                String[] parts = message.split(",");
                try {
                    geoPositionEvent.setLatitude(Double.parseDouble(parts[2]) / 100);
                    geoPositionEvent.setLongitude(Double.parseDouble(parts[4]) / 100);
                    geoPositionEvent.setAltitude(Double.parseDouble(parts[9]));
//                    System.out.println(message);
                } catch (NumberFormatException e) {
                    log.error(e.getMessage(), e);
                }

                engine.fireEvent(geoPositionEvent);
            } else if (message.startsWith("$GNVTG")) {
                String[] parts = message.split(",");

                try {
                    geoPositionEvent.setCourse(Double.parseDouble(parts[1]));
                    geoPositionEvent.setSpeed(Double.parseDouble(parts[7]));
//                    System.out.println(message);
                } catch (NumberFormatException e) {
                    log.error(e.getMessage(), e);
                }
            }
        }
    }
    private Thread portResponseListener;

    @Override
    protected void doStart() throws Exception {
        if(portResponseListener!=null) {
            doStop();
        }
        if (!port.openPort()) {
            throw new Exception("Can't open GPS port (error code/location): " + port.getLastErrorCode() + ": " + port.getLastErrorLocation());
        }

        portResponseListener = new Thread(new GPSReaderJob());
        portResponseListener.start();
    }

    @Override
    protected void doStop() throws Exception {
        portResponseListener.interrupt();
        portResponseListener = null;
        port.closePort();
    }
}
