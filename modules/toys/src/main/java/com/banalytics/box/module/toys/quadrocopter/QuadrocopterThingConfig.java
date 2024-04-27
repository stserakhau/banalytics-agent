package com.banalytics.box.module.toys.quadrocopter;

import com.banalytics.box.api.integration.form.ComponentType;
import com.banalytics.box.api.integration.form.annotation.UIComponent;
import com.banalytics.box.module.AbstractConfiguration;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class QuadrocopterThingConfig extends AbstractConfiguration {
    @UIComponent(index = 10, type = ComponentType.text_input, required = true)
    public String title = "";

    @UIComponent(index = 20, type = ComponentType.drop_down, required = true, backendConfig = {
            @UIComponent.BackendConfig(bean = "portService", method = "uiPorts")
    }, restartOnChange = true)
    public String serialPort;

    @UIComponent(index = 110, type = ComponentType.int_input, required = true, uiConfig = {
            @UIComponent.UIConfig(name = "min", value = "0"),
            @UIComponent.UIConfig(name = "max", value = "2000")
    }, restartOnChange = true)
    public int engineStart = 1009;

    @UIComponent(index = 110, type = ComponentType.int_input, required = true, uiConfig = {
            @UIComponent.UIConfig(name = "min", value = "0"),
            @UIComponent.UIConfig(name = "max", value = "2000")
    }, restartOnChange = true)
    public int engineMin = 1010;

    @UIComponent(index = 120, type = ComponentType.int_input, required = true, uiConfig = {
            @UIComponent.UIConfig(name = "min", value = "0"),
            @UIComponent.UIConfig(name = "max", value = "2000")
    }, restartOnChange = true)
    public int engineRange = 1100;

}
