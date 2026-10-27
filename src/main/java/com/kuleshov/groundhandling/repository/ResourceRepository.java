package com.kuleshov.groundhandling.repository;

import com.kuleshov.groundhandling.HibernateUtil;
import com.kuleshov.groundhandling.domain.*;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.List;

public class ResourceRepository {

    public Resource save(Resource resource) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            session.persist(resource);
            tx.commit();
            return resource;
        }
    }

    public Resource update(Resource resource) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            session.merge(resource);
            tx.commit();
            return resource;
        }
    }

    public void delete(Resource resource) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            Resource managed = session.get(Resource.class, resource.getId());
            if (managed != null) session.remove(managed);
            tx.commit();
        }
    }

    public List<Resource> findAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                    "SELECT DISTINCT r FROM Resource r LEFT JOIN FETCH r.assignedFlight",
                    Resource.class).getResultList();
        }
    }

    public List<Equipment> findAvailableEquipment() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                    "FROM Equipment e WHERE e.assignedFlight IS NULL", Equipment.class).getResultList();
        }
    }

    public List<Brigade> findAvailableBrigades() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                    "FROM Brigade b WHERE b.assignedFlight IS NULL", Brigade.class).getResultList();
        }
    }

    public List<Resource> findByAssignedFlight(Flight flight) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                    "FROM Resource r LEFT JOIN FETCH r.assignedFlight WHERE r.assignedFlight.id = :id",
                    Resource.class).setParameter("id", flight.getId()).getResultList();
        }
    }

    public Resource findById(Long id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(Resource.class, id);
        }
    }
}