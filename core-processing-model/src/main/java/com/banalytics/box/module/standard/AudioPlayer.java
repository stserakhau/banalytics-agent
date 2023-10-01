package com.banalytics.box.module.standard;

import java.io.File;

public interface AudioPlayer {
    String getTitle();

    void play(File file) throws Exception;
}
