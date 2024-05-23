package com.banalytics.box.module.toys.gps;

import com.banalytics.box.api.integration.form.ComponentType;
import com.banalytics.box.api.integration.form.annotation.UIComponent;
import com.banalytics.box.module.AbstractConfiguration;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GPSThingConfig extends AbstractConfiguration {
    @UIComponent(index = 10, type = ComponentType.text_input, required = true)
    public String title = "";

    @UIComponent(index = 20, type = ComponentType.drop_down, required = true, backendConfig = {
            @UIComponent.BackendConfig(bean = "portService", method = "uiPorts")
    }, restartOnChange = true)
    public String serialPort;
}
