package com.banalytics.box.module.gamepad;

import com.banalytics.box.api.integration.form.annotation.UIComponent;
import com.banalytics.box.module.AbstractConfiguration;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

import static com.banalytics.box.api.integration.form.ComponentType.int_input;

@Getter
@Setter
public class GamepadStateControllerConfiguration extends AbstractConfiguration {
    public static UUID THING_UUID = UUID.fromString("00000000-0000-0000-0000-000000000011");

    @Override
    public UUID getUuid() {
        return THING_UUID;
    }

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
