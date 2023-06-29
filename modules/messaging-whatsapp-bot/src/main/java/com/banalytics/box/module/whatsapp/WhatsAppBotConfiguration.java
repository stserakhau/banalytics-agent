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
    public static UUID WHATS_APP_UUID = UUID.fromString("00000000-0000-0000-0000-000000000007");

    @Override
    public UUID getUuid() {
        return WHATS_APP_UUID;
    }

    @UIComponent(index = 20, type = ComponentType.password_input, required = true)
    public String pinCode = "";
}
