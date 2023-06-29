package com.banalytics.box.module.storage.filestorage;

import com.banalytics.box.api.integration.form.ComponentType;
import com.banalytics.box.api.integration.form.annotation.UIComponent;
import com.banalytics.box.module.AbstractConfiguration;
import com.banalytics.box.module.ITitle;
import com.banalytics.box.module.constants.CleanupInterval;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

import static com.banalytics.box.api.integration.form.ComponentType.checkbox;
import static com.banalytics.box.api.integration.form.ComponentType.int_input;

@Getter
@Setter
public class FileStorageConfig extends AbstractConfiguration implements ITitle {
    @UIComponent(index = 10, type = ComponentType.text_input, required = false)
    public String title;

    @UIComponent(
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
                    @UIComponent.UIConfig(name = "enableFolderSelection", value = "true"),
                    @UIComponent.UIConfig(name = "enableFileSelection", value = "false")
            })
    public String destinationUri;

    @UIComponent(
            index = 40, type = ComponentType.drop_down, required = true, restartOnChange = true,
            uiConfig = {
                    @UIComponent.UIConfig(name = "show-empty", value = "false")
            }
    )
    public AccessType accessType = AccessType.READ_WRITE;


    @UIComponent(
            index = 60, type = ComponentType.drop_down, required = true, restartOnChange = true,
            uiConfig = {
                    @UIComponent.UIConfig(name = "show-empty", value = "false"),
                    @UIComponent.UIConfig(name = "enableCondition", value = "form.accessType === 'READ_WRITE_DELETE'"),
            },
            dependsOn = {"accessType"}
    )
    public LimitType limitType = LimitType.NO_LIMIT;

    @UIComponent(
            index = 70,
            type = ComponentType.drop_down, required = true, restartOnChange = true,
            dependsOn = {"limitType", "accessType"},
            uiConfig = {
                    @UIComponent.UIConfig(name = "show-empty", value = "false"),
                    @UIComponent.UIConfig(name = "enableCondition", value = "form.limitType !== 'NO_LIMIT' && form.accessType === 'READ_WRITE_DELETE'"),
            }
    )
    public CleanupInterval cleanUpTime = CleanupInterval.m15;

    @UIComponent(
            index = 80,
            type = int_input, required = true, restartOnChange = true,
            dependsOn = {"limitType", "cleanUpTime", "accessType"},
            uiConfig = {
                    @UIComponent.UIConfig(name = "enableCondition", value = "form.limitType !== 'NO_LIMIT' && form.cleanUpTime != 'NA' && form.accessType === 'READ_WRITE_DELETE'"),
                    @UIComponent.UIConfig(name = "min", value = "0"),
                    @UIComponent.UIConfig(name = "max", value = "10")
            }
    )
    public int applyByHierarchyLevel = 0;


    @UIComponent(
            index = 90,
            type = int_input, required = true, restartOnChange = true,
            dependsOn = {"limitType", "cleanUpTime", "accessType"},
            uiConfig = {
                    @UIComponent.UIConfig(name = "enableCondition", value = "form.limitType !== 'NO_LIMIT' && form.cleanUpTime != 'NA' && form.accessType === 'READ_WRITE_DELETE'"),
                    @UIComponent.UIConfig(name = "min", value = "0")
            }
    )
    public long limitValue = 1024;

    @UIComponent(index = 95, type = checkbox, required = true,
            dependsOn = {"accessType"},
            uiConfig = {
                    @UIComponent.UIConfig(name = "enableCondition", value = "form.accessType.indexOf('WRITE') > -1")
            }
    )
    public boolean uploadEnabled = false;

    @UIComponent(index = 96, type = checkbox, required = true,
            dependsOn = {"accessType"},
            uiConfig = {
                    @UIComponent.UIConfig(name = "enableCondition", value = "form.accessType.indexOf('WRITE') > -1")
            }
    )
    public boolean moveEnabled = false;

    @UIComponent(
            index = 100,
            type = ComponentType.drop_down, required = false, restartOnChange = true,
            dependsOn = {"limitType", "cleanUpTime", "accessType"},
            uiConfig = {
                    @UIComponent.UIConfig(name = "enableCondition", value = "form.limitType !== 'NO_LIMIT' && form.cleanUpTime != 'NA' && form.accessType === 'READ_WRITE_DELETE'"),
                    @UIComponent.UIConfig(name = "sort", value = "asc")
            },
            backendConfig = {
                    @UIComponent.BackendConfig(bean = "taskService", method = "findByStandard", params = {"com.banalytics.box.module.standard.FileStorage"})
            }
    )
    public UUID backupFileStorageUuid;

    @UIComponent(
            index = 110, type = checkbox, required = true,
            dependsOn = {"accessType"},
            uiConfig = {
                    @UIComponent.UIConfig(name = "enableCondition", value = "form.accessType === 'READ_WRITE_DELETE'"),
            }
    )
    public boolean safeOps = true;

    @UIComponent(index = 120, type = checkbox, required = true)
    public boolean useBrowserCache = false;

    @UIComponent(
            index = 130,
            type = int_input, required = true,
            dependsOn = {"useBrowserCache"},
            uiConfig = {
                    @UIComponent.UIConfig(name = "enableCondition", value = "''+form.useBrowserCache === 'true'"),
                    @UIComponent.UIConfig(name = "min", value = "1")
            }
    )
    public int browserCacheTTLMinutes = 60;
}
