package com.banalytics.box.module.media.task.tracking;

import com.banalytics.box.api.integration.form.annotation.UIComponent;
import lombok.Getter;
import lombok.Setter;

import static com.banalytics.box.api.integration.form.ComponentType.int_input;

@Getter
@Setter
public class MILObjectTrackerTaskConfig extends AbstractObjectTrackerTaskConfig {
    @UIComponent(index = 10, type = int_input, required = true, restartOnChange = true, uiConfig = {
            @UIComponent.UIConfig(name = "min", value = "1")
    })
    public int featureSetNumFeatures = 250;

    @UIComponent(index = 10, type = int_input, required = true, restartOnChange = true, uiConfig = {
            @UIComponent.UIConfig(name = "min", value = "0.1"),
            @UIComponent.UIConfig(name = "step", value = "0.1")
    })
    public float samplerInitInRadius = 3.0f;

    @UIComponent(index = 10, type = int_input, required = true, restartOnChange = true, uiConfig = {
            @UIComponent.UIConfig(name = "min", value = "10")
    })
    public int samplerInitMaxNegNum = 65;

    @UIComponent(index = 10, type = int_input, required = true, restartOnChange = true, uiConfig = {
            @UIComponent.UIConfig(name = "min", value = "1")
    })
    public int samplerSearchWinSize = 25;

    @UIComponent(index = 10, type = int_input, required = true, restartOnChange = true, uiConfig = {
            @UIComponent.UIConfig(name = "min", value = "1")
    })
    public int samplerTrackInRadius = 4;

    @UIComponent(index = 10, type = int_input, required = true, restartOnChange = true, uiConfig = {
            @UIComponent.UIConfig(name = "min", value = "1")
    })
    public int samplerTrackMaxNegNum = 65;

    @UIComponent(index = 10, type = int_input, required = true, restartOnChange = true, uiConfig = {
            @UIComponent.UIConfig(name = "min", value = "1")
    })
    public int samplerTrackMaxPosNum = 100000;
}
