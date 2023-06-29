package com.banalytics.box.module.webrtc.client.channel.observer;

import com.banalytics.box.module.BoxEngine;
import com.banalytics.box.module.MediaCaptureCallbackSupport;
import com.banalytics.box.module.MediaConsumer;
import com.banalytics.box.module.RealTimeOutputStream;
import com.banalytics.box.module.webrtc.ChannelsUtils;
import com.banalytics.box.module.webrtc.PortalWebRTCIntegrationConfiguration;
import com.banalytics.box.module.webrtc.PortalWebRTCIntegrationThing;
import com.banalytics.box.module.webrtc.client.RTCClient;
import dev.onvoid.webrtc.RTCDataChannel;
import dev.onvoid.webrtc.RTCDataChannelBuffer;
import dev.onvoid.webrtc.RTCDataChannelObserver;
import dev.onvoid.webrtc.RTCDataChannelState;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@RequiredArgsConstructor
public class MediaChannelObserver implements RTCDataChannelObserver, MediaConsumer {
    private final RTCClient rtcClient;
    public final BoxEngine engine;
    private final RTCDataChannel mediaChannel;

    private final Map<Integer, StreamDescriptor> streamsDescriptors = new ConcurrentHashMap<>();

    @RequiredArgsConstructor
    public static class StreamDescriptor {
        final UUID taskUuid;
        final int streamId;
        final int requestedWidth;
        final int requestedHeight;
        final boolean requestedAudio;
        final MediaCaptureCallbackSupport mediaCaptureCallbackSupport;
        boolean streamStarted;
    }

    public void requestStream(StreamDescriptor streamDescriptor) {
        log.warn("REQUEST STREAM");
        int streamId = streamDescriptor.streamId;
        streamsDescriptors.put(streamId, streamDescriptor);

        PortalWebRTCIntegrationThing webRtcThing = engine.getThing(PortalWebRTCIntegrationConfiguration.WEB_RTC_UUID);

        log.info("Media channel(s) opening: " + streamId);

        this.realTimeVideoOutputStream = streamDescriptor.mediaCaptureCallbackSupport.createRealTimeVideoStream(streamId, this);

        if (this.realTimeVideoOutputStream != null) {
            this.realTimeVideoOutputStream.setRequestedImageSize(streamDescriptor.requestedWidth, streamDescriptor.requestedHeight);
            log.info("Video channel opened: " + streamId);
        }
        if (streamDescriptor.requestedAudio) {
            this.realTimeAudioOutputStream = streamDescriptor.mediaCaptureCallbackSupport.createRealTimeAudioStream(streamId, this);
            if (this.realTimeAudioOutputStream != null) {
                log.info("Audio channel opened: " + streamId);
            }
        }
    }

    @Override
    public void onBufferedAmountChange(long previousAmount) {

    }

    @Override
    public void onStateChange() {
        log.debug("Media channel state changed to: {}", mediaChannel.getState());
    }

    private RealTimeOutputStream realTimeVideoOutputStream;
    private RealTimeOutputStream realTimeAudioOutputStream;

    private void stopStream(int streamId) {
        StreamDescriptor sd = streamsDescriptors.get(streamId);
        if (sd == null) {
            return;
        }
        log.info("Stopping stream: {}", sd.streamId);
        try {
            sd.mediaCaptureCallbackSupport.releaseRealTimeVideoStream(sd.streamId, this);
            log.info("Stream stopped: {}", sd.streamId);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        if (sd.requestedAudio) {
            try {
                sd.mediaCaptureCallbackSupport.releaseRealTimeAudioStream(sd.streamId, this);
                log.info("Audio stream {} stopped", sd.streamId);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
    }

    @Override
    public void accept(MediaData mediaData) {
//        log.warn("ACCEPT DATA");
        if (mediaChannel.getState() != RTCDataChannelState.OPEN) {
            log.error("Media channel closed but data sending. stop stream {}", mediaData.streamId);
            stopStream(mediaData.streamId);
            return;
        }
        try {
            StreamDescriptor sd = streamsDescriptors.get(mediaData.streamId);
            if(sd==null) {
                log.error("Stream was closed: {}", mediaData.streamId);
            }
            if (!sd.streamStarted) {
                ChannelsUtils.sendRTStreamStart(mediaChannel, mediaData.streamId, realTimeVideoOutputStream.imageWidth, realTimeVideoOutputStream.imageHeight);
                sd.streamStarted = true;
            } else {
                if (realTimeVideoOutputStream != null && realTimeVideoOutputStream.fpsChanged) {
                    ChannelsUtils.sendStreamCommand(mediaChannel, "fps=" + realTimeVideoOutputStream.fps + ";" + mediaData.streamId);
                }
            }
            ByteBuffer buffer = ByteBuffer.allocate(4 + 1 + mediaData.data.length);// streamId + (0 is video, 1 is audio) byte + data length)
            buffer.putInt(mediaData.streamId);
            if (mediaData.mediaType == MediaData.MediaType.VIDEO) {
                buffer.put((byte) 0);
            } else {
                buffer.put((byte) 1);
            }
            buffer.put(mediaData.data);
            RTCDataChannelBuffer channelBuffer = new RTCDataChannelBuffer(buffer, true);
            ChannelsUtils.send(mediaChannel, channelBuffer);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    @Override
    public void onMessage(RTCDataChannelBuffer buffer) {
        if (buffer.binary) {
            log.warn("Channel not support binary messages");
            return;
        }
        try {
            String consumerCommand = Charset.defaultCharset().decode(buffer.data).toString();
            log.debug("Consumer command: {}", consumerCommand);
            String[] parts = consumerCommand.split(":");
            int streamId = Integer.parseInt(parts[0]);

            StreamDescriptor sd = streamsDescriptors.get(streamId);

            RealTimeOutputStream realTimeVideoOutputStream = sd.mediaCaptureCallbackSupport.getRealTimeVideoStream(streamId, this);
            //todo no control commands for audio. motentially mute only
            //            RealTimeOutputStream realTimeAudioOutputStream = mediaCaptureCallbackSupport.getRealTimeAudioStream(this);

            String command = parts[1];
            switch (command) {
                case "VIDEO-SIZE" -> {
                    log.warn("VIDEO SIZE");
                    int width = Integer.parseInt(parts[1]);
                    int height = Integer.parseInt(parts[2]);
                    realTimeVideoOutputStream.setRequestedImageSize(width, height);
                }
                case "STOP" -> {
                    log.warn("STOP");
                    stopStream(streamId);
                }
                default -> log.warn("Message received. But channel doesn't support processing:\n{}", consumerCommand);
            }
        } catch (Throwable t) {
            log.error(t.getMessage(), t);
        }
    }
}