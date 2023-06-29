package com.banalytics.box.module.media.task.classification.yolo;

import org.bytedeco.opencv.global.opencv_dnn;

public enum PreferableTarget {
    DNN_TARGET_CPU(opencv_dnn.DNN_TARGET_CPU),
    DNN_TARGET_OPENCL(opencv_dnn.DNN_TARGET_OPENCL),
    DNN_TARGET_OPENCL_FP16(opencv_dnn.DNN_TARGET_OPENCL_FP16),
    DNN_TARGET_MYRIAD(opencv_dnn.DNN_TARGET_MYRIAD),
    DNN_TARGET_VULKAN(opencv_dnn.DNN_TARGET_VULKAN),
    /**
     * FPGA device with CPU fallbacks using Inference Engine's Heterogeneous plugin.
     */
    DNN_TARGET_FPGA(opencv_dnn.DNN_TARGET_FPGA),
    DNN_TARGET_CUDA(opencv_dnn.DNN_TARGET_CUDA),
    DNN_TARGET_CUDA_FP16(opencv_dnn.DNN_TARGET_CUDA_FP16),
    DNN_TARGET_HDDL(opencv_dnn.DNN_TARGET_HDDL);

    public final int value;

    PreferableTarget(int value) {
        this.value = value;
    }

    public static PreferableTarget of(int code) {
        for (PreferableTarget pb : values()) {
            if (pb.value == code) {
                return pb;
            }
        }
        return null;
    }
}
