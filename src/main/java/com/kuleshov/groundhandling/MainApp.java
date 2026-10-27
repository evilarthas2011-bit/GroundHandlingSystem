package com.kuleshov.groundhandling;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {

    private static Stage primaryStage;

    @Override
    public void start(Stage stage) {
        primaryStage = stage;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/dashboard.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root, 1200, 700);

            stage.setTitle("Ground Handling System - Панель управления");
            stage.setScene(scene);
            stage.show();

            System.out.println("Приложение успешно запущено!");

        } catch (Exception e) {
            System.err.println("Ошибка запуска приложения: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static Stage getPrimaryStage() { return primaryStage; }

    public static void main(String[] args) { launch(args); }
}