package com.valor.mercury.manager.dao;

import org.hibernate.Criteria;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;

/**
 * @author Gavin
 * 2020/02/17 10:11
 */
@Repository
public class MercuryManagerDao extends HibernateBaseDao {

    public <T> List<T> listEntity(Class<T> t, Criterion... criterions) {
        Criteria criteria = currentSession().createCriteria(t);
        for (Criterion criterion : criterions)
            criteria.add(criterion);
        criteria.addOrder(Order.desc("id"));
        List<T> result = criteria.list();
        if (result == null)
            return Collections.emptyList();
        else
            return result;
    }

    public <T> List<T> listEntity(Class<T> t, Integer pageIndex, Integer pageSize, String sortFiled, Criterion... criterions) {
        Criteria criteria = currentSession().createCriteria(t);
        for (Criterion criterion : criterions)
            if (criterion != null)
                criteria.add(criterion);
        if (pageIndex != null && pageSize != null) {
            criteria.setFirstResult((pageIndex - 1) * pageSize);
            criteria.setMaxResults(pageSize);
        }
        criteria.addOrder(Order.desc(sortFiled));
        List<T> result = criteria.list();
        if (result == null)
            return Collections.emptyList();
        else
            return result;
    }

    public <T> List<T> listEntity(Class<T> t, String sortFiled, Criterion... criterions) {
        Criteria criteria = currentSession().createCriteria(t);
        for (Criterion criterion : criterions)
            if (criterion != null)
                criteria.add(criterion);
        criteria.addOrder(Order.desc(sortFiled));
        List<T> result = criteria.list();
        if (result == null)
            return Collections.emptyList();
        else
            return result;
    }

    public <T> List<T> listEntity(Class<T> t, Integer pageIndex, Integer pageSize, Criterion... criterions) {
        Criteria criteria = currentSession().createCriteria(t);
        for (Criterion criterion : criterions)
            if (criterion != null)
                criteria.add(criterion);
        if (pageIndex != null && pageSize != null) {
            criteria.setFirstResult((pageIndex - 1) * pageSize);
            criteria.setMaxResults(pageSize);
        }
        criteria.addOrder(Order.desc("id"));
        List<T> result = criteria.list();
        if (result == null)
            return Collections.emptyList();
        else
            return result;
    }


    public <T> T getByQuery(Class<T> t, Criterion... criterions) {
        Criteria criteria = currentSession().createCriteria(t);
        for (Criterion criterion : criterions)
            criteria.add(criterion);
        List<T> result = criteria.list();
        if (result == null || result.size() == 0)
            return null;
        else return result.get(0);
    }


    public <T> Long count(Class<T> clazz, Criterion... criterionList) {
        Criteria criteria = currentSession().createCriteria(clazz);
        for (Criterion criterion : criterionList)
            if (criterion != null)
                criteria.add(criterion);
        criteria.setProjection(Projections.rowCount());
        return Long.parseLong(criteria.uniqueResult().toString());
    }
}
