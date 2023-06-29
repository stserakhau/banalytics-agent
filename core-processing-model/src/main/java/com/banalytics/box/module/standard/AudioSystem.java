package com.banalytics.box.module.standard;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;

public interface AudioSystem {
    TargetDataLine openTargetDataLine() throws LineUnavailableException;

    int sampleRate();
}
