package com.banalytics.box.module.media.task.classification.yolo;

import com.banalytics.box.api.integration.form.ComponentType;
import com.banalytics.box.api.integration.form.annotation.UIComponent;
import com.banalytics.box.module.AbstractConfiguration;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

import static com.banalytics.box.api.integration.form.ComponentType.*;

@Getter
@Setter
public class YoloDetectionConfig extends AbstractConfiguration {
    @UIComponent(
            index = 20,
            type = ComponentType.drop_down,
            required = true,
            uiConfig = {
                    @UIComponent.UIConfig(name = "show-empty", value = "false")
            },
            backendConfig = {
                    @UIComponent.BackendConfig(bean = "taskService", method = "findByStandard", params = {"com.banalytics.box.module.media.task.classification.yolo.YoloWorkerThing"})
            }, restartOnChange = true
    )
    public UUID imageClassifierThingUuid;

    @UIComponent(
            index = 30, type = ComponentType.multi_select, required = false,
            dependsOn = {"yoloWorkerThingUuid"},
            uiConfig = {
                    @UIComponent.UIConfig(name = "enableCondition", value = "form.yoloWorkerThingUuid !== ''"),
                    @UIComponent.UIConfig(name = "sort", value = "asc"),
                    @UIComponent.UIConfig(name = "api-uuid", value = "yoloWorkerThingUuid"),
                    @UIComponent.UIConfig(name = "api-method", value = "readSupportedClasses")
            },
            restartOnChange = true
    )
    public String targetClasses;

    @UIComponent(
            index = 40, type = int_input,
            uiConfig = {
                    @UIComponent.UIConfig(name = "min", value = "0.1"),
                    @UIComponent.UIConfig(name = "max", value = "1"),
                    @UIComponent.UIConfig(name = "step", value = "0.05")
            },
            restartOnChange = true
    )
    public double confidenceThreshold = 0.6d;

    @UIComponent(
            index = 50, type = int_input,
            uiConfig = {
                    @UIComponent.UIConfig(name = "min", value = "0.05"),
                    @UIComponent.UIConfig(name = "max", value = "1"),
                    @UIComponent.UIConfig(name = "step", value = "0.05")
            }
    )
    public double nmsThreshold = 0.7d; //https://towardsdatascience.com/non-maximum-suppression-nms-93ce178e177c


    @UIComponent(
            index = 60,
            type = figures_painter,
            required = false
    )
    public String detectionAreas;

    @UIComponent(index = 70, type = checkbox, required = true)
    public Boolean drawClasses = true;

    @UIComponent(
            index = 80,
            type = int_input, required = true,
            uiConfig = {
                    @UIComponent.UIConfig(name = "step", value = "50"),
                    @UIComponent.UIConfig(name = "min", value = "100"),
                    @UIComponent.UIConfig(name = "max", value = "10000")
            }
    )
    public int detectionDelay = 500;

    @UIComponent(
            index = 90,
            type = int_input, required = true,
            uiConfig = {
                    @UIComponent.UIConfig(name = "min", value = "0")
            }, restartOnChange = true
    )
    public int eventStunTimeSec = 1;

    @UIComponent(index = 100, type = ComponentType.int_input, required = true,
            uiConfig = {
                    @UIComponent.UIConfig(name = "step", value = "0.1"),
                    @UIComponent.UIConfig(name = "min", value = "0.3"),
                    @UIComponent.UIConfig(name = "max", value = "2")
            })
    public double fontScale = 0.6;
}
