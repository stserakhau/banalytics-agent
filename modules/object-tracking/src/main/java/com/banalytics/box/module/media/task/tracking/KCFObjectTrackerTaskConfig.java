package com.banalytics.box.module.media.task.tracking;

import com.banalytics.box.api.integration.form.annotation.UIComponent;
import lombok.Getter;
import lombok.Setter;

import static com.banalytics.box.api.integration.form.ComponentType.checkbox;
import static com.banalytics.box.api.integration.form.ComponentType.int_input;

@Getter
@Setter
public class KCFObjectTrackerTaskConfig extends AbstractObjectTrackerTaskConfig {
    @UIComponent(index = 10, type = int_input, required = true, restartOnChange = true, uiConfig = {
            @UIComponent.UIConfig(name = "min", value = "0"),
            @UIComponent.UIConfig(name = "max", value = "1"),
            @UIComponent.UIConfig(name = "step", value = "0.05")
    })
    public float detectThresh = 0.2f;

    @UIComponent(index = 10, type = checkbox, required = true, restartOnChange = true)
    public boolean compressFeature = true;

    @UIComponent(index = 10, type = int_input, required = true, restartOnChange = true, uiConfig = {
            @UIComponent.UIConfig(name = "min", value = "1"),
            @UIComponent.UIConfig(name = "max", value = "10")
    })
    public int compressSize = 2;

    @UIComponent(index = 10, type = checkbox, required = true, restartOnChange = true)
    public boolean resize = true;

    @UIComponent(index = 10, type = int_input, required = true, restartOnChange = true, uiConfig = {
            @UIComponent.UIConfig(name = "min", value = "0")
    })
    public int descNpca = 1;

    @UIComponent(index = 10, type = int_input, required = true, restartOnChange = true, uiConfig = {
            @UIComponent.UIConfig(name = "min", value = "0")
    })
    public int descPca = 2;

    @UIComponent(index = 10, type = checkbox, required = true, restartOnChange = true)
    public float interpFactor = 0.075f;

    @UIComponent(index = 10, type = int_input, required = true, restartOnChange = true, uiConfig = {
            @UIComponent.UIConfig(name = "min", value = "0")
    })
    public int maxPatchSize = 6400;

    @UIComponent(index = 10, type = int_input, required = true, restartOnChange = true, uiConfig = {
            @UIComponent.UIConfig(name = "min", value = "0"),
            @UIComponent.UIConfig(name = "max", value = "1"),
            @UIComponent.UIConfig(name = "step", value = "0.0001")
    })
    public float outputSigmaFactor = 0.0625f;

    @UIComponent(index = 10, type = int_input, required = true, restartOnChange = true, uiConfig = {
            @UIComponent.UIConfig(name = "min", value = "0"),
            @UIComponent.UIConfig(name = "max", value = "1"),
            @UIComponent.UIConfig(name = "step", value = "0.05")
    })
    public float pcaLearningRate = 0.15f;

    @UIComponent(index = 10, type = int_input, required = true, restartOnChange = true, uiConfig = {
            @UIComponent.UIConfig(name = "min", value = "0"),
            @UIComponent.UIConfig(name = "max", value = "1"),
            @UIComponent.UIConfig(name = "step", value = "0.05")
    })
    public float sigma = 0.2f;

    @UIComponent(index = 10, type = checkbox, required = true, restartOnChange = true)
    public boolean splitCoeff = true;

    @UIComponent(index = 10, type = checkbox, required = true, restartOnChange = true)
    public boolean wrapKernel = false;
}
