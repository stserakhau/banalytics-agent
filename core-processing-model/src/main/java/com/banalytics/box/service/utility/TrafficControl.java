package com.banalytics.box.service.utility;

import lombok.extern.slf4j.Slf4j;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
public final class TrafficControl {
    public final static TrafficControl INSTANCE = new TrafficControl();

    private TrafficControl() {
    }

    public final AtomicLong outboundTraffic = new AtomicLong(0);

    public boolean overhead = false;
    private int generalOnFlightDataLimit = 100000;// 200 kb
    private int fileTransmissionOnFlightDataLimit = 30000;// 200 kb
    private final double tikInterval = 0.100;// 100 msec
    private int freeGeneralResourceSpeedPerTik;
    private int freeFileTransmissionResourceSpeedPerTik;
    private final AtomicInteger generalOnFlightDataAmount = new AtomicInteger();//200k/sec
    private final AtomicInteger fileTransmissionOnFlightDataAmount = new AtomicInteger();//200k/sec

    public void setBandwidthConfig(int maxBandwidth, int fileTransmissionReservePercents) {
        init();
        generalOnFlightDataLimit = maxBandwidth;
        fileTransmissionOnFlightDataLimit = maxBandwidth * fileTransmissionReservePercents / 100;
        freeGeneralResourceSpeedPerTik = (int) (generalOnFlightDataLimit * tikInterval); // 200 kb/sec = 20kb/0.1sec //todo BUT !!! return resource can improve speed
        freeFileTransmissionResourceSpeedPerTik = (int) (fileTransmissionOnFlightDataLimit * tikInterval); // 200 kb/sec = 20kb/0.1sec //todo BUT !!! return resource can improve speed
    }

    boolean initialized = false;

    private final Timer timer = new Timer();

    private void init() {
        if (initialized) {
            return;
        }
        initialized = true;
        long interval = (long) (tikInterval * 1000);
        timer.schedule(new TimerTask() {//speed control
            @Override
            public void run() {//each tik free available data amount to transmission
                int val = generalOnFlightDataAmount.get();
                if (val > 0) {
//                    log.info("Tik general free {} / {}", generalOnFlightDataAmount.get(), freeGeneralResourceSpeedPerTik);
                    returnResource(generalOnFlightDataAmount, freeGeneralResourceSpeedPerTik);
                }
                int val1 = fileTransmissionOnFlightDataAmount.get();
                if (val1 > 0) {
//                    log.info("Tik file free {} / {}", fileTransmissionOnFlightDataAmount.get(), freeFileTransmissionResourceSpeedPerTik);
                    returnResource(fileTransmissionOnFlightDataAmount, freeFileTransmissionResourceSpeedPerTik);
                }
            }
        }, 0, interval);
    }

    private void returnResource(AtomicInteger counter, int dataSize) {
        int val = counter.get();
        if (val > 0) {
            val -= dataSize;
            if (val < 0) {
                val = 0;
            }
            counter.set(val);
        }
    }

    public void acquireGeneralResource(int dataAmount, boolean lock) throws Exception {
        outboundTraffic.addAndGet(dataAmount);
        int onFlightAmount = generalOnFlightDataAmount.addAndGet(dataAmount);
//        log.info("===== On general flight: {}. Requesting: {}", onFlightAmount, dataAmount);
        while (generalOnFlightDataAmount.get() > generalOnFlightDataLimit) {
            overhead = true;
            if (!lock) {
                return;
            }
            Thread.sleep(100);
        }
        overhead = false;
    }

    public void acquireFileTransmissionResource(int dataAmount) throws Exception {
        acquireGeneralResource(dataAmount, true);
        int onFlightAmount = fileTransmissionOnFlightDataAmount.addAndGet(dataAmount);
//        log.info("===== On file flight: {}. Requesting: {}", onFlightAmount, dataAmount);
        while (fileTransmissionOnFlightDataAmount.get() > fileTransmissionOnFlightDataLimit) {
            Thread.sleep(100);
        }
    }

    public boolean hasGeneralOnFlightOverhead() {
        return generalOnFlightDataAmount.get() > generalOnFlightDataLimit;
    }

    public double bandwidthLoad() {
        return 1 - ((double) generalOnFlightDataLimit - generalOnFlightDataAmount.get()) / generalOnFlightDataLimit;
    }
}
