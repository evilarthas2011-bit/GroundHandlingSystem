package com.kuleshov.groundhandling.repository;

import com.kuleshov.groundhandling.domain.Aircraft;
import com.kuleshov.groundhandling.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.List;

public class AircraftRepository {

    public List<Aircraft> findAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM Aircraft", Aircraft.class).getResultList();
        }
    }

    public void save(Aircraft aircraft) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            if (aircraft.getId() == null) {
                session.persist(aircraft);
            } else {
                session.merge(aircraft);
            }
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw e;
        }
    }

    public void update(Aircraft aircraft) {
        save(aircraft);
    }
}