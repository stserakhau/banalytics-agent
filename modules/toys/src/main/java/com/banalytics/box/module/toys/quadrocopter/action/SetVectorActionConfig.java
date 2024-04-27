package com.banalytics.box.module.toys.quadrocopter.action;

import com.banalytics.box.api.integration.form.ComponentType;
import com.banalytics.box.api.integration.form.annotation.UIComponent;
import com.banalytics.box.module.AbstractConfiguration;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import java.util.UUID;

import static com.banalytics.box.api.integration.form.ComponentType.checkbox;

@Getter
@Setter
public class SetVectorActionConfig extends AbstractConfiguration {
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
                    @UIComponent.BackendConfig(bean = "taskService", method = "findByStandard", params = {"com.banalytics.box.module.toys.quadrocopter.QuadrocopterThing"})
            },
            restartOnChange = true
    )
    public UUID deviceUuid;

    @UIComponent(
            index = 30, type = ComponentType.int_input,
            uiConfig = {
                    @UIComponent.UIConfig(name = "min", value = "0")
            }
    )
    public int transitionTimeMillis = 0;

    @UIComponent(index = 32, type = checkbox, required = true)
    public Boolean useRoll = false;
    @UIComponent(index = 33, type = checkbox, required = true)
    public Boolean usePitch = false;
    @UIComponent(index = 34, type = checkbox, required = true)
    public Boolean useYaw = false;
    @UIComponent(index = 35, type = checkbox, required = true)
    public Boolean useThrottle = false;

    @UIComponent(
            index = 40, type = ComponentType.int_input,
            dependsOn = "useRoll",
            uiConfig = {
                    @UIComponent.UIConfig(name = "enableCondition", value = "''+form.useRoll === 'true'"),
                    @UIComponent.UIConfig(name = "min", value = "-1"),
                    @UIComponent.UIConfig(name = "max", value = "1"),
                    @UIComponent.UIConfig(name = "step", value = "0.01")
            }
    )
    public double rollValue = 0;

    @UIComponent(
            index = 50, type = ComponentType.int_input,
            dependsOn = "usePitch",
            uiConfig = {
                    @UIComponent.UIConfig(name = "enableCondition", value = "''+form.usePitch === 'true'"),
                    @UIComponent.UIConfig(name = "min", value = "-1"),
                    @UIComponent.UIConfig(name = "max", value = "1"),
                    @UIComponent.UIConfig(name = "step", value = "0.01")
            }
    )
    public double pitchValue = 0;

    @UIComponent(
            index = 60, type = ComponentType.int_input,
            dependsOn = "useYaw",
            uiConfig = {
                    @UIComponent.UIConfig(name = "enableCondition", value = "''+form.useYaw === 'true'"),
                    @UIComponent.UIConfig(name = "min", value = "-1"),
                    @UIComponent.UIConfig(name = "max", value = "1"),
                    @UIComponent.UIConfig(name = "step", value = "0.01")
            }
    )
    public double yawValue = 0;

    @UIComponent(
            index = 70, type = ComponentType.int_input,
            dependsOn = "useThrottle",
            uiConfig = {
                    @UIComponent.UIConfig(name = "enableCondition", value = "''+form.useThrottle === 'true'"),
                    @UIComponent.UIConfig(name = "min", value = "0"),
                    @UIComponent.UIConfig(name = "max", value = "1"),
                    @UIComponent.UIConfig(name = "step", value = "0.01")
            }
    )
    public double throttleValue = 0;
}
