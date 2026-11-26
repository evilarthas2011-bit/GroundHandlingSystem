package com.kuleshov.groundhandling.repository;

import com.kuleshov.groundhandling.HibernateUtil;
import com.kuleshov.groundhandling.domain.Cargo;
import com.kuleshov.groundhandling.domain.Flight;
import org.hibernate.Hibernate;
import org.hibernate.Session;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class FlightRepositoryImpl implements FlightRepository {

    @Override
    public List<Flight> findAll(Session session) {
        return session.createQuery(
                "SELECT DISTINCT f FROM Flight f LEFT JOIN FETCH f.aircraft",
                Flight.class).getResultList();
    }

    @Override
    public Flight findByIdWithDetails(Session session, Long id) {
        Flight flight = session.createQuery(
                        "SELECT DISTINCT f FROM Flight f " +
                                "LEFT JOIN FETCH f.aircraft a " +
                                "LEFT JOIN FETCH a.compartments " +
                                "WHERE f.id = :id", Flight.class)
                .setParameter("id", id)
                .uniqueResult();
        if (flight == null) {
            return null;
        }

        List<Cargo> cargos = session.createQuery(
                        "SELECT c FROM Cargo c " +
                                "JOIN FETCH c.compartment " +
                                "WHERE c.flight.id = :id ORDER BY c.addedAt, c.id", Cargo.class)
                .setParameter("id", id)
                .getResultList();

        flight.getCargoList().clear();
        flight.getCargoList().addAll(cargos);

        Map<Long, List<Cargo>> byCompartment = cargos.stream()
                .collect(Collectors.groupingBy(c -> c.getCompartment().getId()));

        for (var comp : flight.getAircraft().getCompartments()) {
            Hibernate.initialize(comp.getCargoList());
            comp.getCargoList().clear();
            comp.getCargoList().addAll(
                    byCompartment.getOrDefault(comp.getId(), List.of()));
        }
        return flight;
    }

    @Override
    public Flight findById(Session session, Long id) {
        return session.find(Flight.class, id);
    }

    @Override
    public void save(Session session, Flight flight) {
        session.merge(flight);
    }

    @Override
    public void delete(Session session, Flight flight) {
        Flight managed = session.contains(flight) ? flight : session.merge(flight);
        session.remove(managed);
    }

    @Override
    public List<Flight> findAll() {
        return HibernateUtil.inSession(this::findAll);
    }

    @Override
    public Flight findByIdWithDetails(Long id) {
        return HibernateUtil.inSession(s -> findByIdWithDetails(s, id));
    }
    @Override
    public Flight findByFlightNumber(String flightNumber) {
        return HibernateUtil.inTransaction(session -> findByFlightNumber(session, flightNumber));
    }

    public Flight findByFlightNumber(Session session, String flightNumber) {
        return session.createQuery("FROM Flight f WHERE f.flightNumber = :number", Flight.class)
                .setParameter("number", flightNumber)
                .uniqueResult();
    }


    @Override
    public void save(Flight flight) {
        HibernateUtil.inTransaction(s -> {
            save(s, flight);
            return null;
        });
    }

    @Override
    public void delete(Flight flight) {
        HibernateUtil.inTransaction(s -> {
            delete(s, flight);
            return null;
        });
    }
}
