package com.banalytics.box.module.media.task.classification.yolo;

import org.bytedeco.opencv.opencv_core.IntIntPairVector;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

import static org.bytedeco.opencv.global.opencv_dnn.getAvailableBackends;

@Service
public class BytedecoInfoService implements InitializingBean {
    private final Map<String, String> computationPairs = new HashMap<>();

    @Override
    public void afterPropertiesSet() {
        IntIntPairVector pairs = getAvailableBackends();
        for (long i = 0; i < pairs.size(); i++) {
            int f = pairs.first(i);
            int s = pairs.second(i);
            String key = PreferableBackend.of(f) + ":" + PreferableTarget.of(s);
            String value = PreferableBackend.of(f) + " - " + PreferableTarget.of(s);
            computationPairs.put(key, value);
        }
    }

    public Map<String, String> computationPairs() {
        return computationPairs;
    }
}
