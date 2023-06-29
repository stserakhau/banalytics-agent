package com.banalytics.box.module.media.task.ffmpeg;

import com.banalytics.box.api.integration.form.ComponentType;
import com.banalytics.box.api.integration.form.annotation.UIComponent;
import com.banalytics.box.module.AbstractConfiguration;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class LocalMediaGrabberTaskConfiguration extends AbstractConfiguration {

    @UIComponent(
            index = 10,
            type = ComponentType.drop_down,
            required = true,
            uiConfig = {
                    @UIComponent.UIConfig(name = "sort", value = "asc")
            },
            backendConfig = {
                    @UIComponent.BackendConfig(bean = "taskService", method = "findByStandard", params = {"com.banalytics.box.module.media.thing.LocalMediaThing"})
            },
            restartOnChange = true
    )
    public UUID localMediaUuid;

    @UIComponent(
            index = 20,
            type = ComponentType.drop_down,
            required = true,
            backendConfig = {
                    @UIComponent.BackendConfig(values = {"0", "90", "180", "270"})
            },
            restartOnChange = true
    )
    public String rotateImage = "0";

    @UIComponent(
            index = 30,
            type = ComponentType.int_input,
            required = true,
            uiConfig = {
                    @UIComponent.UIConfig(name = "min", value = "1"),
                    @UIComponent.UIConfig(name = "max", value = "100")
            }, restartOnChange = true
    )
    public int rtBufferSizeMb = 10;
}
