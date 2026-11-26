package com.kuleshov.groundhandling.repository;

import com.kuleshov.groundhandling.domain.Flight;
import org.hibernate.Session;

import java.util.List;

public interface FlightRepository {

    List<Flight> findAll(Session session);
    Flight findByIdWithDetails(Session session, Long id);
    Flight findById(Session session, Long id);
    void save(Session session, Flight flight);
    void delete(Session session, Flight flight);


    List<Flight> findAll();
    Flight findByIdWithDetails(Long id);
    void save(Flight flight);

    void delete(Flight flight);
    Flight findByFlightNumber(String flightNumber);

}
