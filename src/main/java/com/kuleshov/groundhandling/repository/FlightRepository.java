package com.kuleshov.groundhandling.repository;

import com.kuleshov.groundhandling.HibernateUtil;
import com.kuleshov.groundhandling.domain.Cargo;
import com.kuleshov.groundhandling.domain.Flight;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.time.LocalDate;
import java.util.List;


public class FlightRepository {


    public Flight save(Flight flight) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            session.persist(flight);
            transaction.commit();
            return flight;
        }
    }


    public Flight findById(Long id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(Flight.class, id);
        }
    }

    public List<Flight> findByDate(LocalDate date) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                            "FROM Flight f WHERE DATE(f.scheduledTime) = :date", Flight.class)
                    .setParameter("date", date)
                    .getResultList();
        }
    }


    public List<Flight> findAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                            "SELECT DISTINCT f FROM Flight f " +
                                    "LEFT JOIN FETCH f.aircraft a " +
                                    "LEFT JOIN FETCH a.compartments",
                            Flight.class)
                    .getResultList();
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
    public Flight update(Flight flight) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            session.merge(flight);
            transaction.commit();
            return flight;
        }
    }

    public void delete(Long id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            Flight flight = session.get(Flight.class, id);
            if (flight != null) {
                session.remove(flight);
            }
            transaction.commit();
        }
    }
}
