package com.banalytics.box.service.windows;

import com.banalytics.box.module.model.discovery.AudioProperties;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class AudioDevicePropertyAccumulator {
    public AudioProperties audioProperties = new AudioProperties();

    public void process(List<String> lines) {
        for (String line : lines) {
            if (!line.contains("[dshow")) {
                continue;
            }
            log.info(line);
            if (line.contains("bits")) {
                AudioProperties.AudioCase audioCase = new AudioProperties.AudioCase();

                String[] parts = line.substring(line.indexOf("]") + 1).trim().split(", ");
                for (String part : parts) {
                    if (part.startsWith("ch=")) {
                        String channel = part.substring(3).trim();
                        audioCase.setChannels(Integer.parseInt(channel));
                    } else if (part.startsWith("bits=")) {
                        String bits = part.substring(5).trim();
                        audioCase.setBits(Integer.parseInt(bits));
                    } else if (part.startsWith("rate=")) {
                        String rate = part.substring(5).trim();
                        audioCase.setRate(Integer.parseInt(rate));
                    }
                }
                audioProperties.audioCases.add(audioCase);
            }
        }

        audioProperties.audioCases.sort((a,b)->{
            int res1 =  a.getRate() - b.getRate();
            int res2 =  a.getBits() - b.getBits();
            int res3 =  a.getChannels() - b.getChannels();

            return res1 != 0 ? res1 : res2!=0 ? res2 : res3;
        });
    }
}