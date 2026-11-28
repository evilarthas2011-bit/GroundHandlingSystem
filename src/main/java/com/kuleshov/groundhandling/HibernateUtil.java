package com.kuleshov.groundhandling;

import com.kuleshov.groundhandling.domain.*;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;

import java.util.function.Function;

public class HibernateUtil {
    private static final SessionFactory sessionFactory;
    private static final String CFG_FILE =
            System.getProperty("hibernate.cfg", "hibernate.cfg.xml");

    static {
        try {
            Configuration configuration = new Configuration();
            configuration.configure(CFG_FILE);
            configuration.addAnnotatedClass(Flight.class);
            configuration.addAnnotatedClass(Aircraft.class);
            configuration.addAnnotatedClass(Compartment.class);
            configuration.addAnnotatedClass(Cargo.class);
            configuration.addAnnotatedClass(Equipment.class);
            configuration.addAnnotatedClass(Brigade.class);
            sessionFactory = configuration.buildSessionFactory();
        } catch (Throwable ex) {
            throw new ExceptionInInitializerError(ex);
        }
    }

    public static SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    public static <T> T inSession(Function<Session, T> work) {
        try (Session session = sessionFactory.openSession()) {
            return work.apply(session);
        }
    }

    public static <T> T inTransaction(Function<Session, T> work) {
        try (Session session = sessionFactory.openSession()) {
            Transaction tx = session.beginTransaction();
            try {
                T result = work.apply(session);
                tx.commit();
                return result;
            } catch (RuntimeException e) {
                tx.rollback();
                throw e;
            }
        }
    }

}
