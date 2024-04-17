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
    })
    public String serialPort;

    @UIComponent(index = 30, type = ComponentType.int_input, required = true, uiConfig = {
            @UIComponent.UIConfig(name = "min", value = "10"),
            @UIComponent.UIConfig(name = "max", value = "100")
    })
    public int checkCommandResponseInterval = 50;

    @UIComponent(index = 40, type = ComponentType.int_input, required = true, uiConfig = {
            @UIComponent.UIConfig(name = "min", value = "10"),
            @UIComponent.UIConfig(name = "max", value = "100")
    })
    public int stateRequestCycleTickInterval = 10;

    @UIComponent(index = 50, type = ComponentType.int_input, required = true, uiConfig = {
            @UIComponent.UIConfig(name = "min", value = "1"),
            @UIComponent.UIConfig(name = "max", value = "100")
    })
    public int imuRequestTik = 5;

    @UIComponent(index = 60, type = ComponentType.int_input, required = true, uiConfig = {
            @UIComponent.UIConfig(name = "min", value = "1"),
            @UIComponent.UIConfig(name = "max", value = "100")
    })
    public int analogRequestTik = 100;

    @UIComponent(index = 70, type = ComponentType.int_input, required = true, uiConfig = {
            @UIComponent.UIConfig(name = "min", value = "1"),
            @UIComponent.UIConfig(name = "max", value = "100")
    })
    public int attitudeRequestTik = 5;

    @UIComponent(index = 80, type = ComponentType.int_input, required = true, uiConfig = {
            @UIComponent.UIConfig(name = "min", value = "1"),
            @UIComponent.UIConfig(name = "max", value = "100")
    })
    public int altitudeRequestTik = 5;

    @UIComponent(index = 90, type = ComponentType.int_input, required = true, uiConfig = {
            @UIComponent.UIConfig(name = "min", value = "0"),
            @UIComponent.UIConfig(name = "max", value = "32000")
    })
    public int engineStop = 0;

    @UIComponent(index = 100, type = ComponentType.int_input, required = true, uiConfig = {
            @UIComponent.UIConfig(name = "min", value = "0"),
            @UIComponent.UIConfig(name = "max", value = "32000")
    })
    public int engineTest = 1008;

    @UIComponent(index = 110, type = ComponentType.int_input, required = true, uiConfig = {
            @UIComponent.UIConfig(name = "min", value = "0"),
            @UIComponent.UIConfig(name = "max", value = "32000")
    })
    public int engineStart = 1009;

    @UIComponent(index = 120, type = ComponentType.int_input, required = true, uiConfig = {
            @UIComponent.UIConfig(name = "min", value = "0"),
            @UIComponent.UIConfig(name = "max", value = "32000")
    })
    public int engineMax = 2000;

    @UIComponent(index = 130, type = ComponentType.int_input, required = true, restartOnChange = true,  uiConfig = {
            @UIComponent.UIConfig(name = "min", value = "0"),
            @UIComponent.UIConfig(name = "max", value = "500")
    })
    public int trimmerPower = 50;

    @UIComponent(index = 150, type = ComponentType.text_input, required = true, restartOnChange = true)
    public String pidXTrimmerValues = "0;0;0";

    @UIComponent(index = 160, type = ComponentType.text_input, required = true, restartOnChange = true)
    public String pidYTrimmerValues = "0;0;0";

    @UIComponent(index = 170, type = ComponentType.text_input, required = true, restartOnChange = true)
    public String pidZTrimmerValues = "0;0;0";

    @UIComponent(index = 180, type = ComponentType.int_input, required = true, restartOnChange = true,  uiConfig = {
            @UIComponent.UIConfig(name = "min", value = "0"),
            @UIComponent.UIConfig(name = "max", value = "180")
    })
    public int disarmXYAngle = 15;
}
