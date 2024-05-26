package com.banalytics.box.module.toys.quadrocopter;

import com.banalytics.box.api.integration.form.ComponentType;
import com.banalytics.box.api.integration.form.annotation.UIComponent;
import com.banalytics.box.module.AbstractConfiguration;
import com.banalytics.box.module.constants.DateFormat;
import com.banalytics.box.module.constants.PenColor;
import com.banalytics.box.module.constants.PenFont;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class QuadrocopterWatermarkConfig extends AbstractConfiguration {

    @UIComponent(
            index = 10,
            type = ComponentType.drop_down,
            required = true,
            uiConfig = {
                    @UIComponent.UIConfig(name = "sort", value = "asc")
            },
            backendConfig = {
                    @UIComponent.BackendConfig(bean = "taskService", method = "findByStandard", params = {"com.banalytics.box.module.toys.quadrocopter.QuadrocopterThing"})
            },
            restartOnChange = true
    )
    public UUID quadrocopterThingUuid;

    @UIComponent(index = 20, type = ComponentType.int_input, required = true)
    public int centroidShiftX = 0;

    @UIComponent(index = 30, type = ComponentType.int_input, required = true)
    public int centroidShiftY = 0;

    @UIComponent(index = 70, type = ComponentType.drop_down, required = true, restartOnChange = true)
    public DateFormat dateFormat = DateFormat.DDMMYYYY_HHMMSS;

    @UIComponent(index = 80, type = ComponentType.checkbox, required = true)
    public boolean invertColor = false;

    @UIComponent(index = 90, type = ComponentType.drop_down, required = true, restartOnChange = true)
    public PenFont penFont = PenFont.FONT_HERSHEY_COMPLEX;

    @UIComponent(index = 100, type = ComponentType.drop_down, required = true, restartOnChange = true)
    public PenColor penColor = PenColor.WHITE;

    @UIComponent(index = 110, type = ComponentType.int_input, required = true, restartOnChange = true,
            uiConfig = {
                    @UIComponent.UIConfig(name = "step", value = "0.1"),
                    @UIComponent.UIConfig(name = "min", value = "0.3"),
                    @UIComponent.UIConfig(name = "max", value = "2")
            })
    public double fontScale = 0.6;

    @UIComponent(index = 120, type = ComponentType.int_input, required = true, restartOnChange = true,
            uiConfig = {
                    @UIComponent.UIConfig(name = "min", value = "1"),
                    @UIComponent.UIConfig(name = "max", value = "10")
            })
    public int fontThickness = 1;
}
