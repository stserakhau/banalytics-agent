package com.banalytics.box.module.media.task.tracking;

import com.banalytics.box.module.AbstractListOfTask;
import com.banalytics.box.module.BoxEngine;
import com.banalytics.box.module.ExecutionContext;
import com.banalytics.box.module.media.task.AbstractStreamingMediaTask;
import com.banalytics.box.service.SystemThreadsService;
import lombok.extern.slf4j.Slf4j;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.OpenCVFrameConverter;
import org.bytedeco.opencv.opencv_core.*;
import org.bytedeco.opencv.opencv_video.*;

import static com.banalytics.box.module.ExecutionContext.GlobalVariables.CALCULATED_FRAME_RATE;
import static com.banalytics.box.module.ExecutionContext.GlobalVariables.VIDEO_KEY_FRAME;
import static org.bytedeco.opencv.global.opencv_core.ACCESS_READ;
import static org.bytedeco.opencv.global.opencv_imgproc.*;

@Slf4j
public abstract class AbstractObjectTrackerTask<CONFIG extends AbstractObjectTrackerTaskConfig> extends AbstractStreamingMediaTask<CONFIG> {
    public AbstractObjectTrackerTask(BoxEngine engine, AbstractListOfTask<?> parent) {
        super(engine, parent);
    }

    private FrameGrabber grabber;

    @Override
    protected FrameGrabber getGrabber() {
        return grabber;
    }

    @Override
    protected int getAudioChannels() {
        return grabber.getAudioChannels();
    }

    private OpenCVFrameConverter.ToMat converter;

    private Tracker tracker;

    private Point centroidP;
    private Rect centroid;
    private Rect target;

    protected abstract Tracker createTracker() throws Exception;

    @Override
    public void doStart(boolean ignoreAutostartProperty, boolean startChildren) throws Exception {
        this.converter = new OpenCVFrameConverter.ToMat();
        this.tracker = createTracker();

        super.doStart(ignoreAutostartProperty, startChildren);
    }

    private final Object STOP_SYNC = new Object();

    @Override
    public void doStop() throws Exception {
        synchronized (STOP_SYNC) {
            if (this.converter != null) {
                this.converter.close();
            }
            if (this.tracker != null) {
                this.tracker.close();
                this.tracker = null;
            }
            if (this.centroid != null) {
                this.centroid.close();
                this.centroid = null;
            }
            if (this.target != null) {
                this.target.close();
                this.target = null;
            }
        }
        super.doStop();

    }

    int frameCounter = 0;

    boolean insideTracker = false;

    long nextTracking = 0;

    @Override
    protected boolean doProcess(ExecutionContext executionContext) throws Exception {
        if (tracker == null) {
            return true;
        }
        if (!insideTracker) {
            this.frameCounter++;
        }
        this.grabber = executionContext.getVar(FrameGrabber.class);
        Frame frame = executionContext.getVar(Frame.class);
        boolean videoKeyFrame = executionContext.getVar(VIDEO_KEY_FRAME, false);
        double frameRate = executionContext.getVar(CALCULATED_FRAME_RATE);
        frameRate = frameRate == 0 ? 10 : frameRate;
        if (centroid == null) {
            int width = frame.imageWidth;
            int height = frame.imageHeight;

            int centerH = width / 2;
            int centerV = height / 2;
            centroidP = new Point(centerH, centerV);

            int size = width / 20;
            centroid = new Rect(centerH - size, centerV - size, size * 2, size * 2);
        }
        try {
            if (frame != null && frame.image != null) {
                Mat streamColorFrame = converter.convert(frame);
                try (UMat colorFrame = streamColorFrame.getUMat(ACCESS_READ)) {
                    if (target == null) {
                        target = new Rect(this.centroid);
                        tracker.init(colorFrame, this.target);
                    } else {
                        long now = System.currentTimeMillis();
                        if (!insideTracker && now > nextTracking) {
                            insideTracker = true;
                            final UMat clonedColorMat = colorFrame.clone();
                            SystemThreadsService.execute(this, () -> {
                                try {
                                    long start = System.currentTimeMillis();
                                    synchronized (STOP_SYNC) {
                                        tracker.update(clonedColorMat, this.target);
                                    }
                                    long end = System.currentTimeMillis();
                                    nextTracking = end + configuration.stunTimeoutMillis;
                                    log.info("Tracking time: {} ms", (end - start));
                                } finally {
                                    clonedColorMat.close();
                                    insideTracker = false;
                                }
                            });
                        }
                        rectangle(streamColorFrame, this.target, Scalar.RED, 1, LINE_8, 0);

                        try (Point targetPoint = new Point(
                                this.target.x() + this.target.width() / 2,
                                this.target.y() + this.target.height() / 2)
                        ) {
                            line(streamColorFrame, centroidP, targetPoint, Scalar.BLUE, 1, LINE_4, 0);
                        }
                    }
                    rectangle(streamColorFrame, this.centroid, Scalar.GREEN, 1, LINE_4, 0);

                    //draw central rect
                    onFrameReceived(frame, videoKeyFrame, frameRate);
                }
            }
        } catch (Exception e) {
            onException(e);
        }
        return true;
    }

    @Override
    public void onFrameReceived(Frame frame, boolean videoKeyFrame, double frameRate, Object... args) throws Exception {
        super.onFrameReceived(frame, videoKeyFrame, frameRate, args);
        mediaStreamToClient(frame, frameRate);
    }
}
