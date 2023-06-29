package com.banalytics.box.jpa;

import org.apache.commons.beanutils.ConvertUtils;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Restrictions;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static com.banalytics.box.jpa.Utils.buildFilterCriterion;


public enum ExpressionOperators {
    or(3, (clazz, left, right) -> {
        return Restrictions.or(
                buildFilterCriterion(clazz, left),
                buildFilterCriterion(clazz, right)
        );
    }),
    and(2, (clazz, left, right) -> {
        return Restrictions.and(
                buildFilterCriterion(clazz, left),
                buildFilterCriterion(clazz, right)
        );
    }),
    eq(1, (clazz, left, right) -> {
        Object val = right != null ? right.value : null;
        val = getCustomTypedValue(clazz, (String) left.value, val);
        return Restrictions.eqOrIsNull((String) left.value, val);
    }),
    neq(1, (clazz, left, right) -> {
        Object val = right != null ? right.value : null;
        val = getCustomTypedValue(clazz, (String) left.value, val);
        return Restrictions.neOrIsNotNull((String) left.value, val);
    }),
    contains(1, (clazz, left, right) -> {
        String val = right != null ? (right.value == null ? null : right.value.toString()) : null;
        boolean useLike = val != null && (val.indexOf('%') > -1 || val.indexOf('*') > -1);
        if (useLike) {
            val = val.replace('*', '%');
            return Restrictions.ilike((String) left.value, val, MatchMode.EXACT);
        } else {
            return Restrictions.eqOrIsNull((String) left.value, val);
        }
    }),
    in(1, (clazz, left, right) -> {
        Collection values = right != null ? (Collection) right.value : null;
        values = (Collection) getCustomTypedValueCollection(clazz, (String) left.value, values);
        return Restrictions.in((String) left.value, values);
    }),
    nop(1, (clazz, left, right) -> {
        return Restrictions.sqlRestriction("1=1");
    }),
    nin(1, (clazz, left, right) -> {
        Collection values = right != null ? (Collection) right.value : null;
        values = (Collection) getCustomTypedValueCollection(clazz, (String) left.value, values);
        return Restrictions.not(Restrictions.in((String) left.value, values));
    }),
    le(1, (clazz, left, right) -> {
        Object val = right != null ? right.value : null;
        val = getCustomTypedValue(clazz, (String) left.value, val);
        return Restrictions.le((String) left.value, val);
    }),
    ge(1, (clazz, left, right) -> {
        Object val = right != null ? right.value : null;
        val = getCustomTypedValue(clazz, (String) left.value, val);
        return Restrictions.ge((String) left.value, val);
    }),
    gt(1, (clazz, left, right) -> {
        Object val = right != null ? right.value : null;
        val = getCustomTypedValue(clazz, (String) left.value, val);
        return Restrictions.gt((String) left.value, val);
    }),
    lt(1, (clazz, left, right) -> {
        Object val = right != null ? right.value : null;
        val = getCustomTypedValue(clazz, (String) left.value, val);
        return Restrictions.lt((String) left.value, val);
    }),
    between(1, (clazz, left, right) -> {
        Collection values = right != null ? (Collection) right.value : null;
        values = (Collection) getCustomTypedValueCollection(clazz, (String) left.value, values);
        Iterator it = values.iterator();
        return Restrictions.between((String) left.value, it.next(), it.next());
    }),
    isNull(1, (clazz, left, right) -> {
        return Restrictions.isNull((String) left.value);
    }),
    isNotNull(1, (clazz, left, right) -> {
        return Restrictions.isNotNull((String) left.value);
    });

    private final int priority;

    private final Operation operation;

    ExpressionOperators(int priority, Operation operation) {
        this.priority = priority;
        this.operation = operation;
    }

    public Operation operation() {
        return operation;
    }

    public static Map<String, Integer> toMap() {
        Map<String, Integer> res = new HashMap<>();
        for (ExpressionOperators op : ExpressionOperators.values()) {
            res.put(op.name(), op.priority);
        }

        return res;
    }

    public interface Operation {
        Criterion build(Class clazz, ExpressionNode left, ExpressionNode right);
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

    private static Object getCustomTypedValueCollection(Class clazz, String propertyName, Object propertyValue) {//hot fix of the hotfix
        if (propertyValue instanceof Collection) {
            return propertyValue;
        }

        return getCustomTypedValue(clazz, propertyName, propertyValue);
    }
}
