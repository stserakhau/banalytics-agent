package com.banalytics.box.module.media.thing;

import com.banalytics.box.api.integration.form.ComponentType;
import com.banalytics.box.api.integration.form.annotation.UIComponent;
import com.banalytics.box.module.AbstractConfiguration;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LocalMediaThingConfig extends AbstractConfiguration {
    @UIComponent(index = 10, type = ComponentType.text_input, required = true)
    public String title = "";

    @UIComponent(index = 20, type = ComponentType.drop_down, required = true,
            uiConfig = {
                    @UIComponent.UIConfig(name = "sort", value = "asc")
            },
            backendConfig = {
                    @UIComponent.BackendConfig(bean = "localMediaDeviceDiscoveryService", method = "cameras")
            },
            restartOnChange = true
    )
    public String camera;

    @UIComponent(index = 30, type = ComponentType.drop_down, required = true,
            dependsOn = {"camera", "pixelFormat"},
            uiConfig = {//
                    @UIComponent.UIConfig(name = "enableCondition", value = "form.camera !== ''"),
//                    @UIComponent.UIConfig(name = "api-uuid", value = "form.uuid"),//todo work only for created things but not for new
//                    @UIComponent.UIConfig(name = "api-method", value = "readResolutions")
                    @UIComponent.UIConfig(name = "api-uuid", value = "00000000-0000-0000-0000-000000000005"),//portal integration thing
                    @UIComponent.UIConfig(name = "api-method", value = "bean:localMediaDeviceDiscoveryService:cameraResolutionsFps"),
                    @UIComponent.UIConfig(name = "api-params", value = "camera")
            }, restartOnChange = true
    )
    public String resolution;

    @UIComponent(index = 40, type = ComponentType.int_input, required = true,
            dependsOn = {"camera"},
            uiConfig = {
                    @UIComponent.UIConfig(name = "enableCondition", value = "form.camera !== ''"),
                    @UIComponent.UIConfig(name = "min", value = "1"),
                    @UIComponent.UIConfig(name = "max", value = "50")
            }, restartOnChange = true
    )
    public double fps = 15;

    @UIComponent(index = 50, type = ComponentType.drop_down,
            dependsOn = {"camera"},
            uiConfig = {
                    @UIComponent.UIConfig(name = "enableCondition", value = "form.camera !== ''"),
                    @UIComponent.UIConfig(name = "sort", value = "asc")
            },
            backendConfig = {
                    @UIComponent.BackendConfig(bean = "localMediaDeviceDiscoveryService", method = "microphones")
            },
            restartOnChange = true
    )
    public String microphone;

    @UIComponent(index = 60, type = ComponentType.drop_down, required = true,
            dependsOn = {"microphone"},
            uiConfig = {
                    @UIComponent.UIConfig(name = "enableCondition", value = "form.microphone !== ''"),
//                    @UIComponent.UIConfig(name = "api-uuid", value = "form.uuid"),//todo work only for created things but not for new
//                    @UIComponent.UIConfig(name = "api-method", value = "readMicrophones")
                    @UIComponent.UIConfig(name = "api-uuid", value = "00000000-0000-0000-0000-000000000005"),//portal integration thing
                    @UIComponent.UIConfig(name = "api-method", value = "bean:localMediaDeviceDiscoveryService:audioSupportedSampleRates"),
                    @UIComponent.UIConfig(name = "api-params", value = "microphone")
            }, restartOnChange = true
    )
    public int sampleRate;
}
