package com.banalytics.box.module;

import java.util.Set;

public interface PropertyValuesProvider {
    Set<String> provideValues(String propertyName);
}
