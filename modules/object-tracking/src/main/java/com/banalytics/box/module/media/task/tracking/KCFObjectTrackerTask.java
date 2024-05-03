package com.banalytics.box.module.media.task.tracking;

import com.banalytics.box.api.integration.model.SubItem;
import com.banalytics.box.module.AbstractListOfTask;
import com.banalytics.box.module.BoxEngine;
import com.banalytics.box.module.media.task.AbstractMediaGrabberTask;
import org.bytedeco.opencv.opencv_tracking.TrackerKCF;
import org.bytedeco.opencv.opencv_video.Tracker;

@SubItem(of = AbstractMediaGrabberTask.class, singleton = true, group = "media-object-tracker")
public class KCFObjectTrackerTask extends AbstractObjectTrackerTask<KCFObjectTrackerTaskConfig> {
    public KCFObjectTrackerTask(BoxEngine engine, AbstractListOfTask<?> parent) {
        super(engine, parent);
    }

    @Override
    protected Tracker createTracker() {
        TrackerKCF tracker = TrackerKCF.create(
                new TrackerKCF.Params()
                        .detect_thresh(configuration.detectThresh)
                        .compress_feature(configuration.compressFeature)
                        .compressed_size(configuration.compressSize)
                        .resize(configuration.resize)
                        .desc_npca(configuration.descNpca)
                        .desc_pca(configuration.descPca)
                        .interp_factor(configuration.interpFactor)
                        .max_patch_size(configuration.maxPatchSize)
                        .output_sigma_factor(configuration.outputSigmaFactor)
                        .pca_learning_rate(configuration.pcaLearningRate)
                        .sigma(configuration.sigma)
                        .split_coeff(configuration.splitCoeff)
                        .wrap_kernel(configuration.wrapKernel)

        );//15ms CPU only
        return tracker;
    }
}
