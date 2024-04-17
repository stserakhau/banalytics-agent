package com.banalytics.box.module.media.thing;

import com.banalytics.box.module.AbstractThing;
import com.banalytics.box.module.BoxEngine;
import com.banalytics.box.module.media.ImageSearch;
import com.banalytics.box.module.storage.FileSystem;
import com.banalytics.box.module.storage.FileVO;
import lombok.extern.slf4j.Slf4j;
import org.bytedeco.javacpp.indexer.FloatIndexer;
import org.bytedeco.javacv.OpenCVFrameConverter;
import org.bytedeco.opencv.global.opencv_imgcodecs;
import org.bytedeco.opencv.opencv_core.*;

import java.io.File;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import static org.bytedeco.opencv.global.opencv_core.CV_32FC1;
import static org.bytedeco.opencv.global.opencv_imgproc.*;
import static org.opencv.imgproc.Imgproc.TM_CCORR_NORMED;

@Slf4j
public class TemplateObjectSearchThing extends AbstractThing<TemplateObjectSearchTaskConfig> implements ImageSearch<Mat> {
    public TemplateObjectSearchThing(BoxEngine engine) {
        super(engine);
    }

    private OpenCVFrameConverter.ToMat converter;

    private FileSystem fileSystem;

    @Override
    public Object uniqueness() {
        return configuration.title;
    }

    @Override
    public String getTitle() {
        return "ORB: " + configuration.title;
    }


    @Override
    protected void doInit() throws Exception {
        this.converter = new OpenCVFrameConverter.ToMat();
    }

    private final List<ImageDetails> searchSamples = new ArrayList<>();

    record ImageDetails(Mat imgSample) {
    }

    @Override
    protected void doStart() throws Exception {
        this.fileSystem = engine.getThingAndSubscribe(configuration.fileSystemUuid, this);

        List<FileVO> files = this.fileSystem.filesList(configuration.targetSamplesPath, null);
        for (FileVO file : files) {
            if (file.isFolder) {
                continue;
            }
            if (file.uri.toLowerCase().endsWith(".png")
                    || file.uri.toLowerCase().endsWith(".jpg")
                    || file.uri.toLowerCase().endsWith(".bmp")
            ) {
                File localFile = this.fileSystem.getLocalFile(file.uri);
                Mat mat = opencv_imgcodecs.imread(localFile.getAbsolutePath(), opencv_imgcodecs.IMREAD_COLOR); //IMREAD_GRAYSCALE
                searchSamples.add(new ImageDetails(mat));
            }
        }

        for (int i = 1; i <= 7; i++) {
            Mat scene = opencv_imgcodecs.imread("E:\\!indexed-experiments\\car-samples\\scene\\target" + i + ".jpg", opencv_imgcodecs.IMREAD_COLOR);

            List<Rect> res = this.search(null, scene);

            for (Rect re : res) {
                rectangle(scene, re, Scalar.RED, 1, LINE_4, 0);
            }

            opencv_imgcodecs.imwrite("E:\\!indexed-experiments\\car-samples\\scene\\target" + i + "-out.jpg", scene);
        }
    }

    /**
     * https://docs.opencv.org/4.x/dc/dc3/tutorial_py_matcher.html
     */
    @Override
    public List<Rect> search(UUID requestor, Mat scene) throws Exception {
        List<Rect> result = new ArrayList<>();

        Mat detectionMat = new Mat(scene.rows(), scene.cols(), CV_32FC1);
        for (ImageDetails targetDetail : searchSamples) {
            long st = System.currentTimeMillis();
            matchTemplate(scene, targetDetail.imgSample, detectionMat, TM_CCORR_NORMED);
            long en = System.currentTimeMillis();
            System.out.println("=============match: " + (en - st));

            List<Point> points = getPointsFromMatAboveThreshold(detectionMat, configuration.threshold);
            int cnt = Math.min(configuration.hypothesisCount, points.size());
            for (int i = 0; i < cnt; i++) {
                Point p = points.get(i);
                rectangle(scene,
                        new Rect(
                                p.x(),
                                p.y(),
                                targetDetail.imgSample.cols(),
                                targetDetail.imgSample.rows()
                        ),
                        randColor(), 2, 0, 0);
            }
        }
        return result;
    }

    private final Object STOP_SYNC = new Object();

    @Override
    public void doStop() throws Exception {
        synchronized (STOP_SYNC) {
            for (ImageDetails targetDetail : searchSamples) {
                targetDetail.imgSample.close();
            }
            searchSamples.clear();

            if (this.converter != null) {
                this.converter.close();
            }
        }
    }

    public static Scalar randColor() {
        int b, g, r;
        b = ThreadLocalRandom.current().nextInt(0, 255 + 1);
        g = ThreadLocalRandom.current().nextInt(0, 255 + 1);
        r = ThreadLocalRandom.current().nextInt(0, 255 + 1);
        return new Scalar(b, g, r, 0);
    }

    public static List<Point> getPointsFromMatAboveThreshold(Mat m, double threshold) {
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
