package com.banalytics.box.filter;

import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.beanutils.PropertyUtils;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public enum FilterOperators {
    or(3, (obj, left, right) -> left.applyFilter(obj) || right.applyFilter(obj)),
    and(2, (obj, left, right) -> left.applyFilter(obj) && right.applyFilter(obj)),
    eq(1, (obj, left, right) -> {
        try {
            String propertyName = (String) left.value;
            Object propertyValue = PropertyUtils.getProperty(obj, propertyName);
            if (propertyValue instanceof String) {
                propertyValue = ((String) propertyValue).replaceAll("[\\(\\)]", "");
            }
            if (propertyValue != null) {
                Object val = right != null ? right.value : null;
                val = getCustomTypedValue(obj.getClass(), (String) left.value, val);
                if (propertyValue.getClass().isEnum()) {
                    return val.equals(((Enum) propertyValue).name());
                } else {
                    return val.equals(propertyValue);
                }
            } else {
                return false;
            }
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }),
    ge(1, (obj, left, right) -> {
        try {
            String propertyName = (String) left.value;
            Object propertyValue = PropertyUtils.getProperty(obj, propertyName);
            if (propertyValue instanceof String) {
                propertyValue = ((String) propertyValue).replaceAll("[\\(\\)]", "");
            }
            if (propertyValue != null) {
                Object val = right != null ? right.value : null;
                val = getCustomTypedValue(obj.getClass(), (String) left.value, val);
                if (val == null) {
                    return false;
                }
                if (propertyValue instanceof Number pv && val instanceof Number v) {
                    return pv.doubleValue() >= v.doubleValue();
                }
                return false;
            } else {
                return false;
            }
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }),
    gt(1, (obj, left, right) -> {
        try {
            String propertyName = (String) left.value;
            Object propertyValue = PropertyUtils.getProperty(obj, propertyName);
            if (propertyValue instanceof String) {
                propertyValue = ((String) propertyValue).replaceAll("[\\(\\)]", "");
            }
            if (propertyValue != null) {
                Object val = right != null ? right.value : null;
                val = getCustomTypedValue(obj.getClass(), (String) left.value, val);
                if (val == null) {
                    return false;
                }
                if (propertyValue instanceof Number pv && val instanceof Number v) {
                    return pv.doubleValue() > v.doubleValue();
                }
                return false;
            } else {
                return false;
            }
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }),
    le(1, (obj, left, right) -> {
        try {
            String propertyName = (String) left.value;
            Object propertyValue = PropertyUtils.getProperty(obj, propertyName);
            if (propertyValue instanceof String) {
                propertyValue = ((String) propertyValue).replaceAll("[\\(\\)]", "");
            }
            if (propertyValue != null) {
                Object val = right != null ? right.value : null;
                val = getCustomTypedValue(obj.getClass(), (String) left.value, val);
                if (val == null) {
                    return false;
                }
                if (propertyValue instanceof Number pv && val instanceof Number v) {
                    return pv.doubleValue() <= v.doubleValue();
                }
                return false;
            } else {
                return false;
            }
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }),
    lt(1, (obj, left, right) -> {
        try {
            String propertyName = (String) left.value;
            Object propertyValue = PropertyUtils.getProperty(obj, propertyName);
            if (propertyValue instanceof String) {
                propertyValue = ((String) propertyValue).replaceAll("[\\(\\)]", "");
            }
            if (propertyValue != null) {
                Object val = right != null ? right.value : null;
                val = getCustomTypedValue(obj.getClass(), (String) left.value, val);
                if (val == null) {
                    return false;
                }
                if (propertyValue instanceof Number pv && val instanceof Number v) {
                    return pv.doubleValue() < v.doubleValue();
                }
                return false;
            } else {
                return false;
            }
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }),
    contains(1, (obj, left, right) -> {
        try {
            String propertyName = (String) left.value;
            Object propertyValue = PropertyUtils.getProperty(obj, propertyName);

            if (propertyValue != null) {
                Object val = right != null ? right.value : null;
                if (propertyValue instanceof Collection) {
                    for (Object o : ((Collection) propertyValue)) {
                        if (o.toString().equals(val.toString())) {
                            return true;
                        }
                    }
                    return false;
                } else if (propertyValue instanceof Map) {
                    Map map = (Map) propertyValue;
                    for (Object o : map.keySet()) {
                        if (o.toString().equals(val)) {
                            return true;
                        }
                    }
                    return false;
                } else {
                    val = getCustomTypedValue(obj.getClass(), (String) left.value, val);

                    return propertyValue.toString().contains(val.toString());
                }
            } else {
                return false;
            }
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }),
    in(1, (obj, left, right) -> {
        try {
            Collection values = getCustomTypedValueCollection(obj.getClass(), (String) left.value, right.value);
            String propertyName = (String) left.value;
            Object propertyValue = PropertyUtils.getProperty(obj, propertyName);
            if (Object[].class.isAssignableFrom(propertyValue.getClass())) {
                Object[] vals = (Object[]) propertyValue;
                for (Object val : vals) {
                    if (values.contains(val)) {
                        return true;
                    }
                }
                return false;
            } else if (Collection.class.isAssignableFrom(propertyValue.getClass())) {
                Collection vals = (Collection) propertyValue;
                for (Object val : vals) {
                    if (values.contains(val)) {
                        return true;
                    }
                }
                return false;
            } else {
                return values.contains(propertyValue);
            }
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }),
    nop(1, (obj, left, right) -> true);

    private final int priority;

    private final Operation operation;

    FilterOperators(int priority, Operation operation) {
        this.priority = priority;
        this.operation = operation;
    }

    public Operation operation() {
        return operation;
    }

    public static Map<String, Integer> toMap() {
        Map<String, Integer> res = new HashMap<>();
        for (FilterOperators op : FilterOperators.values()) {
            res.put(op.name(), op.priority);
        }

        return res;
    }

    public interface Operation {
        boolean apply(Object obj, FilterNode left, FilterNode right);
    }

    private static Object getCustomTypedValue(Class clazz, String propertyName, Object propertyValue) {
        try {
            String[] path = propertyName.split("\\.");

            for (String pn : path) {
                clazz = clazz.getDeclaredField(pn).getType();
            }
            return ConvertUtils.convert(propertyValue, clazz);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException("Can not determine type for field " + propertyName + " in class " + clazz.getCanonicalName(), e);
        }
    }

    private static <T> Collection<T> getCustomTypedValueCollection(Class<T> clazz, String propertyName, Object propertyValue) {//hot fix of the hotfix
        if (propertyValue instanceof Collection) {
            return (Collection) propertyValue;
        }

        return (Collection) getCustomTypedValue(clazz, propertyName, propertyValue);
    }
}
