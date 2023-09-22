package com.banalytics.box.module.media.task.motion.storage;

import com.banalytics.box.api.integration.form.ComponentType;
import com.banalytics.box.api.integration.form.annotation.UIComponent;
import com.banalytics.box.module.AbstractConfiguration;
import com.banalytics.box.module.constants.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

import static com.banalytics.box.api.integration.form.ComponentType.int_input;

@Getter
@Setter
public class MotionVideoRecordingConfig extends AbstractConfiguration {

    @UIComponent(
            index = 15,
            type = ComponentType.drop_down,
            required = true,
            uiConfig = {
                    @UIComponent.UIConfig(name = "sort", value = "asc")
            },
            backendConfig = {
                    @UIComponent.BackendConfig(bean = "taskService", method = "findByStandard", params = {"com.banalytics.box.module.standard.FileStorage"})//<< todo security
            },
            restartOnChange = true
    )
    public UUID storageUuid;

    @UIComponent(index = 15,
            type = ComponentType.checkbox,
            required = true
    )
    public boolean disableAudioRecording = false;

    @UIComponent(index = 20, type = ComponentType.drop_down, required = true, restartOnChange = true)
    public TimestampFormat pathPattern = TimestampFormat.yyyyMMdd_hh_mmss;

    @UIComponent(index = 30, type = ComponentType.drop_down, required = true)
    public UseFrameRate useFrameRate = UseFrameRate.CALCULATED_FRAME_RATE;

    @UIComponent(
            index = 40,
            type = int_input, required = true,
            uiConfig = {
                    @UIComponent.UIConfig(name = "min", value = "0")
            }
    )
    public long minMotionTimeFilterMillis = 1000;

    @UIComponent(
            index = 50,
            type = int_input, required = true,
            uiConfig = {
                    @UIComponent.UIConfig(name = "min", value = "0")
            }
    )
    public long minRecordingSizeKb = 10;


    @UIComponent(index = 60, type = ComponentType.drop_down, required = true)
    public BitRate videoBitRate = BitRate.k256;


    /**
     * Split video stream on parts which size not gread tham splitTimeout
     */
    @UIComponent(
            index = 70,
            type = ComponentType.drop_down,
            required = true
    )
    public SplitTimeInterval splitTimeout = SplitTimeInterval.m1;

    /*
     * Recording time after motion disappeared.
     */
    @UIComponent(
            index = 80,
            type = int_input, required = true,
            uiConfig = {
                    @UIComponent.UIConfig(name = "min", value = "0"),
                    @UIComponent.UIConfig(name = "max", value = "300000"),
            }
    )
    public long recordingOnMotionDisappearedTimoutMillis = 1000L;

    @UIComponent(
            index = 90,
            type = ComponentType.drop_down,
            required = true
    )
    public VideoPreBufferTime preBufferSeconds = VideoPreBufferTime.OFF;
}
