package com.banalytics.box.service;

import org.bytedeco.opencv.global.opencv_dnn;

public enum PreferableBackend {
    DNN_BACKEND_DEFAULT(opencv_dnn.DNN_BACKEND_DEFAULT),
    DNN_BACKEND_HALIDE(opencv_dnn.DNN_BACKEND_HALIDE),
    DNN_BACKEND_INFERENCE_ENGINE(opencv_dnn.DNN_BACKEND_INFERENCE_ENGINE),
    DNN_BACKEND_OPENCV(opencv_dnn.DNN_BACKEND_OPENCV),
    DNN_BACKEND_VKCOM(opencv_dnn.DNN_BACKEND_VKCOM),
    DNN_BACKEND_CUDA(opencv_dnn.DNN_BACKEND_CUDA),
    DNN_BACKEND_WEBNN(opencv_dnn.DNN_BACKEND_WEBNN);

    public final int value;

    PreferableBackend(int value) {
        this.value = value;
    }

    public static PreferableBackend of(int code) {
        for (PreferableBackend pb : values()) {
            if (pb.value == code) {
                return pb;
            }
        }
        return null;
    }
}
