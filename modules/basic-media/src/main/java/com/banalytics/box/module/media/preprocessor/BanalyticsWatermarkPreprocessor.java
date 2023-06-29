package com.banalytics.box.module.media.preprocessor;

import com.banalytics.box.module.AbstractListOfTask;
import com.banalytics.box.module.BoxEngine;
import com.banalytics.box.module.State;
import com.banalytics.box.module.media.task.watermark.WatermarkTask;
import com.banalytics.box.module.constants.PenFont;
import com.banalytics.box.module.constants.Place;
import org.bytedeco.javacv.Frame;
import org.bytedeco.opencv.opencv_core.Scalar;

public class BanalyticsWatermarkPreprocessor extends WatermarkTask {
    public BanalyticsWatermarkPreprocessor(BoxEngine metricDeliveryService, AbstractListOfTask<?> parent) {
        super(null, null);
        configuration.setCustomText("banalytics.live");
        configuration.setWatermarkPlace(Place.TOP_RIGHT);
        configuration.invertColor = true;
        configuration.penFont = PenFont.FONT_HERSHEY_SIMPLEX;
        configuration.fontThickness = 2;
        configuration.fontScale = 1;
        this.state = State.RUN;
        this.configuration.drawDateTime = false;
        this.configuration.drawVideoDetails = false;
        this.configuration.drawSourceTitle = false;
        this.configuration.drawTimeZone = false;
        this.penColor = new Scalar(255, 255, 255, 0);
    }

    @Override
    public synchronized void preProcess(Frame frame) {
        int imageWidth = frame.imageWidth;
        if (imageWidth <= 320) {
            configuration.fontScale = 0.4;
            configuration.fontThickness = 1;
        } else if (imageWidth <= 640) {
            configuration.fontScale = 0.5;
            configuration.fontThickness = 1;
        } else if (imageWidth <= 800) {
            configuration.fontScale = 0.7;
            configuration.fontThickness = 2;
        } else if (imageWidth <= 1200) {
            configuration.fontScale = 1;
            configuration.fontThickness = 2;
        } else if (imageWidth <= 2000) {
            configuration.fontScale = 1.5;
            configuration.fontThickness = 2;
        } else {
            configuration.fontScale = 1.3;
            configuration.fontThickness = 2;
        }
        super.preProcess(frame);
    }

    @Override
    public boolean hidden() {
        return true;
    }
}
