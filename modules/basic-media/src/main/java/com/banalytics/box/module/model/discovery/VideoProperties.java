package com.banalytics.box.module.model.discovery;

import lombok.Getter;
import lombok.Setter;

import java.util.*;

public class VideoProperties {
    public TreeSet<ResolutionFpsCase> resolutionFpsCases = new TreeSet<>((o1, o2) -> {
        int res1 = o1.width - o2.width;
        int res2 = o1.height - o2.height;
        return res1 != 0 ? res1 : res2;
    });

    public void addPixelFormatResFpsCase(ResolutionFpsCase resolutionFpsCase) {
        for (ResolutionFpsCase fpsCase : resolutionFpsCases) {
            if(fpsCase.width == resolutionFpsCase.width && fpsCase.height == resolutionFpsCase.height) {
                fpsCase.setMinFps(Math.min(fpsCase.getMinFps(), resolutionFpsCase.getMinFps()));
                fpsCase.setMaxFps(Math.max(fpsCase.getMaxFps(), resolutionFpsCase.getMaxFps()));
                fpsCase.setMinRecommendedFps(Math.min(fpsCase.getMinRecommendedFps(), resolutionFpsCase.getMinRecommendedFps()));
                fpsCase.setMaxRecommendedFps(Math.max(fpsCase.getMaxRecommendedFps(), resolutionFpsCase.getMaxRecommendedFps()));
                return;
            }
        }
        resolutionFpsCases.add(resolutionFpsCase);
    }

    @Getter
    @Setter
    public static class ResolutionFpsCase {
        int width;
        int height;
        double minRecommendedFps = 100;
        double maxRecommendedFps = 0;
        double minFps = 100;
        double maxFps = 0;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ResolutionFpsCase that = (ResolutionFpsCase) o;
            return width == that.width && height == that.height;
        }

        @Override
        public int hashCode() {
            return Objects.hash(width, height);
        }
    }
}