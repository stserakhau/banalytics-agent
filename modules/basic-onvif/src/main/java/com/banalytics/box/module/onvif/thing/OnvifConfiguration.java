package com.banalytics.box.module.onvif.thing;

import com.banalytics.box.api.integration.form.ComponentType;
import com.banalytics.box.api.integration.form.annotation.UIComponent;
import com.banalytics.box.module.AbstractConfiguration;
import com.banalytics.box.module.IUuid;
import lombok.Getter;
import lombok.Setter;
import org.onvif.ver10.schema.StreamType;
import org.onvif.ver10.schema.TransportProtocol;

import java.util.UUID;

@Getter
@Setter
public class OnvifConfiguration extends AbstractConfiguration implements IUuid {
    @UIComponent(index = 5, type = ComponentType.text_input_readonly)
    public String mac = "";

    @UIComponent(index = 10, type = ComponentType.text_input, required = true)
    public String title = "";

    @UIComponent(index = 20, type = ComponentType.text_input, required = true,
            uiValidation = {
                    @UIComponent.UIConfig(
                            name = "regex",
                            value = "^(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$|^(([a-zA-Z0-9]|[a-zA-Z0-9][a-zA-Z0-9\\-]*[a-zA-Z0-9])\\.)+([A-Za-z]|[A-Za-z][A-Za-z0-9\\-]*[A-Za-z0-9])$"
                    ),
                    @UIComponent.UIConfig(
                            name = "title",
                            value = "host.validation.title"
                    )
            }, restartOnChange = true
    )
    public String host;

    @UIComponent(index = 30, type = ComponentType.int_input, required = true,
            uiConfig = {
                    @UIComponent.UIConfig(name = "min", value = "1"),
                    @UIComponent.UIConfig(name = "max", value = "65535")
            }, restartOnChange = true
    )
    public int port = 80;

    @UIComponent(index = 40, type = ComponentType.text_input, restartOnChange = true)
    public String username;

    @UIComponent(index = 50, type = ComponentType.password_input, restartOnChange = true)
    public String password;

    @UIComponent(index = 60, type = ComponentType.drop_down, required = true, restartOnChange = true)
    public StreamType streamType = StreamType.RTP_UNICAST;

    @UIComponent(index = 70, type = ComponentType.drop_down, required = true, restartOnChange = true)
    public TransportProtocol transportProtocol = TransportProtocol.RTSP;

    @UIComponent(index = 80, type = ComponentType.int_input,
            uiConfig = {
                    @UIComponent.UIConfig(name = "min", value = "0"),
                    @UIComponent.UIConfig(name = "max", value = "65535")
            }, restartOnChange = true
    )
    public Integer overrideRtspPort;

//    @UIComponent(index = 90, type = ComponentType.drop_down, required = true, restartOnChange = true)
//    public TimeType timeType = TimeType.LOCAL_UTC;
//
//    public enum TimeType {
//        CAMERA_UTC, CAMERA_UTC_WITH_MILLIS, LOCAL_UTC
//    }
}
