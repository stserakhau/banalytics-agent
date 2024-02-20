package com.banalytics.box.module.media.task.search;


import org.bytedeco.javacpp.indexer.FloatIndexer;
import org.bytedeco.opencv.opencv_core.*;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import static org.bytedeco.opencv.global.opencv_core.CV_32FC1;
import static org.bytedeco.opencv.global.opencv_highgui.*;
import static org.bytedeco.opencv.global.opencv_imgcodecs.IMREAD_COLOR;
import static org.bytedeco.opencv.global.opencv_imgcodecs.imread;
import static org.bytedeco.opencv.global.opencv_imgproc.matchTemplate;
import static org.bytedeco.opencv.global.opencv_imgproc.rectangle;
import static org.opencv.imgproc.Imgproc.TM_CCORR_NORMED;

public class Test {
    public static void main(String[] args) {
        newStyle(new String[]{
                "E:\\banalytics-samples\\object-tracking\\target5.jpg",
                "E:\\banalytics-samples\\object-tracking\\part6.jpg"
        }, 0.9f);
    }

    /*
     */
    public static void newStyle(String[] args, float thres) {
        //read in image default colors
        Mat sourceColor = imread(args[0]);
//        Mat sourceGrey = new Mat(sourceColor.size(), CV_8UC1);
//        cvtColor(sourceColor, sourceColor, COLOR_BGR2GRAY);
        //load in template in grey
        Mat template = imread(args[1], IMREAD_COLOR); //int = 0
        //Size for the result image
        Size size = new Size(sourceColor.cols() - template.cols() + 1, sourceColor.rows() - template.rows() + 1);
        Mat result = new Mat(size, CV_32FC1);
        matchTemplate(sourceColor, template, result, TM_CCORR_NORMED);
//        threshold(result, result, 0.9, );
        List<Point> points = getPointsFromMatAboveThreshold(result, thres);

        for (int i = 0; i < 1; i++) {
            Point p = points.get(i);
            rectangle(sourceColor, new Rect(p.x(), p.y(), template.cols(), template.rows()), randColor(), 2, 0, 0);
        }

//        DoublePointer minVal = new DoublePointer();
//        DoublePointer maxVal = new DoublePointer();
//        Point min = new Point();
//        Point max = new Point();
//        minMaxLoc(result, minVal, maxVal, min, max, null);
//        rectangle(sourceColor, new Rect(max.x(), max.y(), template.cols(), template.rows()), randColor(), 2, 0, 0);

        imshow("Original marked", sourceColor);
        imshow("Ttemplate", template);
        imshow("Results matrix", result);
        waitKey(0);
        destroyAllWindows();

    }

    // some usefull things.
    public static Scalar randColor() {
        int b, g, r;
        b = ThreadLocalRandom.current().nextInt(0, 255 + 1);
        g = ThreadLocalRandom.current().nextInt(0, 255 + 1);
        r = ThreadLocalRandom.current().nextInt(0, 255 + 1);
        return new Scalar(b, g, r, 0);
    }

    public static List<Point> getPointsFromMatAboveThreshold(Mat m, float threshold) {
        Map<String, Float> targetPoints = new HashMap<>();

        FloatIndexer indexer = m.createIndexer();
        for (int y = 0; y < m.rows(); y++) {
            for (int x = 0; x < m.cols(); x++) {
                float koef = indexer.get(y, x);
                if (koef > threshold) {
                    int _x = x / 10 * 10;
                    int _y = y / 10 * 10;
                    String key = _x + ":" + _y;
                    if (!targetPoints.containsKey(key)) {
                        targetPoints.put(key, koef);
                    }
                }
            }
        }
        Set<Map.Entry<String, Float>> points = new TreeSet<>((o1, o2) -> o2.getValue().compareTo(o1.getValue()));
        points.addAll(targetPoints.entrySet());


        return points.stream().map(p -> {
            String key = p.getKey();
            String[] parts = key.split(":");
            return new Point(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]));
        }).collect(Collectors.toList());

        /*Map<String, Point> targetPoints = new HashMap<>();
        for (Point point : points) {
            int x = point.x() / 10 * 10;
            int y = point.y() / 10 * 10;
            String pair = x + ":" + y;
            if (!targetPoints.containsKey(pair)) {
                targetPoints.put(pair, point);
            }
        }*/
    }
}