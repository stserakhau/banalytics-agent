package com.banalytics.box.module;

import java.io.File;
import java.util.UUID;
import java.util.function.Consumer;

public interface MediaCaptureCallbackSupport {
    default Thing<?> getSourceThing() {
        return null;
    }

    default void screenShot(Consumer<MediaResult> mediaResultConsumer) {
        throw new RuntimeException("Method not supports");
    }

    default RealTimeOutputStream getRealTimeVideoStream(int streamId, MediaConsumer consumer) {
        throw new RuntimeException("Method not supports");
    }

    default RealTimeOutputStream createRealTimeVideoStream(int streamId, MediaConsumer consumer){
        throw new RuntimeException("Method not supports");
    }

    default void releaseRealTimeVideoStream(int streamId, MediaConsumer consumer) throws Exception {
        throw new RuntimeException("Method not supports");
    }

    default RealTimeOutputStream getRealTimeAudioStream(int streamId, MediaConsumer consumer) {
        throw new RuntimeException("Method not supports");
    }

    default RealTimeOutputStream createRealTimeAudioStream(int streamId, MediaConsumer consumer){
        throw new RuntimeException("Method not supports");
    }

    default void releaseRealTimeAudioStream(int streamId, MediaConsumer consumer) throws Exception {
        throw new RuntimeException("Method not supports");
    }

    public static class MediaResult {
        public final UUID sourceUuid;
        public final MediaType mediaType;
        public final byte[] data;
        public final File file;
        public int width;
        public int height;
        public int duration;

        public MediaResult(UUID sourceUuid, MediaType mediaType, byte[] data) {
            this.sourceUuid = sourceUuid;
            this.mediaType = mediaType;
            this.data = data;
            this.file = null;
        }

        public MediaResult(UUID sourceUuid, MediaType mediaType, File file) {
            this.sourceUuid = sourceUuid;
            this.mediaType = mediaType;
            this.data = null;
            this.file = file;
        }

        public enum MediaType {
            audio,
            image,
            video,
        }
    }

    record Data(byte[] data, int length, int width, int height) {
    }
}
