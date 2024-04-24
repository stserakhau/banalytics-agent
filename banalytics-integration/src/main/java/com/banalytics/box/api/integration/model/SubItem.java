package com.banalytics.box.api.integration.model;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target(TYPE)
@Retention(RUNTIME)
public @interface SubItem {
    Class<?>[] of();

    String group() default "default";
    boolean singleton() default false;
}
