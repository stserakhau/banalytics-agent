package com.banalytics.box.module.system.script;

import com.banalytics.box.api.integration.form.ComponentType;
import com.banalytics.box.api.integration.form.annotation.UIComponent;
import com.banalytics.box.module.AbstractConfiguration;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ScriptActionConfiguration extends AbstractConfiguration {
    @UIComponent(
            index = 10,
            type = ComponentType.text_input,
            required = true
    )
    public String title;

    @UIComponent(
            index = 20,
            type = ComponentType.text_area,
            required = true,
            uiConfig = {
                    @UIComponent.UIConfig(name = "rows", value = "10"),
            }
    )
    public String script;
}
