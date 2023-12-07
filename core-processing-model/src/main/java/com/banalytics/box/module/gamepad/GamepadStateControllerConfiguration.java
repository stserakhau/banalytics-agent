package com.banalytics.box.module.gamepad;

import com.banalytics.box.api.integration.form.annotation.UIComponent;
import com.banalytics.box.module.AbstractConfiguration;
import lombok.Getter;
import lombok.Setter;

import static com.banalytics.box.api.integration.form.ComponentType.int_input;

@Getter
@Setter
public class GamepadStateControllerConfiguration extends AbstractConfiguration {
//    @UIComponent(index = 10, type = int_input, required = true,
//            uiConfig = {
//                    @UIComponent.UIConfig(name = "min", value = "0"),
//                    @UIComponent.UIConfig(name = "max", value = "20")
//            })
//    public int gamepadIndex;

    @UIComponent(index = 20, type = int_input, required = true,
            uiConfig = {
                    @UIComponent.UIConfig(name = "step", value = "0.0001"),
                    @UIComponent.UIConfig(name = "min", value = "0.0001"),
                    @UIComponent.UIConfig(name = "max", value = "10000")
            })
    public double axisThreshold = 0.002;

    @UIComponent(index = 30, type = int_input, required = true,
            uiConfig = {
                    @UIComponent.UIConfig(name = "step", value = "0.0001"),
                    @UIComponent.UIConfig(name = "min", value = "0.0001"),
                    @UIComponent.UIConfig(name = "max", value = "10000")
            })
    public double buttonThreshold = 0.002;
}
