package com.banalytics.box.module.whatsapp;

import com.banalytics.box.api.integration.form.ComponentType;
import com.banalytics.box.api.integration.form.annotation.UIComponent;
import com.banalytics.box.module.AbstractConfiguration;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class WhatsAppBotConfiguration extends AbstractConfiguration {
    @UIComponent(index = 20, type = ComponentType.text_input, required = true)
    public String alias;

    @UIComponent(index = 30, type = ComponentType.password_input, required = true)
    public String pinCode = "";
}
