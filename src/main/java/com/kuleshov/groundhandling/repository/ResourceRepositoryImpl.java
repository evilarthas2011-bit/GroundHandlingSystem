package com.kuleshov.groundhandling.repository;

import com.kuleshov.groundhandling.HibernateUtil;
import com.kuleshov.groundhandling.domain.Brigade;
import com.kuleshov.groundhandling.domain.Equipment;
import com.kuleshov.groundhandling.domain.Flight;
import com.kuleshov.groundhandling.domain.Resource;
import org.hibernate.Session;

import java.util.List;

public class ResourceRepositoryImpl implements ResourceRepository {

    // ================= Ядро =================

    @Override
    public List<Resource> findAll(Session session) {
        return session.createQuery(
                "SELECT r FROM Resource r LEFT JOIN FETCH r.assignedFlight",
                Resource.class).getResultList();
    }

    @Override
    public Resource findById(Session session, Long id) {
        return session.get(Resource.class, id);
    }

    @Override
    public List<Resource> findByAssignedFlight(Session session, Flight flight) {
        return session.createQuery(
                        "SELECT r FROM Resource r WHERE r.assignedFlight.id = :flightId",
                        Resource.class)
                .setParameter("flightId", flight.getId())
                .getResultList();
    }

    @Override
    public List<Equipment> findAvailableEquipment(Session session) {
        return session.createQuery(
                "SELECT e FROM Equipment e WHERE e.assignedFlight IS NULL",
                Equipment.class).getResultList();
    }

    @Override
    public List<Brigade> findAvailableBrigades(Session session) {
        return session.createQuery(
                "SELECT b FROM Brigade b WHERE b.assignedFlight IS NULL",
                Brigade.class).getResultList();
    }

    @Override
    public void save(Session session, Resource resource) {
        session.merge(resource);
    }

    @Override
    public void delete(Session session, Resource resource) {
        Resource managed = session.contains(resource) ? resource : session.merge(resource);
        session.remove(managed);
    }

    // ================= Переходные обёртки =================

    @Override
    public List<Resource> findAll() {
        return HibernateUtil.inSession(this::findAll);
    }

    @Override
    public Resource findById(Long id) {
        return HibernateUtil.inSession(s -> findById(s, id));
    }

    @Override
    public List<Resource> findByAssignedFlight(Flight flight) {
        return HibernateUtil.inSession(s -> findByAssignedFlight(s, flight));
    }

    @Override
    public List<Equipment> findAvailableEquipment() {
        return HibernateUtil.inSession(this::findAvailableEquipment);
    }

    @Override
    public List<Brigade> findAvailableBrigades() {
        return HibernateUtil.inSession(this::findAvailableBrigades);
    }
    public void release(Long resourceId) {
        HibernateUtil.inTransaction(session -> {
            Resource resource = session.get(Resource.class, resourceId);
            if (resource != null) {
                resource.setAssignedFlight(null);
            }
            return resource;
        });
    }
    @Override
    public void save(Resource resource) {
        HibernateUtil.inTransaction(s -> {
            save(s, resource);
            return null;
        });
    }

    @Override
    public void delete(Resource resource) {
        HibernateUtil.inTransaction(s -> {
            delete(s, resource);
            return null;
        });
    }
}
