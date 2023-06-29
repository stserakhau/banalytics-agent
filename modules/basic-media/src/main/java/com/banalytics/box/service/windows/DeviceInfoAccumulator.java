package com.banalytics.box.service.windows;

import com.banalytics.box.module.model.discovery.AudioDevice;
import com.banalytics.box.module.model.discovery.LocalDevice;
import com.banalytics.box.module.model.discovery.VideoDevice;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DeviceInfoAccumulator {
    public Map<String, AudioDevice> audioDevices = new HashMap<>();
    public Map<String, VideoDevice> videoDevices = new HashMap<>();

    LocalDevice currentDevice;

    public void process(List<String> lines) {
        for (String details : lines) {
            //todo windows case
            if (!details.contains("[dshow")) {
                continue;
            }

            if (details.contains("Alternative name")) {
                int camNameStart = details.indexOf("\"");
                int camNameEnd = details.lastIndexOf("\"");
                currentDevice.alternativeName = details.substring(camNameStart + 1, camNameEnd);
            } else {
                int camNameStart = details.indexOf("\"");
                int camNameEnd = details.lastIndexOf("\"");
                if (camNameStart == -1 || camNameEnd == -1) {
                    continue;
                }
                String name = details.substring(camNameStart + 1, camNameEnd);

                int typeStart = details.indexOf("(", camNameEnd + 1);
                int typeEnd = details.lastIndexOf(")");

                String type = details.substring(typeStart + 1, typeEnd);

                currentDevice = "audio".equals(type) ? new AudioDevice() : new VideoDevice();
                currentDevice.name = name;
            }

            if (currentDevice.isCompleted()) {
                if (currentDevice instanceof AudioDevice ad) {
                    audioDevices.put(currentDevice.alternativeName, ad);
                } else if (currentDevice instanceof VideoDevice vd) {
                    videoDevices.put(currentDevice.alternativeName, vd);
                }
            }
        }
    }
}