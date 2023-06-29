package com.banalytics.box.module.media.thing;

import com.banalytics.box.api.integration.form.ComponentType;
import com.banalytics.box.api.integration.form.annotation.UIComponent;
import com.banalytics.box.module.AbstractConfiguration;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class FileMediaStreamThingConfiguration extends AbstractConfiguration {
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
                    @UIComponent.UIConfig(name = "enableFolderSelection", value = "false"),
                    @UIComponent.UIConfig(name = "enableFileSelection", value = "true"),
                    @UIComponent.UIConfig(name = "fileNameFilter", value = "^.*\\.(mp4|avi|flv|mov|mkv)$"),
            }, restartOnChange = true)
    public String sourceUri;
}
