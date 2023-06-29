package com.banalytics.box.module.system;

import com.banalytics.box.api.integration.form.ComponentType;
import com.banalytics.box.api.integration.form.annotation.UIComponent;
import com.banalytics.box.module.AbstractConfiguration;
import lombok.Getter;
import lombok.Setter;

import static com.banalytics.box.api.integration.form.ComponentType.checkbox;

@Getter
@Setter
public class ExecuteActionGroupActionConfiguration extends AbstractConfiguration {

    @UIComponent(index = 10, type = ComponentType.text_input, required = true)
    public String title;

    @UIComponent(index = 20, type = checkbox, required = true)
    public Boolean parallelExecution = true;

    @UIComponent(
            index = 30, type = ComponentType.multi_select,
            restartOnChange = true,
            uiConfig = {
                    @UIComponent.UIConfig(name = "sort", value = "asc")
            },
            backendConfig = {
                    @UIComponent.BackendConfig(bean = "taskService", method = "findActionTasksUI")
            }
    )
    public String fireActionsUuids;
}
