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
public class PTZRotateActionConfiguration extends AbstractConfiguration {

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
                    @UIComponent.UIConfig(name = "step", value = "0.1")
            }
    )
    public double speedX = 1;
    @UIComponent(
            index = 40, type = ComponentType.int_input,
            uiConfig = {
                    @UIComponent.UIConfig(name = "step", value = "0.1")
            }
    )
    public double speedY = 1;
    @UIComponent(
            index = 50, type = ComponentType.int_input,
            uiConfig = {
                    @UIComponent.UIConfig(name = "step", value = "0.1")
            })
    public double speedZ = 1;

    @UIComponent(index = 60, type = ComponentType.int_input, required = true, uiConfig = {
            @UIComponent.UIConfig(name = "min", value = "50"),
            @UIComponent.UIConfig(name = "max", value = "1000")
    })
    public int stopTimeout = 100;


//    @UIComponent(index = 70, type = checkbox, required = true, restartOnChange = true)
//    public boolean returnActionEnabled = false;
//
//    @UIComponent(index = 80, type = ComponentType.int_input, required = true, restartOnChange = true,
//            dependsOn = {"returnActionEnabled"},
//            uiConfig = {
//                    @UIComponent.UIConfig(name = "enableCondition", value = "''+form.returnActionEnabled === 'true'"),
//                    @UIComponent.UIConfig(name = "min", value = "3"),
//                    @UIComponent.UIConfig(name = "max", value = "60")
//            })
//    public int returnDelaySec = 90;
//
//    @UIComponent(index = 100, type = drop_down, required = true, restartOnChange = true,
//            dependsOn = {"returnActionEnabled"},
//            uiConfig = {
//                    @UIComponent.UIConfig(name = "enableCondition", value = "''+form.returnActionEnabled === 'true'"),
//                    @UIComponent.UIConfig(name = "sort", value = "asc")
//            },
//            backendConfig = {
//                    @UIComponent.BackendConfig(bean = "taskService", method = "findActionTasksUI")
//            })
//    public UUID returnActionUuid;
}
