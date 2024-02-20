package com.banalytics.box.module.media.task.tracking;

import com.banalytics.box.api.integration.form.ComponentType;
import com.banalytics.box.api.integration.form.annotation.UIComponent;
import lombok.Getter;
import lombok.Setter;

import static com.banalytics.box.api.integration.form.ComponentType.drop_down;

@Getter
@Setter
public class VitObjectTrackerTaskConfig extends AbstractObjectTrackerTaskConfig {
    @UIComponent(
            index = 20,
            type = drop_down,
            required = true,
            uiConfig = {
                    @UIComponent.UIConfig(name = "show-empty", value = "false")
            },
            backendConfig = {
                    @UIComponent.BackendConfig(bean = "taskService", method = "subModelsList", params = {"vittrack"})
            },
            restartOnChange = true
    )
    public String subModelName;

    @UIComponent(
            index = 30,
            type = ComponentType.drop_down,
            required = true,
            uiConfig = {
                    @UIComponent.UIConfig(name = "show-empty", value = "false")
            },
            backendConfig = {
                    @UIComponent.BackendConfig(bean = "bytedecoInfoService", method = "computationPairs")
            },
            restartOnChange = true
    )
    public String computationConfig;
}
