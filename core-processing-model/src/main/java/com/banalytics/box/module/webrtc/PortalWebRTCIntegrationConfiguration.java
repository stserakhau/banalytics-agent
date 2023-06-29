package com.banalytics.box.module.webrtc;

import com.banalytics.box.api.integration.form.annotation.UIComponent;
import com.banalytics.box.module.AbstractConfiguration;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

import static com.banalytics.box.api.integration.form.ComponentType.*;

@Getter
@Setter
public class PortalWebRTCIntegrationConfiguration extends AbstractConfiguration {
    public static UUID WEB_RTC_UUID = UUID.fromString("00000000-0000-0000-0000-000000000005");

    @Override
    public UUID getUuid() {
        return WEB_RTC_UUID;
    }

    @UIComponent(index = 10, type = int_input, required = true, uiConfig = {
            @UIComponent.UIConfig(name = "step", value = "1"),
            @UIComponent.UIConfig(name = "min", value = "15")
    })
    public int tokenTTLMinutes = 15;

    @UIComponent(index = 20, type = int_input, required = true, uiConfig = {
            @UIComponent.UIConfig(name = "step", value = "1"),
            @UIComponent.UIConfig(name = "min", value = "1"),
            @UIComponent.UIConfig(name = "max", value = "10")
    })
    public int clientTimeoutMinutes = 5;

    @UIComponent(index = 50, type = int_input, required = true, uiConfig = {
            @UIComponent.UIConfig(name = "step", value = "10000"),
            @UIComponent.UIConfig(name = "min", value = "50000"),
            @UIComponent.UIConfig(name = "max", value = "100000000")
    })
    public int maxConnectionBandwidth = 500000;

    @UIComponent(index = 60, type = range_input, required = true, uiConfig = {
            @UIComponent.UIConfig(name = "step", value = "5"),
            @UIComponent.UIConfig(name = "min", value = "5"),
            @UIComponent.UIConfig(name = "max", value = "95")
    })
    public int reservedBandwidthForFileTransmissionPercent = 30;

    @UIComponent(index = 70, type = drop_down, required = true,
            uiConfig = {
                    @UIComponent.UIConfig(name = "show-empty", value = "false")
            })
    public QualityStrategy rtMediaQualityStrategy = QualityStrategy.DYNAMIC;

    @UIComponent(index = 80, type = drop_down, required = true,
            uiConfig = {
                    @UIComponent.UIConfig(name = "show-empty", value = "false")
            })
    public QualityProfile rtMediaQualityProfile = QualityProfile.HIGH;

    public enum QualityStrategy {
        STATIC, DYNAMIC
    }

    public enum QualityProfile {
        BAD("faster", "zerolatency ", 0,  5000),
        VERY_LOW("faster", "zerolatency ", 2,10000),
        LOW("faster", "zerolatency ", 4,20000),
        LOWER_MEDIUM("faster", "zerolatency ", 8,30000),
        MEDIUM("faster", "zerolatency ", 10,50000),
        UPPER_MEDIUM("faster", "zerolatency", 14,80000),
        HIGH("faster", "zerolatency", 18,120000),
        VERY_HIGH("faster", "zerolatency", 22,200000),
        HIGHEST("faster", "zerolatency", 28,640000);

        public final String preset;
        public final String tune;
        public final int crf;
        public final int videoBitrate;

        QualityProfile(String preset, String tune, int crf, int videoBitrate) {
            this.preset = preset;
            this.tune = tune;
            this.crf = crf;
            this.videoBitrate = videoBitrate;
        }
    }
}
