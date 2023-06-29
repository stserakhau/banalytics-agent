package com.banalytics.box.module.media.thing;

import com.banalytics.box.api.integration.form.ComponentType;
import com.banalytics.box.api.integration.form.annotation.UIComponent;
import com.banalytics.box.module.AbstractConfiguration;
import com.banalytics.box.module.constants.MediaFormat;
import com.banalytics.box.module.constants.MediaUrlSchema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UrlMediaStreamThingConfiguration extends AbstractConfiguration {
    @UIComponent(index = 5, type = ComponentType.text_input_readonly)
    public String mac = "";

    @UIComponent(index = 10, type = ComponentType.text_input, required = true)
    public String title = "";

    @UIComponent(index = 20, type = ComponentType.drop_down, required = true, restartOnChange = true)
    public MediaUrlSchema schema = MediaUrlSchema.rtsp;

    @UIComponent(index = 30, type = ComponentType.drop_down, required = true, restartOnChange = true,
            dependsOn = {"schema"},
            uiConfig = {
                    @UIComponent.UIConfig(name = "show-empty", value = "false"),
                    @UIComponent.UIConfig(name = "enableCondition", value = "form.schema === 'http' || form.schema === 'https'")
            }
    )
    public MediaFormat streamFormat = MediaFormat.rtsp;

    @UIComponent(index = 40, type = ComponentType.text_input, required = true, restartOnChange = true)
    public String host;

    @UIComponent(index = 50,
            type = ComponentType.int_input,
            required = true,
            uiConfig = {
                    @UIComponent.UIConfig(name = "min", value = "0"),
                    @UIComponent.UIConfig(name = "max", value = "65535")
            }, restartOnChange = true
    )
    public int port;

    @UIComponent(index = 60, type = ComponentType.text_input, restartOnChange = true)
    public String path;

    @UIComponent(index = 70, type = ComponentType.text_input, restartOnChange = true)
    public String username;

    @UIComponent(index = 80, type = ComponentType.password_input, restartOnChange = true)
    public String password;

}
