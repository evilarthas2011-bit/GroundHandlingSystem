package com.kuleshov.groundhandling;

import com.kuleshov.groundhandling.domain.AircraftType;
import com.kuleshov.groundhandling.domain.Flight;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.hibernate.Session;

import java.time.LocalDateTime;

public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        Label statusLabel = new Label("Проверка подключения к БД...");
        Label resultLabel = new Label();

        try {
            Flight testFlight = new Flight(
                    "SU-1234", AircraftType.BOEING_737, LocalDateTime.now()
            );

            Session session = HibernateUtil.getSessionFactory().openSession();
            session.beginTransaction();
            session.persist(testFlight);
            session.getTransaction().commit();
            session.close();

            resultLabel.setText("✓ Рейс успешно сохранен в БД! ID: " + testFlight.getId());
            statusLabel.setText("Подключение к PostgreSQL работает!");

        } catch (Exception e) {
            resultLabel.setText("✗ Ошибка: " + e.getMessage());
            statusLabel.setText("Ошибка подключения к БД");
            e.printStackTrace();
        }

        VBox root = new VBox(10, statusLabel, resultLabel);
        root.setStyle("-fx-padding: 20;");

        Scene scene = new Scene(root, 600, 400);
        primaryStage.setTitle("Ground Handling System - Тест БД");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}