package com.banalytics.box.module.media.thing;

import com.banalytics.box.api.integration.form.ComponentType;
import com.banalytics.box.api.integration.form.annotation.UIComponent;
import com.banalytics.box.module.AbstractConfiguration;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

import static com.banalytics.box.api.integration.form.ComponentType.int_input;
import static com.banalytics.box.api.integration.form.ComponentType.text_input;

@Getter
@Setter
public class TemplateObjectSearchTaskConfig extends AbstractConfiguration {
    @UIComponent(
            index = 10,
            type = text_input,
            required = true
    )
    public String title;

    @UIComponent(
            index = 20,
            type = ComponentType.drop_down,
            required = true,
            uiConfig = {
                    @UIComponent.UIConfig(name = "show-empty", value = "false"),
                    @UIComponent.UIConfig(name = "sort", value = "asc")
            },
            backendConfig = {
                    @UIComponent.BackendConfig(bean = "taskService", method = "findByStandard", params = {"com.banalytics.box.module.storage.FileSystem"})
            }, restartOnChange = true
    )
    public UUID fileSystemUuid;

    @UIComponent(
            index = 30,
            type = ComponentType.folder_chooser,
            required = true,
            uiConfig = {
                    @UIComponent.UIConfig(name = "api-uuid", value = "fileSystemUuid"),
                    @UIComponent.UIConfig(name = "enableFolderSelection", value = "true"),
                    @UIComponent.UIConfig(name = "enableFileSelection", value = "false")
            }, restartOnChange = true)
    public String targetSamplesPath;

    @UIComponent(index = 40, type = int_input, required = true,
            uiConfig = {
                    @UIComponent.UIConfig(name = "min", value = "0.1"),
                    @UIComponent.UIConfig(name = "max", value = "1"),
                    @UIComponent.UIConfig(name = "step", value = "0.05")
            }, restartOnChange = true)
    public double threshold = 0.9;

    @UIComponent(index = 40, type = int_input, required = true,
            uiConfig = {
                    @UIComponent.UIConfig(name = "min", value = "1"),
                    @UIComponent.UIConfig(name = "max", value = "59")
            }, restartOnChange = true)
    public int hypothesisCount = 4;
}
