package com.kuleshov.groundhandling.repository;

import com.kuleshov.groundhandling.domain.*;
import com.kuleshov.groundhandling.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.List;

public class ResourceRepository {

    public List<Resource> findAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                    "SELECT r FROM Resource r LEFT JOIN FETCH r.assignedFlight",
                    Resource.class
            ).getResultList();
        }
    }

    public Resource findById(Long id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(Resource.class, id);
        }
    }

    public List<Resource> findByAssignedFlight(Flight flight) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                            "SELECT r FROM Resource r WHERE r.assignedFlight.id = :flightId",
                            Resource.class)
                    .setParameter("flightId", flight.getId())
                    .getResultList();
        }
    }

    public List<Equipment> findAvailableEquipment() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                    "SELECT e FROM Equipment e WHERE e.assignedFlight IS NULL",
                    Equipment.class
            ).getResultList();
        }
    }

    public List<Brigade> findAvailableBrigades() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                    "SELECT b FROM Brigade b WHERE b.assignedFlight IS NULL",
                    Brigade.class
            ).getResultList();
        }
    }

    public Resource save(Resource resource) {
        Transaction tx = null;
        Session session = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();
            Resource managed = session.merge(resource);
            tx.commit();
            return managed;
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw new RuntimeException("Ошибка сохранения ресурса: " + e.getMessage(), e);
        } finally {
            if (session != null) session.close();
        }
    }

    public Resource update(Resource resource) {
        return save(resource);
    }

    public void delete(Resource resource) {
        Transaction tx = null;
        Session session = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();
            Resource managed = session.merge(resource);
            session.remove(managed);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw new RuntimeException("Ошибка удаления ресурса: " + e.getMessage(), e);
        } finally {
            if (session != null) session.close();
        }
    }
}