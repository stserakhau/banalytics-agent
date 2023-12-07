package com.banalytics.box.module.onvif.task;

import com.banalytics.box.api.integration.form.ComponentType;
import com.banalytics.box.api.integration.form.annotation.UIComponent;
import com.banalytics.box.module.AbstractConfiguration;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import java.util.UUID;

@Getter
@Setter
public class PTZContinousAxisActionConfiguration extends AbstractConfiguration {

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
                    @UIComponent.BackendConfig(bean = "taskService", method = "findByStandard", params = {"com.banalytics.box.module.standard.Onvif"})
            },
            restartOnChange = true
    )
    public UUID deviceUuid;

    @UIComponent(
            index = 30, type = ComponentType.int_input,
            uiConfig = {
                    @UIComponent.UIConfig(name = "min", value = "0"),
                    @UIComponent.UIConfig(name = "max", value = "20")
            }
    )
    public int gamepadIndex;

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
            index = 50, type = ComponentType.int_input,
            uiConfig = {
                    @UIComponent.UIConfig(name = "min", value = "0"),
                    @UIComponent.UIConfig(name = "max", value = "128")
            }
    )
    public int axisXIndex = 1;

    @UIComponent(
            index = 60, type = ComponentType.int_input,
            uiConfig = {
                    @UIComponent.UIConfig(name = "min", value = "0"),
                    @UIComponent.UIConfig(name = "max", value = "128")
            }
    )
    public int axisYIndex = 1;

    @UIComponent(
            index = 60, type = ComponentType.int_input,
            uiConfig = {
                    @UIComponent.UIConfig(name = "min", value = "0"),
                    @UIComponent.UIConfig(name = "max", value = "128")
            }
    )
    public int axisZoomIndex = 1;

    @UIComponent(index = 70, type = ComponentType.checkbox)
    public boolean reverseX;

    @UIComponent(index = 80, type = ComponentType.checkbox)
    public boolean reverseY;

    @UIComponent(index = 90, type = ComponentType.checkbox)
    public boolean reverseZoom;

}
