package com.banalytics.box.module;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

public class ExecutionContext {
    public enum GlobalVariables {
        SOURCE_TASK_UUID,

        VIDEO_MOTION_DETECTED,
        AUDIO_MOTION_DETECTED,

        LIST_OF_MAT,

        CALCULATED_FRAME_RATE,
        VIDEO_KEY_FRAME
    }

    private final Map<String, Object> variables = new HashMap<>(50);

    {
        setVar(LocalDateTime.class, LocalDateTime.now());
    }

    public Map<String, Class<?>> variablesTypes() {
        Map<String, Class<?>> result = new HashMap<>(variables.size());
        variables.forEach((k, v) -> result.put(k, v == null ? null : v.getClass()));
        return result;
    }

    public Map<String, Object> variables() {
        return this.variables;
    }

    public void clear() {
        variables.clear();
    }

    public <T> void setVar(String name, T value) {
        variables.put(name, value);
    }

    public <T> void setVar(Enum<?> name, T value) {
        variables.put(name.name(), value);
    }

    public <T> void setVar(GlobalVariables var, T value) {
        variables.put(var.name(), value);
    }

    public <T> void setVar(Class<T> clazz, T value) {
        variables.put(clazz.getName(), value);
    }

    public <T> T getVar(String name) {
        return (T) variables.get(name);
    }

    public <T> T getVar(Enum<?> name) {
        return (T) variables.get(name.name());
    }

    public <T> T getVar(GlobalVariables var) {
        return (T) variables.get(var.name());
    }

    public <T> T getVar(GlobalVariables var, T def) {
        T val = (T) variables.get(var.name());
        return val == null ? def : val;
    }

    public <T> T getVar(Class<T> clazz) {
        return (T) variables.get(clazz.getName());
    }
}
