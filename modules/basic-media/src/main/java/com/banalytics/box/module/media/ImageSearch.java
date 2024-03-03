package com.banalytics.box.module.media;

import org.bytedeco.opencv.opencv_core.Rect;

import java.util.List;
import java.util.UUID;

public interface ImageSearch<T> {

    List<Rect> search(UUID requestor, T data) throws Exception;

    record SearchResult(int tlX, int tlY, int brX, int brY) {
    }
}
