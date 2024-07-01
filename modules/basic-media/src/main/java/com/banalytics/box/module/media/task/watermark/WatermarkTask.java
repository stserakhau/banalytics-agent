package com.banalytics.box.module.media.task.watermark;

import com.banalytics.box.TimeUtil;
import com.banalytics.box.api.integration.model.SubItem;
import com.banalytics.box.module.*;
import com.banalytics.box.module.constants.PenColor;
import com.banalytics.box.module.media.task.AbstractMediaGrabberTask;
import com.banalytics.box.module.standard.Onvif;
import org.apache.commons.lang3.StringUtils;
import org.bytedeco.javacpp.Pointer;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.OpenCVFrameConverter;
import org.bytedeco.opencv.global.opencv_core;
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

import static com.banalytics.box.module.ExecutionContext.GlobalVariables.CALCULATED_FRAME_RATE;
import static com.banalytics.box.module.ExecutionContext.GlobalVariables.SOURCE_TASK_UUID;
import static org.bytedeco.opencv.global.opencv_imgproc.getTextSize;
import static org.bytedeco.opencv.global.opencv_imgproc.putText;
import static org.opencv.imgproc.Imgproc.LINE_4;

@SubItem(of = {AbstractMediaGrabberTask.class}, group = "media-preprocessors")
public class WatermarkTask extends AbstractTask<WatermarkConfig> implements PreProcessor<Frame> {
    public WatermarkTask(BoxEngine metricDeliveryService, AbstractListOfTask<?> parent) {
        super(metricDeliveryService, parent);
    }

    @Override
    public Map<String, Class<?>> inSpec() {
        return Map.of(FrameGrabber.class.getName(), FrameGrabber.class, Frame.class.getName(), Frame.class, SOURCE_TASK_UUID.name(), UUID.class);
    }

    private final OpenCVFrameConverter.ToMat converter = new OpenCVFrameConverter.ToMat();
    protected Scalar penColor;
    private DateTimeFormatter dateTimeFormatter;
    private final List<Point> drawPoints = new ArrayList<>();
    private final List<Size> textSizes = new ArrayList<>();
    private final DecimalFormat fpsFormat = new DecimalFormat("000.00");

    @Override
    public Object uniqueness() {
        return configuration.watermarkPlace;
    }

    @Override
    public void doStart(boolean ignoreAutostartProperty, boolean startChildren) throws Exception {
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
        clear();
    }

    private void clear() {
        drawPoints.forEach(Pointer::close);
        drawPoints.clear();
        textSizes.forEach(Size::close);
        textSizes.clear();
    }

    @Override
    public synchronized void preProcess(Frame frame) {
        if (state != State.RUN || !frame.getTypes().contains(Frame.Type.VIDEO)) {
            return;
        }

        drawWatermark(frame);
    }

    private double sourceFps = 0;
    private double avgFps = 0;
    private Onvif.PTZ ptz;

    @Override
    protected boolean doProcess(ExecutionContext executionContext) throws Exception {
        avgFps = executionContext.getVar(CALCULATED_FRAME_RATE);
        FrameGrabber grabber = executionContext.getVar(FrameGrabber.class);
        sourceFps = grabber.getFrameRate();
        ptz = executionContext.getVar(Onvif.PTZ.class);
        return true;
    }

    private void drawWatermark(Frame frame) {
        if (!configuration.drawSourceTitle && !configuration.drawDateTime && !configuration.drawTimeZone
                && !configuration.drawVideoDetails && StringUtils.isEmpty(configuration.customText)) {
            return;
        }

        Mat colorFrame = converter.convert(frame);

        List<String> watermark = new ArrayList<>();
        if (configuration.drawSourceTitle) {
            Thing<?> source = getSourceThing();
            watermark.add(source.getTitle());
        }
        if (configuration.drawVideoDetails) {
            String details = frame.imageWidth + "x" + frame.imageHeight + " " + fpsFormat.format(avgFps) + "/" + fpsFormat.format(sourceFps) + " fps";
            watermark.add(details);
            /*if (ptz != null) {
                watermark.add("pan: " + ptz.pan() + " tilt:" + ptz.tilt() + " zoom:" + ptz.zoom());
                if (ptz.moving() || ptz.zooming()) {
                    watermark.add("moving: " + ptz.moving() + " zooming: " + ptz.zooming());
                }
            }*/
        }
        if (configuration.drawDateTime) {
            watermark.add(dateTimeFormatter.format(TimeUtil.currentTimeInServerTz()));
        }

        if (configuration.drawTimeZone) {
            watermark.add(System.getProperty("user.timezone"));
        }

        if (StringUtils.isNotEmpty(configuration.customText)) {
            watermark.add(configuration.customText);
        }

        if (watermark.isEmpty()) {
            return;
        }

        int fontFace = configuration.penFont.index;
        int[] baseline = {0};

        int rowHeight = 0;

        int fw = frame.imageWidth;
        int fh = frame.imageHeight;

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

                switch (configuration.watermarkPlace) {
                    case TOP_LEFT -> {
                        drawPoints.add(new Point(0, yShift));
                    }
                    case TOP_CENTER -> {
                        drawPoints.add(new Point((fw - textW) / 2, yShift));
                    }
                    case TOP_RIGHT -> {
                        drawPoints.add(new Point(fw - textW, yShift));
                    }
                    case BOTTOM_LEFT -> {
                        drawPoints.add(new Point(0, fh - yShift - textH));
                    }
                    case BOTTOM_CENTER -> {
                        drawPoints.add(new Point((fw - textW) / 2, fh - yShift - textH));
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
