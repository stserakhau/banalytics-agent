package com.banalytics.box.service.linux;

import com.banalytics.box.module.BoxEngine;
import com.banalytics.box.module.model.discovery.VideoDevice;
import com.banalytics.box.module.model.discovery.VideoProperties.ResolutionFpsCase;
import com.banalytics.box.service.AbstractLocalMediaDeviceDiscovery;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@RequiredArgsConstructor
@Profile({"linux"})
@Service
public class LocalMediaDeviceDiscoveryService extends AbstractLocalMediaDeviceDiscovery {
    final BoxEngine engine;

    private long expirationTimeout;

    public synchronized void scanLocalDevices() {
        try {
            Map<String, VideoDevice> foundVideoDevices = videoDevices();
            Set<String> oldVideoDevices = new HashSet<>(this.videoDevices.keySet());
            for (Map.Entry<String, VideoDevice> entry : foundVideoDevices.entrySet()) {
                String key = entry.getKey();
                oldVideoDevices.remove(key);
                if (this.videoDevices.containsKey(key)) {
                    continue;
                }
                VideoDevice val = entry.getValue();
                this.videoDevices.put(key, val);
            }
            oldVideoDevices.forEach(foundVideoDevices::remove);//clen devices which are not found

            for (Map.Entry<String, VideoDevice> entry : videoDevices.entrySet()) {
                if (!entry.getValue().videoProperties.resolutionFpsCases.isEmpty()) {
                    continue;
                }
                fillResolutionFps(entry.getValue());
            }
        } catch (Exception e) {
            log.info(e.getMessage());
        }
    }

    // v4l2-ctl --list-devices
    private Map<String, VideoDevice> videoDevices() throws Exception {
        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command("v4l2-ctl", "--list-devices");
        Process process = processBuilder.start();
        process.waitFor();

        List<String> lines = IOUtils.readLines(process.getInputStream(), "UTF-8");
        Map<String, VideoDevice> result = new HashMap<>();

        String deviceName = null;
        for (String line : lines) {
            if (line.startsWith("\t")) {
                if (deviceName != null) {
                    VideoDevice vd = new VideoDevice();
                    String name = deviceName.trim();
                    vd.name = name;
                    vd.alternativeName = line.trim();
                    result.put(name, vd);
                    deviceName = null;
                } else {
                    //skip line
                }
            } else {
                deviceName = line;
            }
        }

        return result;
    }

    // v4l2-ctl --list-formats-ext -d /dev/video0
    private static void fillResolutionFps(VideoDevice videoDevice) throws Exception {
        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command("v4l2-ctl", "--list-formats-ext", "-d", videoDevice.alternativeName);
        Process process = processBuilder.start();
        process.waitFor();

        List<String> lines = IOUtils.readLines(process.getInputStream(), "UTF-8");

        String currentPixFmt = null;
        ResolutionFpsCase currentCase = null;
        for (String line : lines) {
            line = line.trim();
            String[] args = line.split(" ");
            if (line.contains("Size:")) {
                String resolution = args[2];
                currentCase = new ResolutionFpsCase();
                String[] parts = resolution.split("x");
                currentCase.setWidth(Integer.parseInt(parts[0]));
                currentCase.setHeight(Integer.parseInt(parts[1]));
                videoDevice.videoProperties.addPixelFormatResFpsCase(currentCase);
            } else if (line.contains("Interval:")) {
                String rawFps = args[3];
                String sFps = rawFps.substring(1);
                double fps = Double.parseDouble(sFps);
                currentCase.setMinFps(Math.min(currentCase.getMinFps(), fps));
                currentCase.setMaxFps(Math.max(currentCase.getMaxFps(), fps));
            }
        }
    }
}
