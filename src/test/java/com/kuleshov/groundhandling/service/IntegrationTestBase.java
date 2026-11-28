package com.kuleshov.groundhandling.service;

import com.kuleshov.groundhandling.HibernateUtil;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;


public abstract class IntegrationTestBase {

    @BeforeAll
    static void initFactory() {
        System.setProperty("hibernate.cfg", "hibernate-test.cfg.xml");
        HibernateUtil.getSessionFactory();
    }

    @BeforeEach
    void cleanDatabase() {
        HibernateUtil.inTransaction(session -> {
            session.createQuery("DELETE FROM Cargo").executeUpdate();
            session.createQuery("DELETE FROM Resource").executeUpdate();
            session.createQuery("DELETE FROM Flight").executeUpdate();
            session.createQuery("DELETE FROM Compartment").executeUpdate();
            session.createQuery("DELETE FROM Aircraft").executeUpdate();
            return null;
        });
    }
}
