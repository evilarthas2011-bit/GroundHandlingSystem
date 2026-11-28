package com.kuleshov.groundhandling.repository;

import com.kuleshov.groundhandling.HibernateUtil;
import com.kuleshov.groundhandling.domain.Aircraft;
import org.hibernate.Session;

import java.util.List;

public class AircraftRepositoryImpl implements AircraftRepository {

    @Override
    public List<Aircraft> findAll(Session session) {
        return session.createQuery(
                "SELECT DISTINCT a FROM Aircraft a LEFT JOIN FETCH a.compartments",
                Aircraft.class).getResultList();
    }

    @Override
    public Aircraft findById(Session session, Long id) {
        return session.get(Aircraft.class, id);
    }

    @Override
    public Aircraft findByTailNumber(Session session, String tailNumber) {
        return session.createQuery(
                        "SELECT a FROM Aircraft a WHERE a.tailNumber = :tn", Aircraft.class)
                .setParameter("tn", tailNumber)
                .getResultStream()
                .findFirst()
                .orElse(null);
    }

    @Override
    public void save(Session session, Aircraft aircraft) {
        session.merge(aircraft);
    }

    @Override
    public List<Aircraft> findAll() {
        return HibernateUtil.inSession(this::findAll);
    }

    @Override
    public Aircraft findByTailNumber(String tailNumber) {
        return HibernateUtil.inSession(s -> findByTailNumber(s, tailNumber));
    }

    @Override
    public void save(Aircraft aircraft) {
        HibernateUtil.inTransaction(s -> {
            save(s, aircraft);
            return null;
        });
    }
}
