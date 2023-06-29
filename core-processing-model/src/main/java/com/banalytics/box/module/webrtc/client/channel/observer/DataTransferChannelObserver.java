package com.banalytics.box.module.webrtc.client.channel.observer;

import com.banalytics.box.module.BoxEngine;
import com.banalytics.box.module.standard.FileStorage;
import com.banalytics.box.module.utils.ObjectPool;
import com.banalytics.box.module.webrtc.ChannelsUtils;
import com.banalytics.box.module.webrtc.client.RTCClient;
import com.banalytics.box.service.utility.TrafficControl;
import dev.onvoid.webrtc.RTCDataChannel;
import dev.onvoid.webrtc.RTCDataChannelBuffer;
import dev.onvoid.webrtc.RTCDataChannelObserver;
import dev.onvoid.webrtc.RTCDataChannelState;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static com.banalytics.box.service.SystemThreadsService.getExecutorService;

@Slf4j
@RequiredArgsConstructor
public class DataTransferChannelObserver implements RTCDataChannelObserver {
    private final RTCClient rtcClient;
    public final BoxEngine engine;
    private final RTCDataChannel dataTransferChannel;

    public final Map<Long, DataTransferChannelObserver.TransmissionInfo> downloadTransmissionsMap = new ConcurrentHashMap<>();

    @Override
    public void onBufferedAmountChange(long previousAmount) {
        log.info("Events channel onBufferedAmountChange({})", previousAmount);
    }

    @Override
    public void onStateChange() {
        if (dataTransferChannel.getState() == RTCDataChannelState.OPEN) {
            log.info("Data transfer channel opened: {}", rtcClient.transactionId);
            rtcClient.lastInteractionTime = System.currentTimeMillis();
        } else if (dataTransferChannel.getState() == RTCDataChannelState.CLOSED) {
            log.info("Data transfer closed: {}", rtcClient.transactionId);
        } else {
            log.info("Data transfer state changed from {} to {}", dataTransferChannel.getState(), rtcClient.transactionId);
        }
    }

    private final int transmissionBatchSize = ChannelsUtils.MESSAGE_BUFFER_SIZE - 8 - 4;// -8 -4 transmissionId & batchIndex metadata

    private final ObjectPool<byte[]> buffersPool = new ObjectPool<>(
            2, // 2 parallel transmissions
            () -> new byte[transmissionBatchSize]
    );

    final Map<Integer, FileOutputStream> uploadTransmissionIdFile = new HashMap<>();

    @Override
    public void onMessage(RTCDataChannelBuffer messageBuffer) {
        if (!rtcClient.authenticated()) {
            return;
        }
        rtcClient.lastInteractionTime = System.currentTimeMillis();
        try {
            if (messageBuffer.binary) {
                ByteBuffer data = messageBuffer.data;
                int transmissionId = data.getInt(0);
                FileOutputStream fos = uploadTransmissionIdFile.get(transmissionId);
                if (fos == null) {
                    return;
                }
                byte[] array = new byte[data.remaining()];
                data.get(array);
                fos.write(array, 4, array.length - 4);
            } else {

                String clientMessage = Charset.defaultCharset()
                        .decode(messageBuffer.data)
                        .toString();
                String[] parts = clientMessage.split(":");
                String message = parts[0];
                switch (message) {
                    case "CLIENT_UPLOAD_STARTED" -> {
                        int transmissionId = Integer.parseInt(parts[1]);
                        UUID targetThingUuid = UUID.fromString(parts[2]);
                        String contextPath = parts[3];
                        FileStorage fs = engine.getThing(targetThingUuid);
                        File file = fs.startOutputTransaction(contextPath);
                        uploadTransmissionIdFile.put(transmissionId, new FileOutputStream(file));
                    }
                    case "CLIENT_UPLOAD_CANCEL" -> {
                        int transmissionId = Integer.parseInt(parts[1]);
                        UUID targetThingUuid = UUID.fromString(parts[2]);
                        String contextPath = parts[3];
                        FileOutputStream fos = uploadTransmissionIdFile.get(transmissionId);
                        fos.flush();
                        fos.close();
                        FileStorage fs = engine.getThing(targetThingUuid);
                        fs.rollbackOutputTransaction(contextPath, stringFilePair -> {
                            uploadTransmissionIdFile.remove(transmissionId);
                        });
                    }
                    case "CLIENT_UPLOAD_DONE" -> {
                        int transmissionId = Integer.parseInt(parts[1]);
                        UUID targetThingUuid = UUID.fromString(parts[2]);
                        String contextPath = parts[3];
                        FileOutputStream fos = uploadTransmissionIdFile.get(transmissionId);
                        fos.flush();
                        fos.close();
                        FileStorage fs = engine.getThing(targetThingUuid);
                        fs.commitOutputTransaction(contextPath, stringFilePair -> {
                            uploadTransmissionIdFile.remove(transmissionId);
                        });
                    }

                    case "CLIENT_DOWNLOAD_STARTED" -> {
                        long transmissionId = Long.parseLong(parts[1]);
                        TransmissionInfo transmissionInfo = downloadTransmissionsMap.get(transmissionId);
                        int batchCount = (int) Math.ceil(transmissionInfo.contentSize / (double) transmissionBatchSize);
                        log.info("Client ready to receive file: {}. Batch count: {}", transmissionInfo.file.getName(), batchCount);
                        for (int i = 0; i < batchCount; i++) {
                            transmissionInfo.batchesToSend.add(i);
                        }
                        getExecutorService(this).submit(() -> {// start sender thread
                            try {
                                _send(transmissionId);
                            } catch (Exception e) {
                                log.error(e.getMessage(), e);
                            } finally {
                                downloadTransmissionsMap.remove(transmissionId);
                                System.gc();
                            }
                        });
                    }

                    case "CLIENT_DOWNLOAD_CANCEL" -> {
                        long transmissionId = Long.parseLong(parts[1]);
                        downloadTransmissionsMap.remove(transmissionId);
                        log.info("Download cancelled: {}", transmissionId);
                    }
                }
            }
        } catch (Throwable t) {
            log.error(t.getMessage(), t);
        }
    }

    private void _send(Long transmissionId) throws Exception {
        TransmissionInfo transmissionInfo = downloadTransmissionsMap.get(transmissionId);
        List<Integer> b2send = transmissionInfo.batchesToSend;
        final byte[] buffer = buffersPool.acquireObject();
        try {
            while (!b2send.isEmpty()) {// 1st phase - send main data
                if (!downloadTransmissionsMap.containsKey(transmissionId)) {
                    log.info("Transmission cancelled: {}", transmissionId);
                    break;
                }
                int batchIndex = b2send.remove(0);
                byte[] bufferToSend = queueBatchData(transmissionInfo, batchIndex, buffer);
                sendBatch(transmissionInfo, batchIndex, bufferToSend);
            }
        } finally {
            buffersPool.returnObject(buffer);
        }
    }

    private synchronized byte[] queueBatchData(TransmissionInfo transmissionInfo, int batchIndex, byte[] buffer) throws Exception {
        transmissionInfo.randomAccessFile.position((long) batchIndex * transmissionBatchSize);
        ByteBuffer bb = ByteBuffer.wrap(buffer);
        int read = transmissionInfo.randomAccessFile.read(bb);
        if (read == -1) {
            throw new Exception("End of stream.");
        }
        if (read != transmissionBatchSize) {
            byte[] truncatedBuffer = new byte[read];
            System.arraycopy(buffer, 0, truncatedBuffer, 0, read);
            buffer = truncatedBuffer;
        }
        return buffer;
    }

    private void sendBatch(TransmissionInfo transmissionInfo, int batchIndex, byte[] data) throws Exception {
//                log.info("Send {} / {}", transmissionInfo.transmissionId, batchIndex);
        try {
            long transmissionId = transmissionInfo.transmissionId;
            TrafficControl.INSTANCE.acquireFileTransmissionResource(data.length);
            ChannelsUtils.sendStreamBatch(dataTransferChannel, data, transmissionId, batchIndex);
        } catch (Exception e) {
            throw new Exception("Transmission cancelled " + e.getMessage() + ": " + transmissionInfo.transmissionId);
        }
    }

    public record TransmissionInfo(long transmissionId, File file, SeekableByteChannel randomAccessFile,
                                   long contentSize, List<Integer> batchesToSend) {
    }
}
