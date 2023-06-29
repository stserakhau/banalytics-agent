package com.banalytics.box.module;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.server.MimeMappings;

import java.io.File;
import java.util.Map;
import java.util.Set;

public interface Thing<CONFIGURATION extends IConfiguration> extends StateSupport, InitShutdownSupport {
    String PERMISSION_CREATE = "create*";
    String PERMISSION_READ = "read*";
    String PERMISSION_UPDATE = "update*";
    String PERMISSION_DELETE = "delete*";
    String PERMISSION_VIDEO = "video";
    String PERMISSION_AUDIO = "audio";
    String PERMISSION_ACTION = "action";
    String PERMISSION_START_STOP = "start-stop";

    static Thing<?> blankOf(Class<? extends Thing<?>> thingClass, BoxEngine metricDeliveryService) throws Exception {
        return thingClass.getDeclaredConstructor(new Class[]{BoxEngine.class}).newInstance(metricDeliveryService);
    }

    String getTitle();

    String getSelfClassName();


    CONFIGURATION getConfiguration();

    void configuration(CONFIGURATION configuration);

    default Object uniqueness() {
        return null;
    }

    Set<InitShutdownSupport> getSubscribers();

    default void destroy() {
    }

    default void onSave() throws Exception {
    }

    default boolean billable() {
        return false;
    }

    void subscribe(InitShutdownSupport initShutdownSupport);

    void unSubscribe(InitShutdownSupport initShutdownSupport);

    default Map<String, ?> options() {
        return Map.of();
    }

    default Set<String> generalPermissions() {
        return Set.of(PERMISSION_READ, PERMISSION_UPDATE, PERMISSION_ACTION, PERMISSION_START_STOP);
    }

    default Set<String> apiMethodsPermissions() {
        return Set.of(PERMISSION_CREATE, PERMISSION_READ, PERMISSION_UPDATE, PERMISSION_DELETE);
    }

    default Object call(Map<String, Object> params) throws Exception {
        throw new RuntimeException("Api not supported");
    }

    @RequiredArgsConstructor
    @Getter
    class DownloadStream {
        private static final MimeMappings customMapping = new MimeMappings(MimeMappings.DEFAULT);
        private static final String DEFAULT_MIME_TYPE;

        static {
            customMapping.add("xml", "text/xml");
            customMapping.add("log", "text/plain");
            customMapping.add("bat", "text/plain");
            DEFAULT_MIME_TYPE = MimeMappings.DEFAULT.get("bin");
        }
        final long cacheTtlMillis;
        final String mimeType;
        final File file;

        public static String mimeTypeOf(String fileName) {
            int extDotIndex = fileName.lastIndexOf('.');
            String extension = extDotIndex > 0 ? fileName.substring(extDotIndex + 1) : null;
            if (extension == null) {
                return DEFAULT_MIME_TYPE;
            }
            String mimeType = customMapping.get(extension);
            return mimeType != null ? mimeType : DEFAULT_MIME_TYPE;
        }
    }

    public interface StarUpOrder {
        public int CORE = 0;
        public int DATA_EXCHANGE = 1000;
        public int INTEGRATION = 10000;
        public int BUSINESS = 10000;
        public int BUSINESS_LONG_START = 50000;
    }
}
