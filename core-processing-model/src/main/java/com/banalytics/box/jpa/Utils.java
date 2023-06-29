package com.banalytics.box.jpa;

import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Triple;
import org.hibernate.Criteria;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.internal.CriteriaImpl;
import org.hibernate.internal.FilterImpl;
import org.hibernate.internal.SessionImpl;
import org.hibernate.loader.OuterJoinLoader;
import org.hibernate.loader.criteria.CriteriaLoader;
import org.hibernate.loader.criteria.CriteriaQueryTranslator;
import org.hibernate.persister.entity.OuterJoinLoadable;
import org.springframework.data.domain.PageRequest;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class Utils {
    public static final String ASC = "asc";

    public static Criterion buildFilterCriterion(Class clazz, ExpressionNode root) {
        if (root == null) {
            return null;
        }
        Object value = root.value;
        if (value instanceof String) {
            String operation = (String) value;
            ExpressionNode left = root.left;
            ExpressionNode right = root.right;

            return ExpressionOperators
                    .valueOf(operation)
                    .operation()
                    .build(clazz, left, right);
        }
        throw new RuntimeException("Unknown case: " + root.toString());
    }


    public static void appendOrderCriterion(Map<String, String> order, Criteria toCriteria) {
        if (order != null && !order.isEmpty()) {
            // order index, field name, field value (asc or desc)
            List<Triple<Integer, String, String>> orders = new ArrayList<>();
            for (Map.Entry<String, String> se : order.entrySet()) {
                String asc = se.getValue();
                if (asc == null) {
                    continue;
                }
                if (se.getKey().contains(".")) {
                    // If an order param has an order index
                    String orderIndex = se.getKey().substring(0, se.getKey().indexOf("."));
                    String field = se.getKey().substring(se.getKey().indexOf(".") + 1);
                    if (NumberUtils.isNumber(orderIndex)) {
                        orders.add(new ImmutableTriple<>(Integer.valueOf(orderIndex), field, se.getValue()));
                    } else {
                        // if orderIndex is not a number it means that it is a part of compound field name.
                        orders.add(new ImmutableTriple<>(Integer.MAX_VALUE, se.getKey(), se.getValue()));
                    }
                } else {
                    // If an order param does not have an order index, they are going to the end
                    orders.add(new ImmutableTriple<>(Integer.MAX_VALUE, se.getKey(), se.getValue()));
                }
            }

            orders.sort(Comparator.comparing(Triple::getLeft));

            for (Triple<Integer, String, String> orderParam : orders) {
                String propertyName = orderParam.getMiddle();
                String sortType = orderParam.getRight();
                Order orderClause = ASC.equals(sortType) ? Order.asc(propertyName) : Order.desc(propertyName);
                toCriteria.addOrder(orderClause);
            }
        }
    }

    public static void applyPageNum(int pageNum, int pageSize, Criteria listCriteria) {
        if (pageNum >= 0 && pageSize > 0) {
            int firstRow = pageSize * pageNum;
            listCriteria
                    .setFirstResult(firstRow)
                    .setMaxResults(pageSize);
        }
    }

    public static String toSql(Criteria criteria) {
        try {
            CriteriaImpl c = (CriteriaImpl) criteria;
            SessionImpl s = (SessionImpl) c.getSession();
            SessionFactoryImplementor factory = (SessionFactoryImplementor) s.getSessionFactory();
            String[] implementors = factory.getImplementors(c.getEntityOrClassName());
            CriteriaLoader loader = new CriteriaLoader((OuterJoinLoadable) factory.getEntityPersister(implementors[0]), factory, c, implementors[0], s.getLoadQueryInfluencers());
            Field f = OuterJoinLoader.class.getDeclaredField("sql");
            f.setAccessible(true);
            String sql = (String) f.get(loader);
            Field fp = CriteriaLoader.class.getDeclaredField("translator");
            fp.setAccessible(true);
            CriteriaQueryTranslator translator = (CriteriaQueryTranslator) fp.get(loader);
            Object[] parameters = translator.getQueryParameters().getPositionalParameterValues();

            if (sql != null) {
                {//apply authorize filter
                    FilterImpl filter = (FilterImpl) s.getEnabledFilter("authorize");
                    for (Map.Entry<String, ?> entry : filter.getParameters().entrySet()) {
                        String k = entry.getKey();
                        Object v = entry.getValue();
                        sql = sql.replaceAll(":authorize." + k, processValue(v));
                    }
                }
                int fromPosition = sql.indexOf(" from ");
                sql = "SELECT * " + sql.substring(fromPosition);

                if (parameters != null && parameters.length > 0) {
                    for (Object val : parameters) {
                        String value = processValue(val);
                        sql = sql.replaceFirst("\\?", value);
                    }
                }
            }
            return sql;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static String processValue(Object val) {
        if (val instanceof Boolean) {
            return ((Boolean) val) ? "true" : "false";
        } else if (val instanceof LocalDateTime) {
            return "'" + ((LocalDateTime) val).toString() + "'";
        } else if (val instanceof String) {
            return "'" + val + "'";
        } else if (val instanceof Number) {
            return val.toString();
        } else if (val instanceof List) {
            return val.toString().replaceAll("\\[", "").replaceAll("\\]", "");
        }

        return val.toString();
    }
}

