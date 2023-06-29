package com.banalytics.box.module.onvif.task;

import com.banalytics.box.api.integration.form.ComponentType;
import com.banalytics.box.api.integration.form.annotation.UIComponent;
import com.banalytics.box.module.AbstractConfiguration;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import java.util.UUID;

import static com.banalytics.box.api.integration.form.ComponentType.checkbox;
import static com.banalytics.box.api.integration.form.ComponentType.drop_down;

@Getter
@Setter
public class GoToPresetActionConfiguration extends AbstractConfiguration {

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
            }
    )
    public UUID deviceUuid;

    @UIComponent(
            index = 30,
            type = ComponentType.drop_down,
            required = true,
            dependsOn = {"deviceUuid"},
            uiConfig = {
                    @UIComponent.UIConfig(name = "show-empty", value = "false"),
                    @UIComponent.UIConfig(name = "enableCondition", value = "form.deviceUuid !== ''"),
                    @UIComponent.UIConfig(name = "api-uuid", value = "deviceUuid"),
                    @UIComponent.UIConfig(name = "api-method", value = "ptzPresets")
            }
    )
    public String presetToken;

    @UIComponent(index = 40, type = ComponentType.int_input, required = true)
    public int stunTimeoutSec = 10;

    @UIComponent(index = 50, type = checkbox, required = true)
    public boolean returnActionEnabled = false;

    @UIComponent(index = 60, type = ComponentType.int_input, required = true,
            dependsOn = {"returnActionEnabled"},
            uiConfig = {
                    @UIComponent.UIConfig(name = "enableCondition", value = "''+form.returnActionEnabled === 'true'"),
                    @UIComponent.UIConfig(name = "min", value = "3"),
                    @UIComponent.UIConfig(name = "max", value = "60")
            })
    public int returnDelaySec = 10;

    @UIComponent(index = 70, type = drop_down, required = true, restartOnChange = true,
            dependsOn = {"returnActionEnabled"},
            uiConfig = {
                    @UIComponent.UIConfig(name = "enableCondition", value = "''+form.returnActionEnabled === 'true'"),
                    @UIComponent.UIConfig(name = "sort", value = "asc")
            },
            backendConfig = {
                    @UIComponent.BackendConfig(bean = "taskService", method = "findActionTasksUI")
            })
    public UUID returnActionUuid;
}
