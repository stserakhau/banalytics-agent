package com.banalytics.box.module.audio;

import com.banalytics.box.api.integration.form.ComponentType;
import com.banalytics.box.api.integration.form.annotation.UIComponent;
import com.banalytics.box.module.AbstractConfiguration;
import com.banalytics.box.module.constants.TimeInterval;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class PlayAudioActionConfiguration extends AbstractConfiguration {
    @UIComponent(
            index = 100,
            type = ComponentType.drop_down,
            required = true,
            uiConfig = {
                    @UIComponent.UIConfig(name = "sort", value = "asc"),
                    @UIComponent.UIConfig(name = "show-empty", value = "false")
            },
            backendConfig = {
                    @UIComponent.BackendConfig(bean = "taskService", method = "findByStandard", params = {"com.banalytics.box.module.standard.AudioPlayer"})
            },
            restartOnChange = true
    )
    public UUID audioPlayerUuid;

    @UIComponent(
            index = 110,
            type = ComponentType.drop_down,
            required = true,
            uiConfig = {
                    @UIComponent.UIConfig(name = "show-empty", value = "false"),
                    @UIComponent.UIConfig(name = "sort", value = "asc")
            },
            backendConfig = {
                    @UIComponent.BackendConfig(bean = "taskService", method = "findByStandard", params = {"com.banalytics.box.module.storage.FileSystem"})
            },
            restartOnChange = true
    )
    public UUID fileSystemUuid;

    @UIComponent(
            index = 120,
            type = ComponentType.folder_chooser,
            required = true,
            dependsOn = {"fileSystemUuid"},
            uiConfig = {
                    @UIComponent.UIConfig(name = "enableCondition", value = "form.fileSystemUuid !== ''"),
                    @UIComponent.UIConfig(name = "api-uuid", value = "fileSystemUuid"),
                    @UIComponent.UIConfig(name = "enableFolderSelection", value = "false"),
                    @UIComponent.UIConfig(name = "enableFileSelection", value = "true"),
                    @UIComponent.UIConfig(name = "fileNameFilter", value = "^.*\\.(wav|acc|mp3)$")
            })
    public String playAudioFile;

    @UIComponent(
            index = 130,
            type = ComponentType.drop_down,
            required = true,
            uiConfig = {
                    @UIComponent.UIConfig(name = "show-empty", value = "false")
            }
    )
    public TimeInterval waitBeforeNextExecution = TimeInterval.s5;
}
