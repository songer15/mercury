package com.valor.mercury.manager.service;

import com.valor.mercury.manager.dao.MercuryManagerDao;
import com.valor.mercury.manager.model.system.PageResult;
import org.hibernate.criterion.Criterion;

import java.io.Serializable;
import java.util.List;

/**
 * @author Gavin
 * 2019/11/25 16:57
 */
public abstract class BaseDBService {

    public abstract MercuryManagerDao getSchedulerDao();

    public boolean addEntity(Object object) {
        try {
            getSchedulerDao().saveEntity(object);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public <T> List<T> listEntity(Class<T> clazz) {
        return getSchedulerDao().listEntity(clazz);
    }

    public <T> List<T> listEntity(Class<T> clazz, Criterion... criteria) {
        return getSchedulerDao().listEntity(clazz, criteria);
    }

    public <T> List<T> listEntity(Class<T> clazz,String sortFiled, Criterion... criteria) {
        return getSchedulerDao().listEntity(clazz,sortFiled, criteria);
    }

    public boolean update(Object object) {
        try {
            getSchedulerDao().updateEntity(object);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public <T> T getEntityByCriterion(Class<T> clazz, Criterion... criteria) {
        List<T> result = getSchedulerDao().listEntity(clazz, criteria);
        if (result == null || result.size() == 0)
            return null;
        else
            return result.get(0);
    }

    public <T> T getEntityById(Class<T> t, Serializable id) {
        return getSchedulerDao().getById(t, id);
    }

    public <T> boolean deleteById(Class<T> clazz, Serializable id) {
        try {
            T t = getSchedulerDao().getById(clazz, id);
            getSchedulerDao().deleteEntity(t);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public <T> PageResult<T> list(Class<T> clazz, Integer page, Integer limit, Criterion... criterions) {
        List<T> list = getSchedulerDao().listEntity(clazz, page, limit, criterions);
        Long count = getSchedulerDao().count(clazz, criterions);
        return new PageResult<>(count, list);
    }

    public <T> PageResult<T> list(Class<T> clazz, Integer page, Integer limit, String sortFiled, Criterion... criterions) {
        List<T> list = getSchedulerDao().listEntity(clazz, page, limit, sortFiled, criterions);
        Long count = getSchedulerDao().count(clazz, criterions);
        return new PageResult<>(count, list);
    }

    public boolean deleteEntity(Object object) {
        try {
            getSchedulerDao().deleteEntity(object);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public <T> List<T> getObjectList(Class<T> clazz) {
        return getSchedulerDao().listEntity(clazz);
    }
}
