package com.banalytics.box.service;

import lombok.extern.slf4j.Slf4j;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.Line;
import javax.sound.sampled.Mixer;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

@Slf4j
public class LocalAudioService {
    private final static Map<String, Mixer> audioOutMixersMap = new LinkedHashMap<>();

    public static Set<String> supportedAudioPlayers() throws Exception {
        loadAudioPlayers();
        return audioOutMixersMap.keySet();
    }

    public static Mixer getAudioPlayer(String audioPlayerDescriptor) throws Exception {
        loadAudioPlayers();

        Mixer clip = audioOutMixersMap.get(audioPlayerDescriptor);
        if (clip == null) {
            throw new RuntimeException("Local audio system was reconfigured by user changed. ");
        }
        return clip;
    }

    private static void loadAudioPlayers() throws Exception {
        Mixer.Info[] mixers = AudioSystem.getMixerInfo();
        audioOutMixersMap.clear();
        for (Mixer.Info mixerInfo : mixers) {
            Mixer mixer = AudioSystem.getMixer(mixerInfo);
            for (Line.Info lineInfo : mixer.getSourceLineInfo()) {
                Line line = mixer.getLine(lineInfo);
                if (line instanceof Clip) {
                    audioOutMixersMap.put(
                            mixerInfo.getName().replaceAll(":", "_"),
                            mixer
                    );
                }
            }
        }
    }
}
