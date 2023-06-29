package com.banalytics.box.module.audio;

import com.banalytics.box.api.integration.form.ComponentType;
import com.banalytics.box.api.integration.form.annotation.UIComponent;
import com.banalytics.box.module.AbstractConfiguration;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LocalAudioPlayerThingConfiguration extends AbstractConfiguration {

    @UIComponent(
            index = 3,
            type = ComponentType.drop_down,
            required = true,
            uiConfig = {
                    @UIComponent.UIConfig(name = "sort", value = "asc")
            },
            backendConfig = {
                    @UIComponent.BackendConfig(bean = "audioService", method = "supportedAudioPlayers")
            },
            restartOnChange = true
    )
    public String audioDevice = "";
}
