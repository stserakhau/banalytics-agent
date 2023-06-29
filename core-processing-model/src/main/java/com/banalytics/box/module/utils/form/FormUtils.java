package com.banalytics.box.module.utils.form;

import com.banalytics.box.api.integration.form.FormModel;
import com.banalytics.box.api.integration.form.annotation.UIComponent;
import com.banalytics.box.api.integration.form.annotation.UIExtension;
import com.banalytics.box.api.integration.form.annotation.UIExtensions;
import com.banalytics.box.module.utils.DontWriteToXml;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.MethodUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.BeansException;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.util.ReflectionUtils;
import org.xml.sax.Attributes;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;

@Slf4j
public class FormUtils {

    public static void buildConfig(StringBuilder sb, Object config) {
        BeanWrapper wrapper = PropertyAccessorFactory.forBeanPropertyAccess(config);
        wrapper.setAutoGrowNestedPaths(true);

        for (PropertyDescriptor pd : wrapper.getPropertyDescriptors()) {
            String propertyName = pd.getName();

            Method readMethod = pd.getReadMethod();
            DontWriteToXml tr = readMethod == null ? null : readMethod.getAnnotation(DontWriteToXml.class);
            boolean dontWriteToXml = tr != null;
            Method writeMethod = pd.getWriteMethod();
            if (writeMethod == null || dontWriteToXml) {
                continue;
            }

            Object val = wrapper.getPropertyValue(propertyName);
            String value = val == null ? "null" : val.toString();
            String escapedValue = StringEscapeUtils.escapeXml11(value);
            sb.append(' ').append(propertyName)
                    .append("=\"")
                    .append(escapedValue)
                    .append('"');
        }
    }

    public static void populate(Object obj, Attributes attributes) {
        BeanWrapper taskW = new BeanWrapperImpl(obj);
        for (int i = 0; i < attributes.getLength(); i++) {
            String propName = attributes.getQName(i);
            String propValue = attributes.getValue(i);
            if ("null".equals(propValue)) {
                propValue = null;
            } else {
                propValue = StringEscapeUtils.unescapeXml(propValue);
            }
            try {
                taskW.setPropertyValue(propName, propValue);
            } catch (BeansException e) {
                log.warn(e.getMessage());
            }
        }
    }

    public static FormModel describe(BeanFactory beanFactory, Object configuration) throws Exception {
        FormModel formModel = new FormModel();

        Class clazz = configuration.getClass();

        UIExtensions uiExtensions = (UIExtensions) clazz.getAnnotation(UIExtensions.class);
        if (uiExtensions != null) {
            for (UIExtension extension : uiExtensions.extensions()) {
                FormModel.UIExtensionDescriptor desc = new FormModel.UIExtensionDescriptor();
                desc.setType(extension.type());
                for (UIExtension.ExtensionConfig extensionConfig : extension.uiConfig()) {
                    desc.getConfiguration().put(extensionConfig.name(), extensionConfig.value());
                }
                formModel.getExtensionDescriptors().add(desc);
            }
        }

        BeanWrapper wrapper = PropertyAccessorFactory.forBeanPropertyAccess(configuration);
        wrapper.setAutoGrowNestedPaths(true);

        for (PropertyDescriptor pd : wrapper.getPropertyDescriptors()) {
            try {
                String propertyName = pd.getName();

                Field field = ReflectionUtils.findField(clazz, propertyName);
                if (field == null) {//case when uuid with singleton configuration - getter returns static value and no field;
                    continue;
                }

                UIComponent uiComponent = field.getAnnotation(UIComponent.class);
                if (uiComponent == null) {
                    continue;
                }
                FormModel.PropertyDescriptor descriptor = new FormModel.PropertyDescriptor();
                formModel.getPropertyDescriptors().put(
                        propertyName,
                        descriptor
                );
                Class<?> pt = pd.getPropertyType();
                {
                    descriptor.setDataType(FormModel.DataType.find(pt));
                }
                {
                    Object defaultValue = switch (uiComponent.type()) {
                        case password_input -> "";
                        default -> field.get(configuration);
                    };
                    descriptor.setDefaultValue(defaultValue == null ? null : String.valueOf(defaultValue));
                }

                {
                    descriptor.setIndex(uiComponent.index());
                    descriptor.setDependsOn(uiComponent.dependsOn());
                    if (uiComponent.required()) {
                        descriptor.getValidation().put("required", true);
                    }

                    if (uiComponent.uiValidation() != null) {
                        for (UIComponent.UIConfig config : uiComponent.uiValidation()) {
                            descriptor.getValidation().put(config.name(), config.value());
                        }
                    }

                    FormModel.UIComponentDescriptor uiDescriptor = descriptor.getUiComponentDescriptor();
                    uiDescriptor.setType(uiComponent.type());
                    Map<String, Object> uiConfiguration = uiDescriptor.getConfiguration();
                    {//enum possible values
                        List<Object> possibleValues = new ArrayList<>();

                        if (Enum.class.isAssignableFrom(pt)) { //if enum take the enum values
                            for (Object o : pt.getEnumConstants()) {
                                possibleValues.add(o.toString());
                            }
                        }

                        if (uiComponent.backendConfig() != null) {
                            for (UIComponent.BackendConfig config : uiComponent.backendConfig()) {
                                if (config.values() != null) {
                                    possibleValues.addAll(Arrays.asList(config.values()));
                                }
                                if (isNotEmpty(config.bean())) {
                                    Object bean = beanFactory.getBean(config.bean());
                                    String[] params = config.params();
                                    Object values = MethodUtils.invokeMethod(bean, config.method(), params);
                                    if (values instanceof Collection<?> coll) {
                                        possibleValues.addAll(coll);
                                    } else if (values instanceof Map<?, ?> map) {
                                        map.forEach((k, v) -> possibleValues.add(k.toString() + "~" + v.toString()));
                                    }
                                }
                            }
                        }

                        if (!possibleValues.isEmpty()) {
                            uiConfiguration.put("possibleValues", possibleValues);
                        }
                    }
                    if (uiComponent.uiConfig() != null) {
                        for (UIComponent.UIConfig config : uiComponent.uiConfig()) {
                            uiConfiguration.put(config.name(), config.value());
                        }
                    }
                }
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }
        return formModel;
    }

    public static Class<?>[] getInterfaces(Class<?> clazz) {
        List<Class<?>> interfaces = new ArrayList<>();

        do {
            Collections.addAll(interfaces, clazz.getInterfaces());
            clazz = clazz.getSuperclass();
        } while (clazz != Object.class);

        return interfaces.toArray(new Class[0]);
    }
}
