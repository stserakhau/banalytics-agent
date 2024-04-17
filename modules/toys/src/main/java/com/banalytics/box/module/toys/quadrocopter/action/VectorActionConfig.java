package com.banalytics.box.module.toys.quadrocopter.action;

import com.banalytics.box.api.integration.form.ComponentType;
import com.banalytics.box.api.integration.form.annotation.UIComponent;
import com.banalytics.box.module.AbstractConfiguration;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import java.util.UUID;

@Getter
@Setter
public class VectorActionConfig extends AbstractConfiguration {
    @UIComponent(index = 10, type = ComponentType.text_input, required = true)
    public String title;

    @NotNull
    @UIComponent(
            index = 20,
            type = ComponentType.drop_down,
            required = true,
            uiConfig = {
                    @UIComponent.UIConfig(name = "sort", value = "asc"),
                    @UIComponent.UIConfig(name = "show-empty", value = "false")
            },
            backendConfig = {
                    @UIComponent.BackendConfig(bean = "taskService", method = "findByStandard", params = {"com.banalytics.box.module.toys.quadrocopter.QuadrocopterThing"})
            },
            restartOnChange = true
    )
    public UUID deviceUuid;

    @UIComponent(
            index = 30, type = ComponentType.drop_down,
            uiConfig = {
                    @UIComponent.UIConfig(name = "sort", value = "asc"),
                    @UIComponent.UIConfig(name = "api-uuid", value = "00000000-0000-0000-0000-000000000011"),
                    @UIComponent.UIConfig(name = "api-method", value = "readGamepadsIds")
            }
    )
    public String gamepadId;

    @UIComponent(
            index = 40, type = ComponentType.int_input,
            uiConfig = {
                    @UIComponent.UIConfig(name = "step", value = "0.01"),
                    @UIComponent.UIConfig(name = "min", value = "-1"),
                    @UIComponent.UIConfig(name = "max", value = "1")
            }
    )
    public double stopThreshold = 0.1;

    @UIComponent(
            index = 45, type = ComponentType.drop_down
    )
    public AxisFunction xyAxisFunction = AxisFunction.LINEAR;

    @UIComponent(
            index = 50, type = ComponentType.int_input,
            uiConfig = {
                    @UIComponent.UIConfig(name = "min", value = "-1"),
                    @UIComponent.UIConfig(name = "max", value = "128")
            }
    )
    public int axisXIndex = -1;

    @UIComponent(
            index = 60, type = ComponentType.int_input,
            uiConfig = {
                    @UIComponent.UIConfig(name = "min", value = "-1"),
                    @UIComponent.UIConfig(name = "max", value = "128")
            }
    )
    public int axisYIndex = -1;

    @UIComponent(
            index = 70, type = ComponentType.int_input,
            uiConfig = {
                    @UIComponent.UIConfig(name = "min", value = "-1"),
                    @UIComponent.UIConfig(name = "max", value = "128")
            }
    )
    public int axisZIndex = -1;

    @UIComponent(
            index = 80, type = ComponentType.int_input,
            uiConfig = {
                    @UIComponent.UIConfig(name = "min", value = "-1"),
                    @UIComponent.UIConfig(name = "max", value = "128")
            }
    )
    public int axisPowerIndex = -1;

    @UIComponent(index = 90, type = ComponentType.checkbox)
    public boolean reverseX;

    @UIComponent(index = 100, type = ComponentType.checkbox)
    public boolean reverseY;

    @UIComponent(index = 110, type = ComponentType.checkbox)
    public boolean reverseZ;

    @UIComponent(index = 120, type = ComponentType.checkbox)
    public boolean negativePowerAllowed;

    @UIComponent(index = 130, type = ComponentType.checkbox)
    public boolean reversePower;

}
