package com.banalytics.box.module.media.task.tracking;

import com.banalytics.box.api.integration.form.annotation.UIComponent;
import com.banalytics.box.module.AbstractConfiguration;
import lombok.Getter;
import lombok.Setter;

import static com.banalytics.box.api.integration.form.ComponentType.*;

@Getter
@Setter
public abstract class AbstractObjectTrackerTaskConfig extends AbstractConfiguration {

    @UIComponent(
            index = 10,
            type = roi_capture,
            restartOnChange = true
    )
    public String roiBox;


    @UIComponent(index = 20, type = int_input, required = true,
            restartOnChange = true,
            uiConfig = {
                    @UIComponent.UIConfig(name = "min", value = "0"),
                    @UIComponent.UIConfig(name = "max", value = "5000")
            })
    public int stunTimeoutMillis = 100;
}
