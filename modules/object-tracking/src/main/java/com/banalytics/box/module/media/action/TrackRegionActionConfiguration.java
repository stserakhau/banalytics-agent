package com.banalytics.box.module.media.action;

import com.banalytics.box.api.integration.form.ComponentType;
import com.banalytics.box.api.integration.form.annotation.UIComponent;
import com.banalytics.box.module.AbstractConfiguration;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class TrackRegionActionConfiguration extends AbstractConfiguration {
    @UIComponent(
            index = 100,
            type = ComponentType.drop_down,
            required = true,
            backendConfig = {
                    @UIComponent.BackendConfig(bean = "taskService", method = "findByStandard", params = {"com.banalytics.box.module.media.task.tracking.ObjectTracker"})
            },
            restartOnChange = true
    )
    public UUID objectTrackingTask;

    @UIComponent(index = 110, type = ComponentType.checkbox, required = true)
    public boolean trackCentroid = false;
}
