package com.kuleshov.groundhandling.repository;

import com.kuleshov.groundhandling.domain.Brigade;
import com.kuleshov.groundhandling.domain.Equipment;
import com.kuleshov.groundhandling.domain.Flight;
import com.kuleshov.groundhandling.domain.Resource;
import com.kuleshov.groundhandling.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.List;

public class ResourceRepository {

    public Resource save(Resource resource) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            session.persist(resource);
            transaction.commit();
            return resource;
        }
    }


    public List<Resource> findAvailable() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                            "FROM Resource r WHERE r.isAvailable = true", Resource.class)
                    .getResultList();
        }
    }
    public List<Equipment> findAvailableEquipment() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                            "FROM Equipment e WHERE e.isAvailable = true", Equipment.class)
                    .getResultList();
        }
    }

    public List<Brigade> findAvailableBrigades() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                            "FROM Brigade b WHERE b.isAvailable = true", Brigade.class)
                    .getResultList();
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
                            "FROM Resource r WHERE r.assignedFlight = :flight", Resource.class)
                    .setParameter("flight", flight)
                    .getResultList();
        }}
    public Resource update(Resource resource) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            session.merge(resource);
            transaction.commit();
            return resource;
        }
    }
}