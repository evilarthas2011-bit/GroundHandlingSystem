package com.kuleshov.groundhandling;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        // Создаем простой интерфейс для проверки запуска
        Label label = new Label("Автоматизированное раб/место диспетчера в аэропорту ");
        label.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        StackPane root = new StackPane();
        root.getChildren().add(label);

        Scene scene = new Scene(root, 800, 600);

        primaryStage.setTitle("Ground Handling System v1.0");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
