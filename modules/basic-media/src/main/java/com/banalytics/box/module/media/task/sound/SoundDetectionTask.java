package com.banalytics.box.module.media.task.sound;

import com.banalytics.box.api.integration.webrtc.channel.NodeDescriptor;
import com.banalytics.box.api.integration.webrtc.channel.events.AbstractEvent;
import com.banalytics.box.api.integration.webrtc.channel.events.SoundEvent;
import com.banalytics.box.module.AbstractListOfTask;
import com.banalytics.box.module.AbstractTask;
import com.banalytics.box.module.BoxEngine;
import com.banalytics.box.module.ExecutionContext;
import com.banalytics.box.module.media.task.motion.detector.MotionDetectionTask;
import com.banalytics.box.module.media.task.motion.storage.MotionVideoRecordingTask;
import com.banalytics.box.module.media.task.sound.utils.Spectrogram;
import com.banalytics.box.module.utils.ConvertionUtils;
import lombok.extern.slf4j.Slf4j;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber;

import java.nio.ByteBuffer;
import java.nio.ShortBuffer;
import java.text.DecimalFormat;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import static com.banalytics.box.module.ExecutionContext.GlobalVariables.AUDIO_MOTION_DETECTED;
import static com.banalytics.box.module.utils.ConvertionUtils.averageHistoryMagnitude;
import static com.banalytics.box.module.utils.Utils.nodeType;

@Slf4j
public class SoundDetectionTask extends AbstractTask<SoundDetectionConfig> {
    final NodeDescriptor.NodeType NODE_TYPE = nodeType(this.getClass());

    public SoundDetectionTask(BoxEngine metricDeliveryService, AbstractListOfTask<?> parent) {
        super(metricDeliveryService, parent);
    }

    @Override
    public Map<String, Class<?>> inSpec() {
        return Map.of(Frame.class.getName(), Frame.class);
    }

    private final LinkedList<double[]> backgroundNoiseHistory = new LinkedList<>();


    long soaTimeoutValue = 0;
    long soaTimeout;

    @Override
    public void doStart(boolean ignoreAutostartProperty, boolean startChildren) throws Exception {
        backgroundNoiseHistory.clear();
        if (configuration.historyLength > 1) {
            soaTimeoutValue = configuration.speedOfAccustomSec * 1000L / configuration.historyLength;
        } else {
            soaTimeoutValue = -1;
        }
        soaTimeout = 0;
        sampleBuffer = null;
        super.doStart(ignoreAutostartProperty, startChildren);
    }

//    private final ShortBuffer soundBuffer = ShortBuffer.allocate(16 * 1024);

    DecimalFormat magFmt = new DecimalFormat("000.00");

    int sampleRate;

    int bufferHead;
    short[] sampleBuffer;

    private void push(short[] sample, Function<short[], Void> callback) {
        int sampleAppendix;
        do {
            int capacity = sampleBuffer.length - bufferHead;
            sampleAppendix = sample.length - capacity;
            if (sampleAppendix <= 0) {
                System.arraycopy(sample, 0, sampleBuffer, bufferHead, sample.length);
                bufferHead += sample.length;
            } else {
                System.arraycopy(sample, 0, sampleBuffer, bufferHead, capacity);
                callback.apply(sampleBuffer);
                bufferHead += capacity;
            }

            if (bufferHead == sampleBuffer.length) {
                callback.apply(sampleBuffer);
                bufferHead = 0;
            }
        } while (sampleAppendix > 0);
    }

    @Override
    protected boolean doProcess(ExecutionContext executionContext) throws Exception {
        Frame frame = executionContext.getVar(Frame.class);

        if (frame.samples == null) {
            return true;
        }
        if (sampleBuffer == null) {
            FrameGrabber grabber = executionContext.getVar(FrameGrabber.class);
            this.sampleRate = grabber.getSampleRate();
            sampleBuffer = new short[Spectrogram.align(sampleRate)];
            bufferHead = 0;
        }

        try {
            final ShortBuffer channelSamplesShortBuffer = (ShortBuffer) frame.samples[0];
            channelSamplesShortBuffer.rewind();

            final ByteBuffer outBuffer = ByteBuffer.allocate(channelSamplesShortBuffer.capacity() * 2);

            for (int i = 0; i < channelSamplesShortBuffer.capacity(); i++) {
                short val = channelSamplesShortBuffer.get(i);
                outBuffer.putShort(val);
            }

            byte[] rtspData = outBuffer.array();

            short[] sample = ConvertionUtils.convertBigEndian(rtspData);

            push(sample, data -> {
                Spectrogram spectr = Spectrogram.buildSpectr(data, configuration.specterSensitivity);

                double[] magnitude = spectr.scaledMagnitude;

                if (backgroundNoiseHistory.size() < configuration.historyLength) {//accumulate history
                    backgroundNoiseHistory.addLast(magnitude);
                }

                double[] magnitudeAverage = averageHistoryMagnitude(backgroundNoiseHistory);

                //detect new noices
                boolean noiseDetected = false;
                for (int i = 0; i < magnitudeAverage.length; i++) {
                    double k = magnitude[i] / magnitudeAverage[i];
                    if (k > configuration.threshold) {
                        noiseDetected = true;
                    }
                }
                if (configuration.debug || noiseDetected) {
                    SoundEvent evt = new SoundEvent(
                            NODE_TYPE,
                            getUuid(),
                            getSelfClassName(),
                            getTitle(),
                            getSourceThingUuid(),
                            configuration.debug,
                            magnitude,
                            magnitudeAverage
                    );
                    engine.fireEvent(evt);
                }

                long now = System.currentTimeMillis();
                if (configuration.applyThresholdExceed) {
                    if (noiseDetected || now > soaTimeout) {
                        backgroundNoiseHistory.removeFirst();
                        backgroundNoiseHistory.addLast(magnitude);
                        soaTimeout = now + soaTimeoutValue;
                    }
                } else {
                    if (!noiseDetected) {
                        backgroundNoiseHistory.removeFirst();
                        backgroundNoiseHistory.addLast(magnitude);
                    }
                }

                executionContext.setVar(
                        AUDIO_MOTION_DETECTED,
                        noiseDetected
                );
                return null;
            });
//                final ShortBuffer sampleBuffer = (ShortBuffer) frame.samples[0];
//                sampleBuffer.rewind();
//                short[] sa = new short[sampleBuffer.limit()];
//                sampleBuffer.get(sa);
//                soundBuffer.put(sa);
//                if (soundBuffer.position() >= 2 * 1024) {
//                    soundBuffer.rewind();
//                }
        } catch (Exception e) {
            onProcessingException(e);
        }

        return true;
    }

    @Override
    public Set<Class<? extends AbstractEvent>> produceEvents() {
        Set<Class<? extends AbstractEvent>> events = new HashSet<>(super.produceEvents());
        events.add(SoundEvent.class);
        return events;
    }

    @Override
    public Set<Class<? extends AbstractTask<?>>> shouldAddBefore() {
        return Set.of(MotionDetectionTask.class, MotionVideoRecordingTask.class);
    }
}
