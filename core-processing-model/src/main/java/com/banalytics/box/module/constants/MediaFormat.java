package com.banalytics.box.module.constants;

import java.util.Map;

/**
 * https://developer.mozilla.org/ru/docs/Web/HTTP/Basics_of_HTTP/MIME_types
 */
public enum MediaFormat {
    rtsp("video/h264", Map.of(
            "timeout", "1000000",
            "stimeout", "5000000",
            "stream_loop", "-1",
            "r", "5",
            "reconnect", "1",
            "reconnect_at_eof", "1",
            "reconnect_streamed", "1",
            "reconnect_delay_max", "2"
    )),
    mjpeg("video/h264",
            Map.of(
                    "tune", "zerolatency",
                    "preset", "ultrafast",
                    "timeout", "1000000",
                    "stimeout", "5000000",
                    "stream_loop", "-1",
                    "r", "5",
                    "reconnect", "1",
                    "reconnect_at_eof", "1",
                    "reconnect_streamed", "1",
                    "reconnect_delay_max", "2"
            )
    ),
    h264("video/h264", Map.of(
            "tune", "zerolatency",
            "preset", "ultrafast",
            "timeout", "1000000",
            "stimeout", "5000000",
            "stream_loop", "-1",
            "r", "5",
            "reconnect", "1",
            "reconnect_at_eof", "1",
            "reconnect_streamed", "1",
            "reconnect_delay_max", "2"
    )),
    hevc("video/hevc", Map.of(
            "tune", "zerolatency",
            "preset", "ultrafast",
            "timeout", "1000000",
            "stimeout", "5000000",
            "stream_loop", "-1",
            "r", "5",
            "reconnect", "1",
            "reconnect_at_eof", "1",
            "reconnect_streamed", "1",
            "reconnect_delay_max", "2"
    )),
    mp4("video/mp4", Map.of()),
    webm("video/webm", Map.of());

    public final String mimeType;
    public final Map<String, String> grabberOptions;

    MediaFormat(String mimeType, Map<String, String> grabberOptions) {
        this.mimeType = mimeType;
        this.grabberOptions = grabberOptions;
    }
}
