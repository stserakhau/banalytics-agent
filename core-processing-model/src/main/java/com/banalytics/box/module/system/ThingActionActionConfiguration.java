package com.banalytics.box.module.system;

import com.banalytics.box.api.integration.form.ComponentType;
import com.banalytics.box.api.integration.form.annotation.UIComponent;
import com.banalytics.box.module.AbstractConfiguration;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class ThingActionActionConfiguration extends AbstractConfiguration {
    @UIComponent(
            index = 100,
            type = ComponentType.drop_down,
            required = true,
            backendConfig = {
                    @UIComponent.BackendConfig(bean = "taskService", method = "nonConfigThingMap")
            },
            restartOnChange = true
    )
    public UUID targetThing;

    @UIComponent(
            index = 110,
            type = ComponentType.checkbox,
            required = true
    )
    public boolean ignoreAutostartState = false;

    @UIComponent(
            index = 120, type = ComponentType.drop_down, required = true
    )
    public Action action;

    public enum Action {
        START, STOP, RESTART
    }
}
