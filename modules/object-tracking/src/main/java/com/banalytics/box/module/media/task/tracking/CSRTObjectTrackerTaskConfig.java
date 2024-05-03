package com.banalytics.box.module.media.task.tracking;

import com.banalytics.box.api.integration.form.ComponentType;
import com.banalytics.box.api.integration.form.annotation.UIComponent;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class CSRTObjectTrackerTaskConfig extends AbstractObjectTrackerTaskConfig {
    /*@UIComponent(
            index = 20,
            type = ComponentType.drop_down,
            required = true, restartOnChange = true,
            uiConfig = {
                    @UIComponent.UIConfig(name = "show-empty", value = "false"),
                    @UIComponent.UIConfig(name = "sort", value = "asc")
            },
            backendConfig = {
                    @UIComponent.BackendConfig(bean = "taskService", method = "findByStandard", params = {"com.banalytics.box.module.storage.FileSystem"})
            }
    )
    public UUID fileSystemUuid;

    @UIComponent(
            index = 30,
            type = ComponentType.folder_chooser,
            required = true, restartOnChange = true,
            uiConfig = {
                    @UIComponent.UIConfig(name = "api-uuid", value = "fileSystemUuid"),
                    @UIComponent.UIConfig(name = "enableFolderSelection", value = "false"),
                    @UIComponent.UIConfig(name = "enableFileSelection", value = "true")
            })
    public String initialMaskFile;*/
}
