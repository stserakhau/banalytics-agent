package com.banalytics.box.module.media;

import java.util.List;
import java.util.UUID;

public interface ImageClassifier<T> {

    List<ClassificationResult> predict(UUID requestor, List<T> images, float confidenceThreshold, float nmsThreshold) throws Exception;

    record ClassificationResult(int classId, String className, float confidence,
                                  int x, int y, int width, int height) {
    }
}
