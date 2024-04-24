package com.banalytics.box.module.media.task.motion.detector;

import com.banalytics.box.api.integration.form.ComponentType;
import com.banalytics.box.api.integration.form.annotation.UIComponent;
import com.banalytics.box.module.AbstractConfiguration;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

import static com.banalytics.box.api.integration.form.ComponentType.*;

@Getter
@Setter
public class MotionDetectionConfig extends AbstractConfiguration {

    /**
     * Rectangle: rect:x1,y1,x2,y2;
     * Polyline: poly:x1,y1,x2,y2,...,xn,yn;
     */
    @UIComponent(
            index = 10,
            type = figures_painter,
            restartOnChange = true
    )
    public String detectionAreas;

    @UIComponent(index = 20, type = drop_down, required = true, uiConfig = {
            @UIComponent.UIConfig(name = "show-empty", value = "false")
    })
    public DebugMode debug = DebugMode.BG_SUBSTRACTOR;

//    @UIComponent(index = 25, type = checkbox, required = true, restartOnChange = true)
//    public Boolean autoCalibration = true;

    @UIComponent(index = 30, type = int_input, required = true, restartOnChange = true,
//            dependsOn = {"autoCalibration"},
            uiConfig = {
//                    @UIComponent.UIConfig(name = "enableCondition", value = "''+form.autoCalibration === 'false'"),
                    @UIComponent.UIConfig(name = "min", value = "4"),
                    @UIComponent.UIConfig(name = "max", value = "100")
            })
    public int backgroundHistoryDistThreshold = 8;

    @UIComponent(index = 40, type = drop_down, required = true, restartOnChange = true,
//            dependsOn = {"autoCalibration"},
            uiConfig = {
//                    @UIComponent.UIConfig(name = "enableCondition", value = "''+form.autoCalibration === 'false'"),
                    @UIComponent.UIConfig(name = "show-empty", value = "false")
            })
    public MatrixSizeType blurSize = MatrixSizeType.s3x3;

//    @UIComponent(index = 50, type = int_input, required = true, restartOnChange = true,
//            dependsOn = {"autoCalibration"},
//            uiConfig = {
//                    @UIComponent.UIConfig(name = "enableCondition", value = "''+form.autoCalibration === 'false'"),
//                    @UIComponent.UIConfig(name = "min", value = "1"),
//                    @UIComponent.UIConfig(name = "max", value = "100")
//            })
//    public int backgroundHistorySize = 4;

    @UIComponent(index = 60, type = drop_down, required = true, restartOnChange = true,
//            dependsOn = {"autoCalibration"},
            uiConfig = {
//                    @UIComponent.UIConfig(name = "enableCondition", value = "''+form.autoCalibration === 'false'"),
                    @UIComponent.UIConfig(name = "show-empty", value = "false")
            }
    )
    public MatrixSizeType dilateSize = MatrixSizeType.zero;

    @UIComponent(index = 70, type = int_input, required = true,
            uiConfig = {
                    @UIComponent.UIConfig(name = "min", value = "1"),
                    @UIComponent.UIConfig(name = "max", value = "30")
            })
    public int detectionIntensity = 10;

    @UIComponent(index = 80, type = int_input, required = true,
            uiConfig = {
                    @UIComponent.UIConfig(name = "min", value = "10"),
                    @UIComponent.UIConfig(name = "max", value = "1000000")
            })
    public int triggeredAreaSize = 200;

    @UIComponent(index = 90, type = checkbox, required = true)
    public Boolean drawDetections = true;

    @UIComponent(index = 100, type = checkbox, required = true)
    public Boolean drawNoises = false;

    @UIComponent(index = 110, type = ComponentType.int_input, required = true,
            uiConfig = {
                    @UIComponent.UIConfig(name = "step", value = "0.1"),
                    @UIComponent.UIConfig(name = "min", value = "0.3"),
                    @UIComponent.UIConfig(name = "max", value = "2")
            })
    public double fontScale = 0.6;

    @UIComponent(index = 115, type = int_input, required = true,
            uiConfig = {
                    @UIComponent.UIConfig(name = "min", value = "0"),
                    @UIComponent.UIConfig(name = "max", value = "20")
            }, restartOnChange = true)
    public int turnOnDelaySec = 5;

    @UIComponent(index = 120, type = int_input, required = true,
            uiConfig = {
                    @UIComponent.UIConfig(name = "min", value = "0")
            }, restartOnChange = true)
    public int eventStunTimeSec = 1;

    @UIComponent(index = 130, type = drop_down, required = true, restartOnChange = true,
            uiConfig = {
                    @UIComponent.UIConfig(name = "show-empty", value = "false")
            })
    public MotionTriggerMode motionTriggerMode = MotionTriggerMode.MOTION_ONLY;


    @UIComponent(index = 140, type = drop_down, required = true, restartOnChange = true,
            dependsOn = {"motionTriggerMode"},
            uiConfig = {
                    @UIComponent.UIConfig(name = "enableCondition", value = "form.motionTriggerMode === 'MOTION_AND_CLASSIFIER'"),
                    @UIComponent.UIConfig(name = "sort", value = "asc")
            },
            backendConfig = {
                    @UIComponent.BackendConfig(bean = "taskService", method = "findByStandard", params = {"com.banalytics.box.module.media.ImageClassifier"})
            })
    public UUID imageClassifierThingUuid;

    @UIComponent(
            index = 150, type = int_input,
            dependsOn = {"motionTriggerMode"},
            uiConfig = {
                    @UIComponent.UIConfig(name = "enableCondition", value = "form.motionTriggerMode === 'MOTION_AND_CLASSIFIER'"),
                    @UIComponent.UIConfig(name = "min", value = "0.1"),
                    @UIComponent.UIConfig(name = "max", value = "1"),
                    @UIComponent.UIConfig(name = "step", value = "0.05")
            }
    )
    public double confidenceThreshold = 0.6d;

    @UIComponent(
            index = 160, type = int_input,
            dependsOn = {"motionTriggerMode"},
            uiConfig = {
                    @UIComponent.UIConfig(name = "enableCondition", value = "form.motionTriggerMode === 'MOTION_AND_CLASSIFIER'"),
                    @UIComponent.UIConfig(name = "min", value = "0.05"),
                    @UIComponent.UIConfig(name = "max", value = "1"),
                    @UIComponent.UIConfig(name = "step", value = "0.05")
            }
    )
    public double nmsThreshold = 0.7d; //https://towardsdatascience.com/non-maximum-suppression-nms-93ce178e177c

    @UIComponent(index = 170, type = multi_select, restartOnChange = true,
            dependsOn = {"motionTriggerMode", "imageClassifierThingUuid"},
            uiConfig = {
                    @UIComponent.UIConfig(name = "enableCondition", value = "form.motionTriggerMode === 'MOTION_AND_CLASSIFIER' && form.imageClassifierThingUuid !== ''"),
                    @UIComponent.UIConfig(name = "sort", value = "asc"),
                    @UIComponent.UIConfig(name = "api-uuid", value = "imageClassifierThingUuid"),
                    @UIComponent.UIConfig(name = "api-method", value = "readSupportedClasses")
            })
    public String targetClasses;

    @UIComponent(index = 180, type = checkbox, required = true,
            dependsOn = {"motionTriggerMode", "imageClassifierThingUuid"},
            uiConfig = {
                    @UIComponent.UIConfig(name = "enableCondition", value = "form.motionTriggerMode === 'MOTION_AND_CLASSIFIER' && form.imageClassifierThingUuid !== ''")
            })
    public Boolean drawClasses = true;

    @UIComponent(index = 190, type = int_input, required = true,
            dependsOn = {"motionTriggerMode", "imageClassifierThingUuid"},
            uiConfig = {
                    @UIComponent.UIConfig(name = "enableCondition", value = "form.motionTriggerMode === 'MOTION_AND_CLASSIFIER' && form.imageClassifierThingUuid !== ''"),
                    @UIComponent.UIConfig(name = "step", value = "50"),
                    @UIComponent.UIConfig(name = "min", value = "100"),
                    @UIComponent.UIConfig(name = "max", value = "10000")
            })
    public int classificationDelay = 500;


    enum MotionTriggerMode {
        MOTION_ONLY,
        MOTION_AND_CLASSIFIER
    }

    enum DebugMode {
        OFF,
        BG_SUBSTRACTOR,
        TARGET_FRAME
    }
}
