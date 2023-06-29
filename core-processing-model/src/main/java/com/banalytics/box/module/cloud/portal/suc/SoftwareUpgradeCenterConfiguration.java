package com.banalytics.box.module.cloud.portal.suc;

import com.banalytics.box.api.integration.form.annotation.UIComponent;
import com.banalytics.box.module.AbstractConfiguration;
import com.banalytics.box.module.Singleton;
import com.banalytics.box.module.constants.SUCUpdateType;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

import static com.banalytics.box.api.integration.form.ComponentType.drop_down;

@Getter
@Setter
public class SoftwareUpgradeCenterConfiguration extends AbstractConfiguration {
    public static UUID THING_UUID = UUID.fromString("00000000-0000-0000-0000-000000000002");

    @UIComponent(
            index = 10,
            type = drop_down, required = true,
            restartOnChange = true
    )
    public SUCUpdateType updateType = SUCUpdateType.PORTAL;

    @Override
    public UUID getUuid() {
        return THING_UUID;
    }
}
