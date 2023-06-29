package com.banalytics.box.module.model.discovery;

public abstract class LocalDevice {
    public String name;
    public String alternativeName;

    public boolean isCompleted() {
        return name != null && alternativeName != null;
    }
}