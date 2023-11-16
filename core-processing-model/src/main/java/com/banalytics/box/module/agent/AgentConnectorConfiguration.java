package com.banalytics.box.module.agent;

import com.banalytics.box.api.integration.form.ComponentType;
import com.banalytics.box.api.integration.form.annotation.UIComponent;
import com.banalytics.box.module.AbstractConfiguration;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class AgentConnectorConfiguration extends AbstractConfiguration {
    @UIComponent(index = 10, type = ComponentType.text_input)
    public String title;

    @UIComponent(
            index = 20,
            type = ComponentType.text_input,
            required = true, restartOnChange = true
    )
    public UUID agentUuid;

    @UIComponent(
            index = 20,
            type = ComponentType.password_input, restartOnChange = true
    )
    public String password;
}
