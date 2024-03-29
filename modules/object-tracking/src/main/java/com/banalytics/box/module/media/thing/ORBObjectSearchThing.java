package com.banalytics.box.module.media.thing;

import com.banalytics.box.module.AbstractThing;
import com.banalytics.box.module.BoxEngine;
import com.banalytics.box.module.media.ImageSearch;
import com.banalytics.box.module.storage.FileSystem;
import com.banalytics.box.module.storage.FileVO;
import lombok.extern.slf4j.Slf4j;
import org.bytedeco.javacv.OpenCVFrameConverter;
import org.bytedeco.opencv.global.opencv_imgcodecs;
import org.bytedeco.opencv.opencv_core.*;
import org.bytedeco.opencv.opencv_features2d.BFMatcher;
import org.bytedeco.opencv.opencv_features2d.Feature2D;
import org.bytedeco.opencv.opencv_features2d.ORB;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.bytedeco.opencv.global.opencv_imgproc.LINE_4;
import static org.bytedeco.opencv.global.opencv_imgproc.rectangle;

@Slf4j
public class ORBObjectSearchThing extends AbstractThing<ORBObjectSearchTaskConfig> implements ImageSearch<Mat> {
    public ORBObjectSearchThing(BoxEngine engine) {
        super(engine);
    }

    private OpenCVFrameConverter.ToMat converter;

    private FileSystem fileSystem;


    private final Mat mask = new Mat();
    private final UMat umask = new UMat();
    private Feature2D orb;
    private BFMatcher bf;

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

        this.orb = ORB.create();
        this.bf = new BFMatcher();
    }

    private final List<ImageDetails> targetDetails = new ArrayList<>();

    record ImageDetails(KeyPointVector keyPoints, Mat descriptors) {
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
                try (Mat mat = opencv_imgcodecs.imread(localFile.getAbsolutePath(), opencv_imgcodecs.IMREAD_COLOR)) { //IMREAD_GRAYSCALE
                    KeyPointVector keypoints = new KeyPointVector();
                    Mat descriptors = new Mat();
                    orb.detectAndCompute(mat, mask, keypoints, descriptors);
                    targetDetails.add(new ImageDetails(keypoints, descriptors));
                }
            }
        }

        for (int i = 1; i <= 3; i++) {
            Mat scene = opencv_imgcodecs.imread("E:\\!indexed-experiments\\car-samples\\scene\\" + i + ".jpg", opencv_imgcodecs.IMREAD_COLOR);

            List<Rect> res = this.search(null, scene);

            for (Rect re : res) {
                rectangle(scene, re, Scalar.RED, 1, LINE_4, 0);
            }

            opencv_imgcodecs.imwrite("E:\\!indexed-experiments\\car-samples\\scene\\" + i + "-out.jpg", scene);
        }
    }

    /**
     * https://docs.opencv.org/4.x/dc/dc3/tutorial_py_matcher.html
     */
    @Override
    public List<Rect> search(UUID requestor, Mat scene) throws Exception {
        try (KeyPointVector sceneKeypoints = new KeyPointVector();
             Mat sceneDescriptors = new Mat()) {
            long st = System.currentTimeMillis();
            orb.detectAndCompute(scene, mask, sceneKeypoints, sceneDescriptors);
            long en = System.currentTimeMillis();
            System.out.println("=============detect: " + (en - st));
            List<Rect> result = new ArrayList<>();
            for (ImageDetails targetDetail : targetDetails) {
                try (DMatchVector matches = new DMatchVector()) {
                    st = System.currentTimeMillis();
                    bf.match(
                            targetDetail.descriptors,
                            sceneDescriptors,
                            matches
                    );
                    en = System.currentTimeMillis();
                    System.out.println("=============match: " + (en - st));

                    var selected7Matches = selectBest(matches, 60);

                    var pointIndexesRight = new int[(int) selected7Matches.size()];
                    for (int i = 0; i < selected7Matches.get().length; i++) {
                        DMatch dMatch = selected7Matches.get(i);
                        int leftPointIndex = dMatch.trainIdx();
                        pointIndexesRight[i] = dMatch.queryIdx();
                    }
                    var selPointsRight = new Point2fVector();
                    KeyPoint.convert(sceneKeypoints, selPointsRight, pointIndexesRight);

                    for (Point2f point2f : selPointsRight.get()) {
                        result.add(
                                new Rect((int) (point2f.x() - 3), (int) (point2f.y() - 3), 6, 6)
                        );
                    }

//                    opencv_features2d.drawKeypoints(scene, sceneKeypoints,
//                            scene, new Scalar(255.0, 0, 0, 0),
//                            opencv_features2d.NOT_DRAW_SINGLE_POINTS);

                }
            }
            return result;
        }
    }

//    private Rect rect(List<DMatch> dMatches) {
//        int x1 = 0, y1 = 0, x2 = 0, y2 = 0;
//        for (DMatch dMatch : dMatches) {
//
//        }
//    }

    private static DMatchVector selectBest(DMatchVector matches, int top) {
        DMatchVector sortedVec = new DMatchVector();
        // Convert to Scala collection, and sort
        List<DMatch> sortedMatches = Arrays.asList(matches.get());
        sortedMatches.sort((f1, f2) -> Float.compare(f2.distance(), f1.distance()));

        int counter = 0;
        for (DMatch sortedMatch : sortedMatches) {
            if (counter > top) {
                break;
            }
            sortedVec.push_back(sortedMatch);
            counter++;
        }
        return sortedVec;
    }

    private final Object STOP_SYNC = new Object();

    @Override
    public void doStop() throws Exception {
        synchronized (STOP_SYNC) {
            for (ImageDetails targetDetail : targetDetails) {
                targetDetail.keyPoints.clear();
                targetDetail.keyPoints.close();
                targetDetail.descriptors.close();
            }
            targetDetails.clear();

            if (this.converter != null) {
                this.converter.close();
            }
            if (this.orb != null) {
                this.orb = null;
            }
            if (this.bf != null) {
                this.bf.close();
                this.bf = null;
            }
        }
    }
}
