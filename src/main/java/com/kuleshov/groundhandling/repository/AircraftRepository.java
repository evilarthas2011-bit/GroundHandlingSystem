package com.kuleshov.groundhandling.repository;

import com.kuleshov.groundhandling.HibernateUtil;
import com.kuleshov.groundhandling.domain.Aircraft;
import com.kuleshov.groundhandling.domain.AircraftType;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.List;

public class AircraftRepository {

    public Aircraft save(Aircraft aircraft) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            session.persist(aircraft);
            tx.commit();
            return aircraft;
        }
    }

    public Aircraft update(Aircraft aircraft) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            session.merge(aircraft);
            tx.commit();
            return aircraft;
        }
    }

    public List<Aircraft> findByType(AircraftType type) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                            "FROM Aircraft a WHERE a.type = :type", Aircraft.class)
                    .setParameter("type", type).getResultList();
        }
    }
}