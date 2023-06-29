package com.banalytics.box.module;

import com.banalytics.box.api.integration.form.ComponentType;
import com.banalytics.box.api.integration.form.annotation.UIComponent;
import com.banalytics.box.module.constants.RestartOnFailure;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

import static com.banalytics.box.api.integration.form.ComponentType.hidden;
import static com.banalytics.box.api.integration.form.ComponentType.text_input_readonly;

@Getter
@Setter
public abstract class AbstractConfiguration implements IConfiguration {
    @UIComponent(
            index = 0,
            type = hidden, required = true
    )
    public UUID uuid;

    @UIComponent(
            index = 0,
            type = hidden, required = true
    )
    public boolean autostart = true;

    @UIComponent(
            index = 2,
            type = ComponentType.drop_down,
            required = true
    )
    public RestartOnFailure restartOnFailure = RestartOnFailure.STOP_ON_FAILURE;
}
