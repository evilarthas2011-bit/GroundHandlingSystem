package com.kuleshov.groundhandling.repository;

import com.kuleshov.groundhandling.domain.Resource;
import com.kuleshov.groundhandling.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.List;

public class ResourceRepository {

    public Resource save(Resource resource) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            session.persist(resource);
            transaction.commit();
            return resource;
        }
    }


    public List<Resource> findAvailable() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                            "FROM Resource r WHERE r.isAvailable = true", Resource.class)
                    .getResultList();
        }
    }

    public Resource findById(Long id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(Resource.class, id);
        }
    }

    public Resource update(Resource resource) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            session.merge(resource);
            transaction.commit();
            return resource;
        }
    }
}