package com.banalytics.box.service.windows;

import com.banalytics.box.module.model.discovery.AudioDevice;
import com.banalytics.box.module.model.discovery.VideoDevice;
import com.banalytics.box.service.AbstractLocalMediaDeviceDiscovery;
import lombok.extern.slf4j.Slf4j;
import org.bytedeco.ffmpeg.avformat.AVFormatContext;
import org.bytedeco.ffmpeg.avformat.AVInputFormat;
import org.bytedeco.ffmpeg.avutil.AVDictionary;
import org.bytedeco.ffmpeg.avutil.LogCallback;
import org.bytedeco.ffmpeg.global.avformat;
import org.bytedeco.ffmpeg.global.avutil;
import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.javacv.FFmpegLogCallback;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.*;

import static org.bytedeco.ffmpeg.global.avdevice.avdevice_register_all;
import static org.bytedeco.ffmpeg.global.avformat.*;
import static org.bytedeco.ffmpeg.global.avutil.av_dict_set;

@Slf4j
@Profile({"windows"})
@Service
public class LocalMediaDeviceDiscoveryService extends AbstractLocalMediaDeviceDiscovery implements InitializingBean {

    @Override
    public void afterPropertiesSet() throws Exception {
        try {
            avdevice_register_all();
            avutil.av_log_set_level(avutil.AV_LOG_FATAL);
        } catch (Throwable e) {
            log.error(e.getMessage(), e);
        }
    }

    private volatile long expirationTimeout;

    public synchronized  void scanLocalDevices() {
        log.info("============Scan started");
        synchronized (org.bytedeco.ffmpeg.global.avcodec.class) {
            long now = System.currentTimeMillis();
            if (now < expirationTimeout) {
                return;
            }
            expirationTimeout = now + 30000;
            List<String> result = Collections.synchronizedList(new ArrayList<>());
            avutil.av_log_set_level(avutil.AV_LOG_INFO);
            avutil.setLogCallback(new LogCallback() {
                StringBuilder line = new StringBuilder();

                @Override
                public void call(int i, BytePointer bytePointer) {
                    try {
                        String message = bytePointer.getString("UTF-8");
                        if (message.isEmpty()) {
                            return;
                        }
                        line.append(message);
                        boolean newLine = message.indexOf('\n') > -1;
                        if (newLine) {
                            result.add(line.toString().trim());
                            line = new StringBuilder();
                        }
                    } catch (Throwable e) {
                        log.error(e.getMessage(), e);
                    }
                }
            });

            DeviceInfoAccumulator deviceInfoAccumulator = new DeviceInfoAccumulator();
            try {
                result.clear();
                execute("dshow", "list_devices", "dummy");
                deviceInfoAccumulator.process(result);

                Set<String> removedAudioDevices = new HashSet<>(audioDevices.keySet());
                for (Map.Entry<String, AudioDevice> entry : deviceInfoAccumulator.audioDevices.entrySet()) {
                    String key = entry.getKey();
                    removedAudioDevices.remove(key);
                    AudioDevice value = entry.getValue();
                    if (audioDevices.containsKey(key)) {
                        continue;
                    }
                    audioDevices.put(key, value);
                }
                removedAudioDevices.forEach(key -> audioDevices.remove(key));

                Set<String> removedVideoDevices = new HashSet<>(videoDevices.keySet());
                for (Map.Entry<String, VideoDevice> entry : deviceInfoAccumulator.videoDevices.entrySet()) {
                    String key = entry.getKey();
                    removedVideoDevices.remove(key);
                    VideoDevice val = entry.getValue();
                    if (videoDevices.containsKey(key)) {
                        continue;
                    }
                    videoDevices.put(key, val);
                }
                removedVideoDevices.forEach(key -> videoDevices.remove(key));

            } catch (Throwable e) {
                log.info(e.getMessage());
            }
            for (VideoDevice videoDevice : videoDevices.values()) {
                if (!videoDevice.videoProperties.resolutionFpsCases.isEmpty()) {
                    continue;
                }
                VideoDevicePropertyAccumulator vda = new VideoDevicePropertyAccumulator();
                try {
                    result.clear();
                    execute("dshow", "list_options", "video=" + videoDevice.alternativeName);
                    vda.process(result);
                } catch (Throwable e) {
                    log.info(e.getMessage());
                } finally {
                    videoDevice.videoProperties = vda.videoProperties;
                }
            }
            for (AudioDevice audioDevice : audioDevices.values()) {
                if (!audioDevice.audioProperties.audioCases.isEmpty()) {
                    continue;
                }
                AudioDevicePropertyAccumulator ada = new AudioDevicePropertyAccumulator();
                try {
                    result.clear();
                    execute("dshow", "list_options", "audio=" + audioDevice.alternativeName);
                    ada.process(result);
                } catch (Throwable e) {
                    log.info(e.getMessage());
                } finally {
                    audioDevice.audioProperties = ada.audioProperties;
                }
            }
            try {
                Thread.sleep(500);
                log.info("============Scan done");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            avutil.av_log_set_level(avutil.AV_LOG_FATAL);
            FFmpegLogCallback.set();
            log.info("============Revert to default logger");
        }
    }

    private static void execute(String format, String option, String device) {
        AVFormatContext context = avformat_alloc_context();
        AVDictionary options = new AVDictionary(null);
        try {
            AVInputFormat inputFormat = avformat.av_find_input_format(format);
            context.iformat(inputFormat);
            int res = av_dict_set(options, option, "true", 0);//list_options or list_formats
            if (res == 0) {
//                System.out.println("=============== " + context + " / " + device + " / " + inputFormat + " / " + options);
                res = avformat_open_input(context, device, inputFormat, options);
                if (!context.isNull()) {
                    avformat_close_input(context);
                }
            }
        } finally {
            avformat_free_context(context);
        }
    }
}
