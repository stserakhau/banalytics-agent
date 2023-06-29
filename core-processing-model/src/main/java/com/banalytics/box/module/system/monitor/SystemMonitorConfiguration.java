package com.banalytics.box.module.system.monitor;

import com.banalytics.box.api.integration.form.ComponentType;
import com.banalytics.box.api.integration.form.annotation.UIComponent;
import com.banalytics.box.module.AbstractConfiguration;
import com.banalytics.box.module.IUuid;
import com.banalytics.box.module.Singleton;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class SystemMonitorConfiguration extends AbstractConfiguration {
    public static UUID THING_UUID = UUID.fromString("00000000-0000-0000-0000-000000000010");

    @UIComponent(index = 10, type = ComponentType.int_input, required = true,
            uiConfig = {
                    @UIComponent.UIConfig(name = "min", value = "1"),
                    @UIComponent.UIConfig(name = "max", value = "60")
            }, restartOnChange = true
    )
    public int updateSpeed = 5;

    @Override
    public UUID getUuid() {
        return THING_UUID;
    }

}
