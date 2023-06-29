package com.banalytics.box.module.events;

import com.banalytics.box.api.integration.form.annotation.UIComponent;
import com.banalytics.box.module.AbstractConfiguration;
import com.banalytics.box.module.IUuid;
import com.banalytics.box.module.Singleton;
import com.banalytics.box.module.constants.LongTimeInterval;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

import static com.banalytics.box.api.integration.form.ComponentType.*;

@Getter
@Setter
public class EventHistoryThingConfig extends AbstractConfiguration {
    public static UUID THING_UUID = UUID.fromString("00000000-0000-0000-0000-000000000004");

    @Override
    public UUID getUuid() {
        return THING_UUID;
    }

    @UIComponent(index = 10, type = int_input, required = true, uiConfig = {
            @UIComponent.UIConfig(name = "min", value = "0")
    }, restartOnChange = true)
    public int historyLengthInDays = 1;

    @UIComponent(index = 20, type = drop_down, required = true, restartOnChange = true)
    public LongTimeInterval cleanUpTimePeriod = LongTimeInterval.HOUR;
}
