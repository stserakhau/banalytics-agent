package com.banalytics.box.module.media.task.classification.yolo;

import com.banalytics.box.api.integration.form.ComponentType;
import com.banalytics.box.api.integration.form.annotation.UIComponent;
import com.banalytics.box.module.AbstractConfiguration;
import lombok.Getter;
import lombok.Setter;

import static com.banalytics.box.api.integration.form.ComponentType.drop_down;
import static com.banalytics.box.api.integration.form.ComponentType.int_input;

@Getter
@Setter
public class YoloWorkerThingConfig extends AbstractConfiguration {

    @UIComponent(
            index = 10,
            type = ComponentType.int_input,
            uiConfig = {
                    @UIComponent.UIConfig(name = "min", value = "1"),
                    @UIComponent.UIConfig(name = "max", value = "10")
            },
            restartOnChange = true,
            required = true)
    public int workers = 1;

    @UIComponent(
            index = 20,
            type = drop_down,
            required = true,
            uiConfig = {
                    @UIComponent.UIConfig(name = "show-empty", value = "false")
            },
            backendConfig = {
                    @UIComponent.BackendConfig(bean = "taskService", method = "subModelsList", params = {"yolo"})
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
