package com.kuleshov.groundhandling;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;


public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/dashboard.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root, 1200, 700);

            primaryStage.setTitle("Ground Handling System - Панель управления");
            primaryStage.setScene(scene);
            primaryStage.show();

            System.out.println("✅ Приложение успешно запущено!");

        } catch (Exception e) {
            System.err.println("❌ Ошибка запуска приложения: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}