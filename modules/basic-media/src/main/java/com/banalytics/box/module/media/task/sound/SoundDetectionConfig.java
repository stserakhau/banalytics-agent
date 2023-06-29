package com.banalytics.box.module.media.task.sound;

import com.banalytics.box.api.integration.form.ComponentType;
import com.banalytics.box.api.integration.form.annotation.UIComponent;
import com.banalytics.box.api.integration.form.annotation.UIExtension;
import com.banalytics.box.api.integration.form.annotation.UIExtensions;
import com.banalytics.box.module.AbstractConfiguration;
import lombok.Getter;
import lombok.Setter;

import static com.banalytics.box.api.integration.form.ComponentType.checkbox;
import static com.banalytics.box.api.integration.form.FormModel.UIExtensionDescriptor.ExtensionType.histogram;

@Getter
@Setter
@UIExtensions(
        extensions = {
                @UIExtension(type = histogram, uiConfig = {
                        @UIExtension.ExtensionConfig(name = "amountOfRanges", value = "specterSensitivity"),
                        @UIExtension.ExtensionConfig(name = "thresholdParamName", value = "threshold")
                })
        }
)
public class SoundDetectionConfig extends AbstractConfiguration {

    @UIComponent(index = 20, type = checkbox, required = true)
    public boolean debug = false;

    /**
     * Threshold for triggering level of the noise
     */
    @UIComponent(
            index = 30,
            type = ComponentType.int_input,
            uiConfig = {
                    @UIComponent.UIConfig(name = "step", value = "0.1"),
                    @UIComponent.UIConfig(name = "min", value = "0.1"),
                    @UIComponent.UIConfig(name = "max", value = "10000")
            }
    )
    public double threshold = 2.5;

    @UIComponent(
            index = 35,
            type = ComponentType.int_input,
            uiConfig = {
                    @UIComponent.UIConfig(name = "min", value = "10"),
                    @UIComponent.UIConfig(name = "max", value = "100")
            }, restartOnChange = true
    )
    public int specterSensitivity = 40;

    /**
     * Length of FFT history for calculation average level of the noise for triggering the events
     */
    @UIComponent(index = 40, type = ComponentType.int_input, required = true,
            uiConfig = {
                    @UIComponent.UIConfig(name = "min", value = "1"),
                    @UIComponent.UIConfig(name = "max", value = "20")
            },
            restartOnChange = true
    )
    public int historyLength = 10;

    /**
     * Enable adaptation to the periodically splashes of the noise, otherwise use average noise level.
     * When enabled, pushing to history only noises which are above the average noice
     */
    @UIComponent(index = 50, type = ComponentType.checkbox, required = true,
            uiConfig = {
                    @UIComponent.UIConfig(name = "min", value = "0"),
                    @UIComponent.UIConfig(name = "max", value = "60")
            }
    )
    public boolean applyThresholdExceed = false;

    /**
     * If periodically noise is out it's time to adoptation to the new level.
     */
    @UIComponent(index = 60, type = ComponentType.int_input, required = true,
            dependsOn = {"applyThresholdExceed"},
            uiConfig = {
                    @UIComponent.UIConfig(name = "enableCondition", value = "''+form.applyThresholdExceed === 'true'"),
                    @UIComponent.UIConfig(name = "min", value = "0"),
                    @UIComponent.UIConfig(name = "max", value = "60")
            },
            restartOnChange = true
    )
    public int speedOfAccustomSec = 1;
}
