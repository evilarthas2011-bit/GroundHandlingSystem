package com.kuleshov.groundhandling.repository;

import com.kuleshov.groundhandling.domain.Aircraft;
import org.hibernate.Session;

import java.util.List;

public interface AircraftRepository {

    List<Aircraft> findAll(Session session);
    Aircraft findById(Session session, Long id);
    Aircraft findByTailNumber(Session session, String tailNumber);
    void save(Session session, Aircraft aircraft);

    List<Aircraft> findAll();
    Aircraft findByTailNumber(String tailNumber);
    void save(Aircraft aircraft);
}
