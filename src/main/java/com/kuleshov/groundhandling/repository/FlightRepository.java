package com.kuleshov.groundhandling.repository;

import com.kuleshov.groundhandling.HibernateUtil;
import com.kuleshov.groundhandling.domain.Cargo;
import com.kuleshov.groundhandling.domain.Flight;
import com.kuleshov.groundhandling.domain.Resource;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.List;

public class FlightRepository {

    public Flight save(Flight flight) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            session.persist(flight);
            tx.commit();
            return flight;
        }
    }

    public Flight update(Flight flight) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            session.merge(flight);
            tx.commit();
            return flight;
        }
    }

    public void delete(Flight flight) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            Flight managed = session.get(Flight.class, flight.getId());
            if (managed != null) session.remove(managed);
            tx.commit();
        }
    }

    public List<Flight> findAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                    "SELECT DISTINCT f FROM Flight f LEFT JOIN FETCH f.aircraft a LEFT JOIN FETCH a.compartments",
                    Flight.class).getResultList();
        }
    }

    public Flight findByIdWithDetails(Long id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Flight flight = session.createQuery(
                    "SELECT DISTINCT f FROM Flight f LEFT JOIN FETCH f.aircraft a LEFT JOIN FETCH a.compartments WHERE f.id = :id",
                    Flight.class).setParameter("id", id).uniqueResult();

            if (flight != null) {
                List<Cargo> cargos = session.createQuery(
                                "FROM Cargo c WHERE c.flight.id = :id", Cargo.class)
                        .setParameter("id", id).getResultList();
                flight.getCargoList().clear();
                flight.getCargoList().addAll(cargos);
            }
            return flight;
        }
    }

    public List<Resource> findAssignedResources(Flight flight) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                    "FROM Resource r LEFT JOIN FETCH r.assignedFlight WHERE r.assignedFlight.id = :id",
                    Resource.class).setParameter("id", flight.getId()).getResultList();
        }
    }
}