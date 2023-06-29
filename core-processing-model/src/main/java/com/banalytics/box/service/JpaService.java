package com.banalytics.box.service;

import com.banalytics.box.TimeUtil;
import com.banalytics.box.jpa.ExpressionNode;
import com.banalytics.box.jpa.ExpressionTreeBuilder;
import com.banalytics.box.jpa.Utils;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class JpaService {
    private final EntityManager entityManager;

    @Transactional(propagation = Propagation.REQUIRED)
    public void persistEntity(Object entity) {
        entityManager.persist(entity);
    }

    public EntityManager getEntityManager() {
        return entityManager;
    }

    public boolean isOpen() {
        return entityManager.isOpen();
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public <T> List<T> query(String query, Class<T> clazz, Map<String, ?> params) {
        TypedQuery<T> q = entityManager.createQuery(query, clazz);
        if (params != null) {
            params.forEach(q::setParameter);
        }
        return q.getResultList();
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public <T> List<T> expressionQuery(int pageNum, int pageSize, String expression, Map<String, String> orderSpecification, Class<T> clazz) {
        Session session = entityManager.unwrap(Session.class);
        session = session.getSession();
        Criteria listCriteria = session.createCriteria(clazz);

        Utils.applyPageNum(pageNum, pageSize, listCriteria);

        if (StringUtils.isNotEmpty(expression)) {
            ExpressionNode exprNode = ExpressionTreeBuilder.parse(expression, clazz);
            Criterion filterCriteria = Utils.buildFilterCriterion(clazz, exprNode);
            listCriteria.add(filterCriteria);
        }

        if (orderSpecification != null && !orderSpecification.isEmpty()) {
            Utils.appendOrderCriterion(orderSpecification, listCriteria);
        }

        return listCriteria.list();
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void cleanUpEventHistory(LocalDateTime expiredFrom) {
        entityManager
                .createQuery("delete from EventStore where dateTime < :expiredFrom")
                .setParameter("expiredFrom", expiredFrom)
                .executeUpdate();
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void cleanUpExpiredTokens() {
        entityManager
                .createQuery("delete from TokenEntity where expirationTime < :now")
                .setParameter("now", TimeUtil.currentTimeInServerTz())
                .executeUpdate();
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void clearTokensByObjectReference(String objectReference) {
        entityManager
                .createQuery("delete from TokenEntity where objectReference = :ref")
                .setParameter("ref", objectReference)
                .executeUpdate();
    }


    @Transactional(propagation = Propagation.REQUIRED)
    public void removeEntity(String id, Class<?> entityClass) {
        Object entity = entityManager.find(entityClass, id);
        entityManager.remove(entity);
    }

    public <T> T findEntity(String id, Class<T> entityClass) {
        return entityManager.find(entityClass, id);
    }
}
