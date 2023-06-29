package com.banalytics.box.module.media.utils;

import org.apache.commons.lang3.StringUtils;
import org.bytedeco.opencv.opencv_core.*;

import java.util.*;

import static org.bytedeco.opencv.global.opencv_imgproc.fillPoly;

public class ZonePainter {
    private String rawValue;
    public final Map<String, List<Point>> detectionAreas = new HashMap<>();
    public final List<List<Point>> insensitiveAreas = new ArrayList<>();

    public void clear() {
        detectionAreas.clear();
        insensitiveAreas.clear();
    }

    public boolean hasDetectionAreas() {
        return !detectionAreas.isEmpty();
    }


    public void init(String areasString) {
        this.rawValue = areasString;
        String[] areas = areasString.split(";");
        for (String area : areas) {
            String[] groups = area.split(":");
            AreaType areaType = AreaType.valueOf(groups[0]);
            String name = groups[1];
            String pointsStr = groups[2];
            String[] coords = pointsStr.split(",");
            int rows = coords.length / 2;

            System.out.print(name + ":");
            List<Point> areaPoints = new ArrayList<>(rows);
            for (int i = 0; i < rows; i++) {
                int xIndex = i * 2;
                int yIndex = xIndex + 1;
                int x = Integer.parseInt(coords[xIndex]);
                int y = Integer.parseInt(coords[yIndex]);
                areaPoints.add(new Point(x, y));
            }

            if (areaType == AreaType.insensitive) {
                this.insensitiveAreas.add(areaPoints);
            } else if (areaType == AreaType.trigger) {
                this.detectionAreas.put(name, areaPoints);
            }
        }
    }

    public boolean checkObjectInZones(int x, int y, int width, int height, Set<String> regionsAccumulator) {
        if (detectionAreas.isEmpty()) {
            return true;
        }

        for (Map.Entry<String, List<Point>> entry : detectionAreas.entrySet()) {
            String areaName = entry.getKey();
            List<Point> area = entry.getValue();
            boolean rectPartInRegion =
                    contains(area, x, y)
                            || contains(area, x + width, y)
                            || contains(area, x, y + height)
                            || contains(area, x + width, y + height);
            if (rectPartInRegion) {
                regionsAccumulator.add(areaName);
                return true;
            }
        }

        return false;
    }

    public Set<String> areasNames(AreaType areaType) {
        if (StringUtils.isEmpty(rawValue)) {
            return null;
        }
        Set<String> res = new HashSet<>();
        String[] rawAreas = rawValue.split(";");
        for (String rawArea : rawAreas) {
            String[] parts = rawArea.split(":");

            if (areaType != null && areaType != AreaType.valueOf(parts[0])) {
                continue;
            }


            res.add(parts[1]);
        }
        return res;
    }

    private static boolean contains(List<Point> points, int testX, int testY) {
        int i;
        int j;
        boolean result = false;
        for (i = 0, j = points.size() - 1; i < points.size(); j = i++) {
            if (
                    (points.get(i).y() > testY)
                            !=
                            (points.get(j).y() > testY) &&
                            (
                                    testX
                                            <
                                            (points.get(j).x() - points.get(i).x())
                                                    * (testY - points.get(i).y())
                                                    / (points.get(j).y() - points.get(i).y())
                                                    + points.get(i).x()
                            )
            ) {
                result = !result;
            }
        }
        return result;
    }

    public Mat insensitiveMask(Mat targetMat) {
        Mat insensitiveMask = new Mat(targetMat.size(), targetMat.type(), Scalar.all(255));
        try (MatVector polygons = new MatVector()) {
            this.insensitiveAreas.forEach(insensitiveArea -> {
                Mat polygon = new Mat();
                insensitiveArea.forEach(p -> {
                    polygon.push_back(new Mat(p));
                });
                polygons.push_back(polygon);
            });
            fillPoly(insensitiveMask, polygons, new Scalar(0));
        }
        return insensitiveMask;
    }

    public UMat insensitiveMask(UMat targetMat) {
        UMat insensitiveMask = new UMat(targetMat.size(), targetMat.type(), Scalar.all(255));
        try (MatVector polygons = new MatVector()) {
            this.insensitiveAreas.forEach(insensitiveArea -> {
                Mat polygon = new Mat();
                insensitiveArea.forEach(p -> {
                    polygon.push_back(new Mat(p));
                });
                polygons.push_back(polygon);
            });
            fillPoly(insensitiveMask, polygons, new Scalar(0));
        }
        return insensitiveMask;
    }

    public enum AreaType {
        trigger,
        insensitive
    }
}
