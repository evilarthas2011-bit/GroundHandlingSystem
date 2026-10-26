package com.kuleshov.groundhandling.repository;

import com.kuleshov.groundhandling.domain.Aircraft;
import com.kuleshov.groundhandling.domain.AircraftType;
import com.kuleshov.groundhandling.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.List;

public class AircraftRepository {


    public Aircraft save(Aircraft aircraft) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            session.persist(aircraft);
            transaction.commit();
            return aircraft;
        }
    }


    public Aircraft findById(Long id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(Aircraft.class, id);
        }
    }


    public List<Aircraft> findAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM Aircraft", Aircraft.class).getResultList();
        }
    }


    public List<Aircraft> findByType(AircraftType type) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                            "FROM Aircraft a WHERE a.type = :type", Aircraft.class)
                    .setParameter("type", type)
                    .getResultList();
        }
    }

    public void delete(Long id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            Aircraft aircraft = session.get(Aircraft.class, id);
            if (aircraft != null) {
                session.remove(aircraft);
            }
            transaction.commit();
        }
    }
    public Aircraft update(Aircraft aircraft) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            session.merge(aircraft);
            transaction.commit();
            return aircraft;
        }
    }
}
