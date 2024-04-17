package com.banalytics.box.module.system;

import com.banalytics.box.api.integration.form.ComponentType;
import com.banalytics.box.api.integration.form.annotation.UIComponent;
import com.banalytics.box.module.AbstractConfiguration;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class TimerActionConfiguration extends AbstractConfiguration {
    @UIComponent(index = 10, type = ComponentType.text_input, required = true)
    public String title;

    @UIComponent(
            index = 100,
            type = ComponentType.drop_down,
            required = true,
            uiConfig = {
                    @UIComponent.UIConfig(name = "sort", value = "asc")
            },
            backendConfig = {
                    @UIComponent.BackendConfig(bean = "taskService", method = "findActionTasksUI")
            },
            restartOnChange = true
    )
    public UUID targetAction;

    @UIComponent(
            index = 110,
            type = ComponentType.int_input,
            required = true
    )
    public long delayMillis = 1000;
}
