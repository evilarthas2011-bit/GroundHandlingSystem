package com.kuleshov.groundhandling;

import com.kuleshov.groundhandling.domain.*;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

public class HibernateUtil {
    private static final SessionFactory sessionFactory;

    static {
        try {
            Configuration configuration = new Configuration();
            configuration.configure("hibernate.cfg.xml");
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
}