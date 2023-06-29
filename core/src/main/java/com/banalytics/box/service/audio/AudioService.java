package com.banalytics.box.service.audio;

import com.banalytics.box.service.LocalAudioService;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class AudioService {

    /**
     * Supported audio players
     */
    public static Set<String> supportedAudioPlayers() throws Exception {
        return LocalAudioService.supportedAudioPlayers();
    }
}
