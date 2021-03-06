package com.valor.mercury.manager.dao;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.*;
import org.hibernate.transform.Transformers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.*;
import java.util.Map.Entry;

/**
 * Hibernate DAO implementation
 */
@Transactional(value = "dispatchTx", timeout = 10)
public abstract class HibernateBaseDao {

    @Autowired
    @Qualifier("localSessionFactory")
    private SessionFactory sessionFactory;

    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }


    public Session currentSession() {
        return sessionFactory.getCurrentSession();
    }


    /**
     * Flush and clear the session.
     */
    public void flushAndClear() {
        Session session = currentSession();
        session.flush();
        session.clear();
    }

    /**
     * store an entity.
     *
     * @param entity the entity to store
     */
    public void storeEntity(final Object entity) {
        currentSession().saveOrUpdate(entity);
    }

    /**
     * store an entity if absent.
     *
     * @param entity the entity to store
     */
    public void storeEntityIfAbsent(final Object entity) {
        Session session = currentSession();
        doStoreEntityIfAbsent(entity, session);
    }

    private void doStoreEntityIfAbsent(final Object entity, Session session) {
        boolean exists = session.contains(entity);
        if (!exists) {
            session.saveOrUpdate(entity);
        }
    }

    /**
     * Merge an entity.
     *
     * @param entity the entity to merge
     */
    public Object mergeEntity(final Object entity) {
        return currentSession().merge(entity);
    }

    public void mergeAll(final Collection entities) {
        if (entities != null && !entities.isEmpty()) {
            Session session = currentSession();
            for (Object entity : entities) {
                session.merge(entity);
            }
        }
    }

    /**
     * store all entities.
     *
     * @param entities the entities to store
     */
    @SuppressWarnings("rawtypes")
    public void storeAll(final Collection entities) {
        if (entities != null && !entities.isEmpty()) {
            Session session = currentSession();
            for (Object entity : entities) {
                session.saveOrUpdate(entity);
            }
        }
    }

    /**
     * store every entity if absent.
     *
     * @param entities the entities to store
     */
    @SuppressWarnings("rawtypes")
    public void storeAllIfAbsent(final Collection entities) {
        if (entities != null && !entities.isEmpty()) {
            Session session = currentSession();
            for (Object entity : entities) {
                doStoreEntityIfAbsent(entity, session);
            }
        }
    }

    /**
     * Delete all entities.
     *
     * @param entities the entities to delete
     */
    @SuppressWarnings("rawtypes")
    public void deleteAll(final Collection entities) {
        if (entities != null && !entities.isEmpty()) {
            Session session = currentSession();
            for (Object entity : entities) {
                session.delete(entity);
            }
        }
    }

    /**
     * Save an entity.
     *
     * @param entity the entity to save
     */
    public Serializable saveEntity(final Object entity) {
        return currentSession().save(entity);
    }

    /**
     * Update an entity.
     *
     * @param entity the entity to update
     */
    public void updateEntity(final Object entity) {
        currentSession().update(entity);
        currentSession().flush();
        currentSession().clear();
    }

    /**
     * Delete an entity.
     *
     * @param entity the entity to delete
     */
    public void deleteEntity(final Object entity) {
        currentSession().delete(entity);
    }

    /**
     * Get a single object by its id
     *
     * @param <T>
     * @param entityClass
     * @param id
     * @return
     */
    @SuppressWarnings("unchecked")
    public <T> T getById(Class<T> entityClass, Serializable id) {
        return (T) currentSession().get(entityClass, id);
    }

    /**
     * Get a single object by the passed field using the name case sensitive.
     *
     * @param <T>
     * @param entityClass
     * @param field
     * @param name
     * @return
     */
    @SuppressWarnings("unchecked")
    public <T> T getByNaturalId(Class<? extends T> entityClass, String field, String name) {
        return (T) currentSession().byNaturalId(entityClass).using(field, name).load();
    }

    /**
     * Get a single object by the passed field using the name case insensitive.
     *
     * @param <T>
     * @param entityClass
     * @param field
     * @param name
     * @return
     */
    @SuppressWarnings("unchecked")
    public <T> T getByNaturalIdCaseInsensitive(Class<? extends T> entityClass, String field, String name) {
        StringBuilder sb = new StringBuilder();
        sb.append("from ");
        sb.append(entityClass.getSimpleName());
        sb.append(" where lower(").append(field).append(") = :name) { ");

        Map<String, Object> params = Collections.singletonMap("name", (Object) name.toLowerCase());
        return (T) this.findUniqueByNamedParameters(sb, params);
    }

    /**
     * Convert row object to a string.
     *
     * @param rowElement
     * @return <code>String</code>
     */
    protected static String convertRowElementToString(Object rowElement) {
        if (rowElement == null) {
            return null;
        } else if (rowElement instanceof String) {
            return (String) rowElement;
        } else {
            return rowElement.toString();
        }
    }

    /**
     * Convert row object to Integer.
     *
     * @param rowElement
     * @return <code>Integer</code>
     */
    protected static Integer convertRowElementToInteger(Object rowElement) {
        if (rowElement == null) {
            return 0;
        } else if (StringUtils.isNumeric(rowElement.toString())) {
            return Integer.valueOf(rowElement.toString());
        } else {
            return 0;
        }
    }

    /**
     * Convert row object to Long.
     *
     * @param rowElement
     * @return <code>Long</code>
     */
    protected static Long convertRowElementToLong(Object rowElement) {
        if (rowElement == null) {
            return 0L;
        } else if (StringUtils.isNumeric(rowElement.toString())) {
            return Long.valueOf(rowElement.toString());
        } else {
            return 0L;
        }
    }

    /**
     * Convert row object to date.
     *
     * @param rowElement
     * @return
     */
    protected static Date convertRowElementToDate(Object rowElement) {
        if (rowElement == null) {
            return null;
        } else if (rowElement instanceof Date) {
            return (Date) rowElement;
        } else {
            // TODO invalid date
            return null;
        }
    }

    /**
     * Convert row object to date.
     *
     * @param rowElement
     * @return
     */
    protected static Timestamp convertRowElementToTimestamp(Object rowElement) {
        if (rowElement == null) {
            return null;
        } else if (rowElement instanceof Timestamp) {
            return (Timestamp) rowElement;
        } else {
            // TODO invalid timestamp
            return null;
        }
    }

    /**
     * Convert row object to big decimal.
     *
     * @param rowElement
     * @return <code>BigDecimal</code>
     */
    protected static BigDecimal convertRowElementToBigDecimal(Object rowElement) {
        if (rowElement == null) {
            return BigDecimal.ZERO;
        } else if (rowElement instanceof BigDecimal) {
            return (BigDecimal) rowElement;
        } else {
            return new BigDecimal(rowElement.toString());
        }
    }

    @SuppressWarnings("rawtypes")
    protected static void applyNamedParameterToQuery(Query queryObject, String paramName, Object value) throws HibernateException {
        if (value instanceof Collection) {
            queryObject.setParameterList(paramName, (Collection) value);
        } else if (value instanceof Object[]) {
            queryObject.setParameterList(paramName, (Object[]) value);
        } else if (value instanceof String) {
            queryObject.setString(paramName, (String) value);
        } else {
            queryObject.setParameter(paramName, value);
        }
    }

    /**
     * Find entries-
     *
     * @param queryString the query string
     * @return list of entities
     */
    @SuppressWarnings("rawtypes")
    public List find(CharSequence queryString) {
        Query queryObject = currentSession().createQuery(queryString.toString());
        queryObject.setCacheable(true);
        return queryObject.list();
    }

    /**
     * Find entries by id.
     *
     * @param queryString the query string
     * @param id          the id
     * @return list of entities
     */
    @SuppressWarnings("rawtypes")
    public List findById(CharSequence queryString, Long id) {
        Query queryObject = currentSession().createQuery(queryString.toString());
        queryObject.setCacheable(true);
        queryObject.setParameter("id", id);
        return queryObject.list();
    }

    /**
     * Find list of entities by named parameters.
     *
     * @param queryCharSequence the query string
     * @param params            the named parameters
     * @return list of entities
     */
    @SuppressWarnings("rawtypes")
    public List findByNamedParameters(CharSequence queryCharSequence, Map<String, Object> params) {
        Query query = currentSession().createQuery(queryCharSequence.toString());
        query.setCacheable(true);
        for (Entry<String, Object> param : params.entrySet()) {
            applyNamedParameterToQuery(query, param.getKey(), param.getValue());
        }
        return query.list();
    }

    /**
     * Find unique entity by named parameters.
     *
     * @param queryCharSequence the query string
     * @param params            the named parameters
     * @return list of entities
     */
    public Object findUniqueByNamedParameters(CharSequence queryCharSequence, Map<String, Object> params) {
        Query query = currentSession().createQuery(queryCharSequence.toString());
        query.setCacheable(true);
        for (Entry<String, Object> param : params.entrySet()) {
            applyNamedParameterToQuery(query, param.getKey(), param.getValue());
        }
        return query.uniqueResult();
    }

    /**
     * Execute an update statement.
     *
     * @param queryCharSequence the query string
     * @return number of affected rows
     */
    public int executeUpdate(CharSequence queryCharSequence) {
        Query query = currentSession().createQuery(queryCharSequence.toString());
        query.setCacheable(true);
        return query.executeUpdate();
    }

    /**
     * Execute an update statement.
     *
     * @param queryCharSequence the query string
     * @param params            the named parameters
     * @return number of affected rows
     */
    public int executeUpdate(CharSequence queryCharSequence, Map<String, Object> params) {
        Query query = currentSession().createQuery(queryCharSequence.toString());
        query.setCacheable(true);
        for (Entry<String, Object> param : params.entrySet()) {
            applyNamedParameterToQuery(query, param.getKey(), param.getValue());
        }
        return query.executeUpdate();
    }

    /**
     * Execute a SQL update statement.
     *
     * @param queryCharSequence the query string
     * @return number of affected rows
     */
    public int executeSqlUpdate(CharSequence queryCharSequence) {
        SQLQuery query = currentSession().createSQLQuery(queryCharSequence.toString());
        query.setCacheable(true);
        return query.executeUpdate();
    }

    /**
     * Execute an update statement.
     *
     * @param queryCharSequence the query string
     * @param params            the named parameters
     * @return number of affected rows
     */
    public int executeSqlUpdate(CharSequence queryCharSequence, Map<String, Object> params) {
        SQLQuery query = currentSession().createSQLQuery(queryCharSequence.toString());
        query.setCacheable(true);
        for (Entry<String, Object> param : params.entrySet()) {
            applyNamedParameterToQuery(query, param.getKey(), param.getValue());
        }
        return query.executeUpdate();
    }

    private void setResultTransformer(Class T, SQLQuery query) {
        if (T != null) {
            if (T.equals(String.class)) {
                // no transformer needed
            } else if (T.equals(Long.class)) {
                // no transformer needed
            } else if (T.equals(Integer.class)) {
                // no transformer needed
            } else if (T.equals(Object[].class)) {
                // no transformer needed
            } else {
                query.setResultTransformer(Transformers.aliasToBean(T));
            }
        }
    }

}

