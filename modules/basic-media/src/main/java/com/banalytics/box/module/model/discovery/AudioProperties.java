package com.banalytics.box.module.model.discovery;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class AudioProperties {
    public List<AudioCase> audioCases = new ArrayList<>();

    @Getter
    @Setter
    public static class AudioCase {
        int channels;
        int bits;
        int rate;
    }
}
