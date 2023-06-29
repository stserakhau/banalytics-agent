package com.banalytics.box.module.network;

import com.banalytics.box.api.integration.form.annotation.UIComponent;
import com.banalytics.box.module.AbstractConfiguration;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

import static com.banalytics.box.api.integration.form.ComponentType.int_input;
import static com.banalytics.box.api.integration.form.ComponentType.range_input;

@Getter
@Setter
public class DeviceDiscoveryConfiguration extends AbstractConfiguration {
    public static UUID THING_UUID = UUID.fromString("00000000-0000-0000-0000-000000000006");

    @Override
    public UUID getUuid() {
        return THING_UUID;
    }

    @UIComponent(index = 10, type = range_input, required = true,
            uiConfig = {
                    @UIComponent.UIConfig(name = "step", value = "100"),
                    @UIComponent.UIConfig(name = "min", value = "500"),
                    @UIComponent.UIConfig(name = "max", value = "10000")
            })
    public int pingTimeout = 3000;
}
