package com.banalytics.box.module.telegram;

import com.banalytics.box.api.integration.form.ComponentType;
import com.banalytics.box.api.integration.form.annotation.UIComponent;
import com.banalytics.box.module.AbstractConfiguration;
import com.banalytics.box.module.ITitle;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class TelegramBotConfiguration extends AbstractConfiguration implements ITitle {

    @UIComponent(index = 1, type = ComponentType.text_input, required = true)
    public String title;

    @UIComponent(index = 2, type = ComponentType.password_input, restartOnChange = true)
    public String botToken;

    @UIComponent(index = 3, type = ComponentType.password_input, required = true)
    public String pinCode = "";
}
