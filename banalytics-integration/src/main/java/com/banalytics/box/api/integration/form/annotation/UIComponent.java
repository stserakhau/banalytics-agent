package com.banalytics.box.api.integration.form.annotation;

import com.banalytics.box.api.integration.form.ComponentType;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target(FIELD)
@Retention(RUNTIME)
public @interface UIComponent {
    int index();

    ComponentType type();

    boolean required() default false;

    UIConfig[] uiValidation() default {};

    UIConfig[] uiConfig() default {};

    BackendConfig[] backendConfig() default {};

    String[] dependsOn() default {};

    boolean restartOnChange() default false;

    @Retention(RUNTIME)
    @interface UIConfig {
        String name();

        String value();
    }

    @Retention(RUNTIME)
    @interface BackendConfig {
        String[] values() default {};

        String bean() default "";

        String method() default "";

        String[] params() default {};
    }
}
