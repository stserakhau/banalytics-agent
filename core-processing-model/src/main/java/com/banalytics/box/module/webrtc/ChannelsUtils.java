package com.banalytics.box.module.webrtc;

import com.banalytics.box.module.utils.ObjectPool;
import com.banalytics.box.service.utility.TrafficControl;
import dev.onvoid.webrtc.RTCDataChannel;
import dev.onvoid.webrtc.RTCDataChannelBuffer;
import dev.onvoid.webrtc.RTCDataChannelObserver;
import dev.onvoid.webrtc.RTCDataChannelState;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.map.PassiveExpiringMap;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;

@Slf4j
public class ChannelsUtils {
    public final static int MESSAGE_BUFFER_SIZE = 16300 + 8 + 4; //+ 8 transmissionId +4 for int batchIndex

    /**
     * Method sends head of stream transmission
     * Message: "stream-start;{mimeType};{img_width}x{img_height};{file len in bytes}"
     * Example: "stream-start;video/mp4;1024x768;1024567"
     */
    public static void sendRTStreamStart(RTCDataChannel channel, int streamId, int width, int height) throws Exception {
        String message = "stream-start;" + streamId + ";video/h264;" + width + "x" + height;
        sendStreamCommand(channel, message);
    }

    /**
     * Method sends the end of the file transmission
     * Message: "stream-end"
     */
    @Deprecated
    public static void sendStreamEnd(RTCDataChannel channel) throws Exception {
        String message = "stream-end";
        sendStreamCommand(channel, message);
    }


    public static void sendStreamCommand(RTCDataChannel channel, String command) throws Exception {
        if (channel.getState() != RTCDataChannelState.OPEN) {
            throw new Exception("Channel closed");
        }
        byte[] data = command.getBytes(StandardCharsets.UTF_8);
        TrafficControl.INSTANCE.acquireGeneralResource(data.length, false);
        RTCDataChannelBuffer streamFinish = new RTCDataChannelBuffer(
                ByteBuffer.wrap(data), false
        );
        send(channel, streamFinish);
//        log.info("Stream command sent: {}", command);
    }

    /**
     * Method sends the body of the file transmission
     */
    public static void sendStreamBatch(RTCDataChannel channel, byte[] batch, long transmissionId, int batchIndex) throws Exception {
        sendStreamBatch(channel, batch, batch.length, transmissionId, batchIndex);
    }

    private static final ObjectPool<byte[]> BUFFERS_POOL = new ObjectPool<>(10, () -> new byte[MESSAGE_BUFFER_SIZE]);

    private static final Map<Integer, ObjectPool<byte[]>> BUFFER_APPENDIX_POOLS = Collections.synchronizedMap(new PassiveExpiringMap<>(30000));

    public static void sendStreamBatch(RTCDataChannel channel, byte[] batch, int dataLength, long transmissionId, int batchIndex) throws Exception {
        if (channel.getState() != RTCDataChannelState.OPEN) {
            throw new Exception("Channel closed");
        }
        { // streaming data case
            int pos = 0;
            final byte[] buffer = BUFFERS_POOL.acquireObject();
            try {
                final int dataOffset = 8 + 4;//+ 8 transmissionId +4 for int batchIndex
                int available;
                while ((available = dataLength - pos) > 0) {
                    ObjectPool<byte[]> appendixBufferPool = null;
                    byte[] appendixBuffer = null;
                    ByteBuffer byteBuffer;
                    if (available < MESSAGE_BUFFER_SIZE) {
                        int appendixBufferSize = 8 + 4 + available; //+ 8 transmissionId +4 for int batchIndex
                        appendixBufferPool = BUFFER_APPENDIX_POOLS.computeIfAbsent(
                                appendixBufferSize,
                                size -> new ObjectPool<>(5, () -> new byte[appendixBufferSize])
                        );

                        appendixBuffer = appendixBufferPool.acquireObject();
                        System.arraycopy(batch, pos, appendixBuffer, 8 + 4, available); //+ 8 transmissionId +4 for int batchIndex
                        byteBuffer = ByteBuffer.wrap(appendixBuffer);
                        byteBuffer.putLong(0, transmissionId);
                        byteBuffer.putInt(8, batchIndex);
                    } else {
                        System.arraycopy(batch, pos, buffer, dataOffset, MESSAGE_BUFFER_SIZE - dataOffset);
                        byteBuffer = ByteBuffer.wrap(buffer);
                        byteBuffer.putLong(0, transmissionId);
                        byteBuffer.putInt(8, batchIndex);
                    }

                    try {
                        RTCDataChannelBuffer channelBuffer = new RTCDataChannelBuffer(byteBuffer, true);
                        send(channel, channelBuffer);
                        pos += MESSAGE_BUFFER_SIZE - dataOffset;
                    } finally {
                        if (appendixBufferPool != null) {
                            appendixBufferPool.returnObject(appendixBuffer);
                        }
                    }
                }
            } finally {
                BUFFERS_POOL.returnObject(buffer);
            }
        }
    }

    public static void send(RTCDataChannel channel, RTCDataChannelBuffer channelBuffer) throws Exception {
        synchronized (channel) {
            channel.send(channelBuffer);
        }
    }

    public static void waitChannelOpen(RTCDataChannel channel, RTCDataChannelObserver observer) {
        channel.registerObserver(observer);

        if (observer instanceof ChannelWaitObserver channelWaitObserver) {
            channelWaitObserver.setTargetChannel(channel);
            String channelName = channel.getLabel();
            log.info("Channel created: {}", channel.getLabel());
            int tries = 1;
            while (channel.getState() == RTCDataChannelState.CONNECTING) {// wait up to 60 seconds
                log.info("File channel '{}' connecting... ({})", channelName, tries);
                tries++;
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                if (tries > 10) {
                    channelWaitObserver.reject();
                    throw new RuntimeException("network.error.overloaded");
                }
                switch (channel.getState()) {
                    case CLOSING, CLOSED -> {
                        channelWaitObserver.reject();
                        throw new RuntimeException("network.error.overloaded");
                    }
                }
            }
            if (channel.getState() == RTCDataChannelState.OPEN) {
                observer.onStateChange();
            }
        }
    }


    public static void sendMessage(long requestId, RTCDataChannel channel, byte[] data) throws Exception {
        TrafficControl.INSTANCE.acquireGeneralResource(data.length, false);
        sendStreamBatch(channel, data, data.length, requestId, data.length);
//        channel.send(new RTCDataChannelBuffer(ByteBuffer.wrap(
//                data
//        ), true));
    }

    public interface ChannelWaitObserver {
        void setTargetChannel(RTCDataChannel channel);

        void reject();
    }
}
