package com.kuleshov.groundhandling.repository;

import com.kuleshov.groundhandling.domain.*;
import com.kuleshov.groundhandling.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.List;

public class FlightRepository {

    public List<Flight> findAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                    "SELECT DISTINCT f FROM Flight f LEFT JOIN FETCH f.aircraft",
                    Flight.class
            ).getResultList();
        }
    }

    public Flight findByIdWithDetails(Long id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Flight flight = session.createQuery(
                            "SELECT DISTINCT f FROM Flight f " +
                                    "LEFT JOIN FETCH f.aircraft a " +
                                    "LEFT JOIN FETCH a.compartments " +
                                    "WHERE f.id = :id", Flight.class)
                    .setParameter("id", id)
                    .uniqueResult();

            if (flight != null) {
                List<Cargo> cargos = session.createQuery(
                                "FROM Cargo c WHERE c.flight.id = :id", Cargo.class)
                        .setParameter("id", id)
                        .getResultList();
                flight.getCargoList().clear();
                flight.getCargoList().addAll(cargos);
            }
            return flight;
        }
    }

    public void save(Flight flight) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            if (flight.getId() == null) {
                session.persist(flight);
            } else {
                session.merge(flight);
            }
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw e;
        }
    }

    public void update(Flight flight) {
        save(flight);
    }

    public void delete(Flight flight) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            Flight managed = session.merge(flight);
            session.remove(managed);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw e;
        }
    }
}