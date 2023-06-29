package com.banalytics.box.service.windows;

import com.banalytics.box.module.model.discovery.VideoProperties;
import lombok.extern.slf4j.Slf4j;

import java.util.Comparator;
import java.util.List;

@Slf4j
public class VideoDevicePropertyAccumulator {
    public VideoProperties videoProperties = new VideoProperties();

    public void process(List<String> lines) {
        for (String line : lines) {
            if (!line.contains("[dshow")) {
                continue;
            }
            log.info(line);
            if (line.contains("fps")) {
                String[] parts = line.substring(line.indexOf("]") + 1).trim().split(" ");
                VideoProperties.ResolutionFpsCase configCase = new VideoProperties.ResolutionFpsCase();
                boolean isMaxPart = false;
//                String currentPixelFormat = null;
                double minFps = 5.0;
                double maxFps = 15.0;
                boolean isPixelFormat = false;
                for (String part : parts) {
                    if (part.startsWith("pixel_format=")) {
//                        currentPixelFormat = part.substring(13);
                        isPixelFormat = true;
                    } else if (part.startsWith("vcodec=")) {
//                        String codec = part.substring(7);
//                        currentPixelFormat = codec;
                        isPixelFormat = false;
                    } else if (part.startsWith("s=")) {
                        String resolution = part.substring(2);
                        String[] resParts = resolution.split("x");
                        configCase.setWidth(Integer.parseInt(resParts[0]));
                        configCase.setHeight(Integer.parseInt(resParts[1]));
                    } else if (part.startsWith("fps=")) {
                        String fps = part.substring(4);
                        if (isMaxPart) {
                            maxFps = Double.parseDouble(fps);
                        } else {
                            minFps = Double.parseDouble(fps);
                        }
                    } else if (part.equals("max")) {
                        isMaxPart = true;
                    }
                }
                if(isPixelFormat) {
                    configCase.setMinRecommendedFps(minFps);
                    configCase.setMaxRecommendedFps(maxFps);
                } else {
                    configCase.setMinFps(minFps);
                    configCase.setMaxFps(maxFps);
                }
                videoProperties.addPixelFormatResFpsCase(configCase);
            }
        }
    }
}