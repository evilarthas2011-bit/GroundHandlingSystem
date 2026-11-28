package com.kuleshov.groundhandling.repository;

import com.kuleshov.groundhandling.domain.Brigade;
import com.kuleshov.groundhandling.domain.Equipment;
import com.kuleshov.groundhandling.domain.Flight;
import com.kuleshov.groundhandling.domain.Resource;
import org.hibernate.Session;

import java.util.List;

public interface ResourceRepository {

    List<Resource> findAll(Session session);
    Resource findById(Session session, Long id);
    List<Resource> findByAssignedFlight(Session session, Flight flight);
    List<Equipment> findAvailableEquipment(Session session);
    List<Brigade> findAvailableBrigades(Session session);
    void save(Session session, Resource resource);
    void delete(Session session, Resource resource);

    List<Resource> findAll();
    Resource findById(Long id);
    List<Resource> findByAssignedFlight(Flight flight);
    List<Equipment> findAvailableEquipment();
    List<Brigade> findAvailableBrigades();
    void save(Resource resource);
    void delete(Resource resource);
}
