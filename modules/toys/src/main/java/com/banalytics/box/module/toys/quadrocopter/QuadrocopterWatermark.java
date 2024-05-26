package com.banalytics.box.module.toys.quadrocopter;

import com.banalytics.box.TimeUtil;
import com.banalytics.box.api.integration.model.SubItem;
import com.banalytics.box.module.*;
import com.banalytics.box.module.constants.PenColor;
import com.banalytics.box.module.constants.Place;
import com.banalytics.box.module.media.task.AbstractMediaGrabberTask;
import com.banalytics.box.module.toys.quadrocopter.model.Quadrocopter;
import org.apache.commons.lang3.tuple.Triple;
import org.bytedeco.javacpp.Pointer;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.OpenCVFrameConverter;
import org.bytedeco.opencv.global.opencv_core;
import org.bytedeco.opencv.global.opencv_imgproc;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.Point;
import org.bytedeco.opencv.opencv_core.Scalar;
import org.bytedeco.opencv.opencv_core.Size;

import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.banalytics.box.module.ExecutionContext.GlobalVariables.SOURCE_TASK_UUID;
import static com.banalytics.box.module.State.RUN;
import static com.banalytics.box.module.constants.Place.*;
import static org.bytedeco.opencv.global.opencv_imgproc.*;
import static org.bytedeco.opencv.opencv_core.AbstractScalar.*;

@SubItem(of = {AbstractMediaGrabberTask.class}, group = "media-preprocessors")
public class QuadrocopterWatermark extends AbstractTask<QuadrocopterWatermarkConfig> implements PreProcessor<Frame> {

    private final DecimalFormat dblFormat = new DecimalFormat("000.000");

    public QuadrocopterWatermark(BoxEngine metricDeliveryService, AbstractListOfTask<?> parent) {
        super(metricDeliveryService, parent);
    }

    @Override
    public Map<String, Class<?>> inSpec() {
        return Map.of(FrameGrabber.class.getName(), FrameGrabber.class, Frame.class.getName(), Frame.class, SOURCE_TASK_UUID.name(), UUID.class);
    }

    private final OpenCVFrameConverter.ToMat converter = new OpenCVFrameConverter.ToMat();
    protected Scalar penColor;
    private DateTimeFormatter dateTimeFormatter;

    @Override
    public Object uniqueness() {
        return configuration.quadrocopterThingUuid;
    }

    private QuadrocopterThing quadrocopterThing;
    private Quadrocopter quadrocopter;

    @Override
    public void doStart(boolean ignoreAutostartProperty, boolean startChildren) throws Exception {
        this.quadrocopterThing = engine.getThingAndSubscribe(configuration.quadrocopterThingUuid, this);
        this.quadrocopter = this.quadrocopterThing.getQuadrocopter();
        if (quadrocopterThing.getState() != RUN) {
            throw new Exception("Quadrocopter not started");
        }

        PenColor color = configuration.getPenColor();
        penColor = new Scalar(
                color.blue,
                color.green,
                color.red,
                color.alpha
        );

        dateTimeFormatter = DateTimeFormatter.ofPattern(configuration.dateFormat.pattern);

        clear();
    }

    @Override
    public void doStop() throws Exception {
        this.quadrocopterThing.unSubscribe(this);
        this.quadrocopter = null;
        clear();
    }

    private void clear() {
        watermarkPlace.forEach((k, v) -> {
            v.getMiddle().forEach(Pointer::close);
            v.getMiddle().clear();
            v.getRight().forEach(Pointer::close);
            v.getRight().clear();
        });
    }

    @Override
    public synchronized void preProcess(Frame frame) {
        if (state != RUN || !frame.getTypes().contains(Frame.Type.VIDEO)) {
            return;
        }

        drawWatermark(frame);
    }

    @Override
    protected boolean doProcess(ExecutionContext executionContext) throws Exception {
        return true;
    }


    private final List<String> TL = new ArrayList<>();
    private final List<String> TR = new ArrayList<>();
    private final List<String> BL = new ArrayList<>();
    private final List<String> BR = new ArrayList<>();

    private final Map<Place, Triple<List<String>, List<Point>, List<Size>>> watermarkPlace = Map.of(
            TOP_LEFT, Triple.of(TL, new ArrayList<>(), new ArrayList<>()),
            TOP_RIGHT, Triple.of(TR, new ArrayList<>(), new ArrayList<>()),
            BOTTOM_LEFT, Triple.of(BL, new ArrayList<>(), new ArrayList<>()),
            BOTTOM_RIGHT, Triple.of(BR, new ArrayList<>(), new ArrayList<>())
    );

    private final Point centroidPoint = new Point(0, 0);

    private final Point midPointTop = new Point(0, 0);
    private final Point midPointBottom = new Point(0, 0);
    private final Point midPointLeft = new Point(0, 0);
    private final Point midPointRight = new Point(0, 0);

    private final Point accelerationVectorPoint = new Point(0, 0);
    private final Point gyroVectorPoint = new Point(0, 0);

    private void drawWatermark(Frame frame) {
        Mat colorFrame = converter.convert(frame);
        TR.clear();
        TL.clear();

        TR.add(dateTimeFormatter.format(TimeUtil.currentTimeInServerTz()));
        TR.add("Head: " + dblFormat.format(quadrocopter.attitude.heading));
        TR.add("Pitc: " + dblFormat.format(quadrocopter.attitude.pitch));

        //TL.add("AccZ: " + dblFormat.format(quadrocopter.imu.accZ));
//        TL.add("");
//        TL.add("GyroZ: " + dblFormat.format(quadrocopter.imu.gyroZ));
//        TL.add("");
        TL.add("MagX: " + dblFormat.format(quadrocopter.imu.magX));
        TL.add("MagY: " + dblFormat.format(quadrocopter.imu.magY));
        TL.add("MagZ: " + dblFormat.format(quadrocopter.imu.magZ));

        TL.add("Amp: " + dblFormat.format(quadrocopter.analog.amperage));
        TL.add("mAh: " + dblFormat.format(quadrocopter.analog.mAhdrawn));
        TL.add("Rssi: " + dblFormat.format(quadrocopter.analog.rssi));
        TL.add("Volt1: " + dblFormat.format(quadrocopter.analog.voltage));
        TL.add("Volt2: " + dblFormat.format(quadrocopter.analog.voltage2));


        int fontFace = configuration.penFont.index;
        int[] baseline = {0};

        int rowHeight = 0;

        int fw = frame.imageWidth;
        int fh = frame.imageHeight;

        int centerX = (fw >> 1);
        int centerY = (fh >> 1);
        int centroidX = centerX + configuration.centroidShiftX;
        int centroidY = centerY - configuration.centroidShiftY;
        centroidPoint.x(centroidX);
        centroidPoint.y(centroidY);
        {
            midPointLeft.x(0);
            midPointLeft.y(centroidY);
            midPointRight.x(fw - 1);
            midPointRight.y(centroidY);
            midPointTop.x(centroidX);
            midPointTop.y(0);
            midPointBottom.x(centroidX);
            midPointBottom.y(fw);

            line(colorFrame, midPointLeft, midPointRight, GREEN, 1, opencv_imgproc.LINE_4, 0);
            line(colorFrame, midPointTop, midPointBottom, GREEN, 1, opencv_imgproc.LINE_4, 0);
        }
        {//compass
            try (
                    Point p1 = new Point(centerX - 200, fh - 5);
                    Point p2 = new Point(centerX + 200, fh - 5)
            ) {
                line(colorFrame, p1, p2, WHITE, 1, opencv_imgproc.LINE_4, 0);

                int rollValue = (int) quadrocopter.attitude.roll;

                p1.x(centerX - 10).y(fh - 30);
                putText(colorFrame, "" + rollValue, p1, fontFace, 0.45, penColor, 1, LINE_4, false);

                int rangeStart = (rollValue - 50) / 10 * 10;

                for (int x = -200; x <= 200; x += 40) {
                    p1.x(centerX + x).y(fh - 5);
                    p2.x(centerX + x).y(fh - 15);
                    line(colorFrame, p1, p2, WHITE, 1, opencv_imgproc.LINE_4, 0);
                }
            }
        }
        {//Altitude
            try (
                    Point p1 = new Point(fw - 10, centerY - 200);
                    Point p2 = new Point(fw - 10, centerY + 200)
            ) {
                //render grid
                line(colorFrame, p1, p2, WHITE, 1, opencv_imgproc.LINE_4, 0);

                int altValue = 100;
                for (int i = -200; i <= 200; i += 20) {
                    p1.x(fw - 20).y(centerY + i);
                    p2.x(fw - 10).y(centerY + i);
                    line(colorFrame, p1, p2, WHITE, 1, opencv_imgproc.LINE_4, 0);

                    if (i % 40 == 0) {
                        p1.x(fw - 50).y(centerY + i + 4);
                        putText(colorFrame, "" + altValue, p1, fontFace, 0.45, penColor, 1, LINE_4, false);
                    }
                    altValue -= 5;
                }
                //render value
                double altInMeters = quadrocopter.altitude.altitude / 100.0;
                int bottomPos = centerY + 200;
                int valuePos = (int) (bottomPos - altInMeters);
                p1.x(fw - 20).y(valuePos);
                p2.x(fw - 10).y(valuePos);
                line(colorFrame, p1, p2, GREEN, 2, opencv_imgproc.LINE_4, 0);

                p1.x(fw - 70).y(valuePos + 5);
                putText(colorFrame, "" + altInMeters, p1, fontFace, 0.5, GREEN, 1, LINE_4, false);
            }
        }
        {//gyro & acc
            int scale = 2;
            accelerationVectorPoint.x(centroidX + (int) (quadrocopter.imu.accY * scale));
            accelerationVectorPoint.y(centroidY - (int) (quadrocopter.imu.accX * scale));
            line(colorFrame, centroidPoint, accelerationVectorPoint, RED, 2, opencv_imgproc.LINE_4, 0);

            gyroVectorPoint.x(centroidX + (int) (quadrocopter.imu.gyroX * scale));
            gyroVectorPoint.y(centroidY + (int) (quadrocopter.imu.gyroY * scale));
            line(colorFrame, centroidPoint, gyroVectorPoint, YELLOW, 2, opencv_imgproc.LINE_4, 0);

            try (
                    Point p1 = new Point(0, 0);
                    Point p2 = new Point(0, 0);
            ) {
                for (int i = -30 * scale; i <= 30 * scale; i += 10 * scale) {
                    p1.x(centroidX + i).y(centroidY - 10);
                    p2.x(centroidX + i).y(centroidY + 10);
                    line(colorFrame, p1, p2, RED, 1, opencv_imgproc.LINE_4, 0);

                    p1.x(centroidX - 10).y(centroidY + i);
                    p2.x(centroidX + 10).y(centroidY + i);
                    line(colorFrame, p1, p2, RED, 1, opencv_imgproc.LINE_4, 0);
                }
            }
        }


        for (Map.Entry<Place, Triple<List<String>, List<Point>, List<Size>>> entry : watermarkPlace.entrySet()) {
            Place key = entry.getKey();
            Triple<List<String>, List<Point>, List<Size>> t = entry.getValue();
            List<String> watermark = t.getLeft();
            List<Point> drawPoints = t.getMiddle();
            List<Size> textSizes = t.getRight();

            if (drawPoints.size() != watermark.size()) {
                this.clear();
            }

            if (drawPoints.isEmpty()) {
                for (int i = 0; i < watermark.size(); i++) {
                    String text = watermark.get(i);

                    Size textSize = getTextSize(text, fontFace, configuration.fontScale, configuration.fontThickness, baseline);
                    textSizes.add(textSize);
                    int textW = textSize.width();
                    int textH = textSize.height() + 5;

                    rowHeight = Math.max(rowHeight, textH);

                    int yShift = i * rowHeight;

                    switch (key) {
                        case TOP_LEFT -> {
                            drawPoints.add(new Point(0, yShift));
                        }
                        case TOP_RIGHT -> {
                            drawPoints.add(new Point(fw - textW, yShift));
                        }
                        case BOTTOM_LEFT -> {
                            drawPoints.add(new Point(0, fh - yShift - textH));
                        }
                        case BOTTOM_RIGHT -> {
                            drawPoints.add(new Point(fw - textW, fh - yShift - textH));
                        }
                    }
                }
            }

            for (int i = 0; i < watermark.size(); i++) {
                String text = watermark.get(i);
                Size size = textSizes.get(i);
                Point drawPoint = drawPoints.get(i);
                int textH = size.height() + 5;
                int textW = size.width();
                try (Mat waterMarkMask = new Mat(textH, textW, colorFrame.type(), Scalar.all(0))) {
                    putText(waterMarkMask, text, new Point(0, textH - 5), fontFace,
                            configuration.fontScale,
                            penColor,
                            configuration.fontThickness,
                            LINE_4,
                            false);

                    int topMax = drawPoint.y() + textH;

                    int widthMax = drawPoint.x() + textW;

                    try {
                        Mat wmArea = colorFrame
                                .rowRange(drawPoint.y(), topMax)
                                .colRange(drawPoint.x(), widthMax);

                        if (configuration.invertColor) {
                            opencv_core.bitwise_xor(wmArea, waterMarkMask, wmArea);
                        } else {
                            opencv_core.bitwise_or(wmArea, waterMarkMask, wmArea);
                        }
                    } catch (Throwable e) {
                        onException(new Exception("error.decreaseFontSize"));
                        break;
                    }
                }
            }
        }
    }
}
