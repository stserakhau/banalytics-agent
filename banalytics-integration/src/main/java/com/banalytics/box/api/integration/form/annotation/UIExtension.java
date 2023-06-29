package com.banalytics.box.api.integration.form.annotation;

import com.banalytics.box.api.integration.form.FormModel;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target(FIELD)
@Retention(RUNTIME)
public @interface UIExtension {
    int index() default 0;

    FormModel.UIExtensionDescriptor.ExtensionType type();

    ExtensionConfig[] uiConfig() default {};

    @Retention(RUNTIME)
    @interface ExtensionConfig {
        String name();

        String value();
    }
}
