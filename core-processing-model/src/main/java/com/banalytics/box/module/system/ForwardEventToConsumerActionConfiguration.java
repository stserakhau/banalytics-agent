package com.banalytics.box.module.system;

import com.banalytics.box.api.integration.form.ComponentType;
import com.banalytics.box.api.integration.form.annotation.UIComponent;
import com.banalytics.box.module.AbstractConfiguration;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class ForwardEventToConsumerActionConfiguration extends AbstractConfiguration {
    @UIComponent(
            index = 20,
            type = ComponentType.drop_down,
            required = true, restartOnChange = true,
            uiConfig = {
                    @UIComponent.UIConfig(name = "sort", value = "asc")
            },
            backendConfig = {
                    @UIComponent.BackendConfig(bean = "taskService", method = "findByStandard", params = {"com.banalytics.box.module.standard.EventConsumer"})
            }
    )
    public UUID eventConsumerThing;

    @UIComponent(
            index = 30, type = ComponentType.multi_select,
            restartOnChange = true,
            uiConfig = {
                    @UIComponent.UIConfig(name = "enableCondition", value = "form.eventConsumerThing !== ''"),
                    @UIComponent.UIConfig(name = "sort", value = "asc"),
                    @UIComponent.UIConfig(name = "api-uuid", value = "eventConsumerThing"),
                    @UIComponent.UIConfig(name = "api-method", value = "readAccounts")
            },
            dependsOn = {"eventConsumerThing"}
    )
    public String forwardToAccounts;
}
