package com.banalytics.box.module.system;

import com.banalytics.box.api.integration.form.ComponentType;
import com.banalytics.box.api.integration.form.annotation.UIComponent;
import com.banalytics.box.module.AbstractConfiguration;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

import static com.banalytics.box.api.integration.form.ComponentType.checkbox;

@Getter
@Setter
public class SetTaskStateActionConfiguration extends AbstractConfiguration {

    @UIComponent(index = 10, type = ComponentType.text_input, required = true)
    public String title;

    @UIComponent(
            index = 20,
            type = ComponentType.drop_down,
            required = true,
            backendConfig = {
                    @UIComponent.BackendConfig(bean = "taskService", method = "nonActionTasksMap")
            },
            restartOnChange = true
    )
    public UUID targetTask;

    @UIComponent(
            index = 30,
            type=ComponentType.task_form,
            required = true,
            dependsOn = {"targetTask"},
            uiConfig = {
                    @UIComponent.UIConfig(name="taskUuid", value="targetTask")
            }
    )
    public String taskFormData;
}
