package com.banalytics.box.api.integration.form;

import lombok.Getter;
import lombok.Setter;

import java.util.*;

@Getter
@Setter
public class FormModel {
    List<UIExtensionDescriptor> extensionDescriptors = new ArrayList<>();

    Map<String, PropertyDescriptor> propertyDescriptors = new HashMap<>();

    @Getter
    @Setter
    public static class PropertyDescriptor {
        int index;
        String[] dependsOn;
        DataType dataType;
        String defaultValue;
        Map<String, Object> validation = new HashMap<>();
        UIComponentDescriptor uiComponentDescriptor = new UIComponentDescriptor();
    }

    @Getter
    @Setter
    public static class UIExtensionDescriptor {
        ExtensionType type;
        Map<String, Object> configuration = new HashMap<>();

        public enum ExtensionType {
            histogram
        }
    }

    @Getter
    @Setter
    public static class UIComponentDescriptor {
        ComponentType type;
        Map<String, Object> configuration = new HashMap<>();
    }

    public enum DataType {
        bool(Set.of(Boolean.class, boolean.class)),
        string(Set.of(String.class, UUID.class)),
        number_int(Set.of(byte.class, short.class, int.class, long.class, Byte.class, Short.class, Integer.class, Long.class)),
        number_float(Set.of(float.class, double.class, Float.class, Double.class)),
        array(Set.of(Set.class));

        public final Set<Class<?>> associatedClasses;

        DataType(Set<Class<?>> associatedClasses) {
            this.associatedClasses = associatedClasses;
        }

        public static DataType find(Class<?> clazz) {
            if (clazz.isArray() || Collection.class.isAssignableFrom(clazz)) {
                return array;
            }
            if (clazz.isEnum()) {
                return string;
            }
            for (DataType dt : DataType.values()) {
                if (dt.associatedClasses.contains(clazz)) {
                    return dt;
                }
            }
            throw new RuntimeException("Class " + clazz + " is not supported in form model");
        }
    }
}
