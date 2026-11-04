package com.kuleshov.groundhandling.controller;

import com.kuleshov.groundhandling.domain.*;
import com.kuleshov.groundhandling.repository.ResourceRepository;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

public class ResourceController implements Initializable {

    @FXML private VBox mainContainer;
    @FXML private TabPane resourceTabPane;
    @FXML private Tab equipmentTab;
    @FXML private Tab brigadeTab;

    @FXML private TableView<Equipment> equipmentTable;
    @FXML private TableColumn<Equipment, Long> equipmentIdColumn;
    @FXML private TableColumn<Equipment, String> equipmentModelColumn;
    @FXML private TableColumn<Equipment, String> equipmentStatusColumn;
    @FXML private TableColumn<Equipment, String> equipmentFlightColumn;
    @FXML private TableColumn<Equipment, Void> equipmentActionsColumn;

    @FXML private TableView<Brigade> brigadeTable;
    @FXML private TableColumn<Brigade, Long> brigadeIdColumn;
    @FXML private TableColumn<Brigade, String> brigadeSpecializationColumn;
    @FXML private TableColumn<Brigade, Integer> brigadeSizeColumn;
    @FXML private TableColumn<Brigade, String> brigadeStatusColumn;
    @FXML private TableColumn<Brigade, String> brigadeFlightColumn;
    @FXML private TableColumn<Brigade, Void> brigadeActionsColumn;

    @FXML private TextField newEquipmentModelField;
    @FXML private TextField newBrigadeSpecializationField;
    @FXML private TextField newBrigadeSizeField;

    @FXML private TextArea logArea;

    private ResourceRepository resourceRepository;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        resourceRepository = new ResourceRepository();

        setupEquipmentTable();
        setupBrigadeTable();
        loadResources();

        log("Экран ресурсов инициализирован");
    }

    private void setupEquipmentTable() {
        equipmentIdColumn.setCellValueFactory(data ->
                new SimpleObjectProperty<>(data.getValue().getId()));

        equipmentModelColumn.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getModelName()));

        equipmentStatusColumn.setCellValueFactory(data ->
                new SimpleStringProperty(
                        data.getValue().getAssignedFlight() != null ? "Занят" : "Свободен"));

        equipmentFlightColumn.setCellValueFactory(data -> {
            Flight flight = data.getValue().getAssignedFlight();
            return new SimpleStringProperty(
                    flight != null ? flight.getFlightNumber() : "—");
        });

        equipmentActionsColumn.setCellFactory(col -> new TableCell<>() {
            private final Button freeButton = new Button("Освободить");
            private final Button deleteButton = new Button("Удалить");

            {
                freeButton.setOnAction(event -> handleFreeEquipment(getTableView().getItems().get(getIndex())));
                deleteButton.setOnAction(event -> handleDeleteEquipment(getTableView().getItems().get(getIndex())));

                freeButton.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white; -fx-padding: 5 10; -fx-background-radius: 3;");
                deleteButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-padding: 5 10; -fx-background-radius: 3;");
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Equipment equipment = getTableView().getItems().get(getIndex());
                    if (equipment.getAssignedFlight() != null) {
                        setGraphic(freeButton);
                    } else {
                        setGraphic(deleteButton);
                    }
                }
            }
        });
    }

    private void setupBrigadeTable() {
        brigadeIdColumn.setCellValueFactory(data ->
                new SimpleObjectProperty<>(data.getValue().getId()));

        brigadeSpecializationColumn.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getSpecialization()));

        brigadeSizeColumn.setCellValueFactory(data ->
                new SimpleObjectProperty<>(data.getValue().getSize()));

        brigadeStatusColumn.setCellValueFactory(data ->
                new SimpleStringProperty(
                        data.getValue().getAssignedFlight() != null ? "Занята" : "Свободна"));

        brigadeFlightColumn.setCellValueFactory(data -> {
            Flight flight = data.getValue().getAssignedFlight();
            return new SimpleStringProperty(
                    flight != null ? flight.getFlightNumber() : "—");
        });

        brigadeActionsColumn.setCellFactory(col -> new TableCell<>() {
            private final Button freeButton = new Button("Освободить");
            private final Button deleteButton = new Button("Удалить");

            {
                freeButton.setOnAction(event -> handleFreeBrigade(getTableView().getItems().get(getIndex())));
                deleteButton.setOnAction(event -> handleDeleteBrigade(getTableView().getItems().get(getIndex())));

                freeButton.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white; -fx-padding: 5 10; -fx-background-radius: 3;");
                deleteButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-padding: 5 10; -fx-background-radius: 3;");
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Brigade brigade = getTableView().getItems().get(getIndex());
                    if (brigade.getAssignedFlight() != null) {
                        setGraphic(freeButton);
                    } else {
                        setGraphic(deleteButton);
                    }
                }
            }
        });
    }

    private void loadResources() {
        try {
            List<Resource> allResources = resourceRepository.findAll();

            List<Equipment> equipmentList = allResources.stream()
                    .filter(r -> r instanceof Equipment)
                    .map(r -> (Equipment) r)
                    .toList();

            List<Brigade> brigadeList = allResources.stream()
                    .filter(r -> r instanceof Brigade)
                    .map(r -> (Brigade) r)
                    .toList();

            equipmentTable.setItems(FXCollections.observableArrayList(equipmentList));
            brigadeTable.setItems(FXCollections.observableArrayList(brigadeList));

        } catch (Exception e) {
            log("Ошибка загрузки ресурсов: " + e.getMessage());
            showError("Ошибка", "Не удалось загрузить список ресурсов.\n" + e.getMessage());
        }
    }

    @FXML
    private void handleAddEquipment() {
        String model = newEquipmentModelField.getText().trim();
        if (model.isEmpty()) {
            showWarning("Внимание", "Введите модель техники!");
            return;
        }

        try {
            Equipment equipment = new Equipment();
            equipment.setName(model); // <-- ДОБАВЛЕНО: заполняем обязательное поле name
            equipment.setModelName(model);
            equipment.setCapacity(0.0);

            resourceRepository.save(equipment);
            log("Добавлена техника: " + model);
            newEquipmentModelField.clear();
            loadResources();

        } catch (Exception e) {
            log("Ошибка добавления техники: " + e.getMessage());
            showError("Ошибка", "Не удалось добавить технику.\n" + e.getMessage());
        }
    }

    @FXML
    private void handleAddBrigade() {
        String specialization = newBrigadeSpecializationField.getText().trim();
        String sizeText = newBrigadeSizeField.getText().trim();

        if (specialization.isEmpty() || sizeText.isEmpty()) {
            showWarning("Внимание", "Заполните все поля!");
            return;
        }

        try {
            int size = Integer.parseInt(sizeText);
            if (size <= 0) {
                showWarning("Ошибка", "Размер бригады должен быть больше 0");
                return;
            }

            Brigade brigade = new Brigade();
            brigade.setName(specialization); // <-- ДОБАВЛЕНО: заполняем обязательное поле name
            brigade.setSpecialization(specialization);
            brigade.setSize(size);

            resourceRepository.save(brigade);
            log("Добавлена бригада: " + specialization + " (" + size + " чел.)");
            newBrigadeSpecializationField.clear();
            newBrigadeSizeField.clear();
            loadResources();

        } catch (NumberFormatException e) {
            showWarning("Ошибка", "Введите корректное число для размера бригады");
        } catch (Exception e) {
            log("Ошибка добавления бригады: " + e.getMessage());
            showError("Ошибка", "Не удалось добавить бригаду.\n" + e.getMessage());
        }
    }

    private void handleFreeEquipment(Equipment equipment) {
        if (equipment == null) return;

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Освободить технику " + equipment.getModelName() + "?");
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    equipment.setAssignedFlight(null);
                    resourceRepository.save(equipment);
                    log("Освобождена техника: " + equipment.getModelName());
                    loadResources();
                } catch (Exception e) {
                    log("Ошибка освобождения техники: " + e.getMessage());
                    showError("Ошибка", "Не удалось освободить технику.\n" + e.getMessage());
                }
            }
        });
    }

    private void handleFreeBrigade(Brigade brigade) {
        if (brigade == null) return;

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Освободить бригаду " + brigade.getSpecialization() + "?");
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    brigade.setAssignedFlight(null);
                    resourceRepository.save(brigade);
                    log("Освобождена бригада: " + brigade.getSpecialization());
                    loadResources();
                } catch (Exception e) {
                    log("Ошибка освобождения бригады: " + e.getMessage());
                    showError("Ошибка", "Не удалось освободить бригаду.\n" + e.getMessage());
                }
            }
        });
    }

    private void handleDeleteEquipment(Equipment equipment) {
        if (equipment == null) return;

        if (equipment.getAssignedFlight() != null) {
            showWarning("Ошибка", "Нельзя удалить занятую технику. Сначала освободите её.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Удалить технику " + equipment.getModelName() + "?");
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    resourceRepository.delete(equipment);
                    log("Удалена техника: " + equipment.getModelName());
                    loadResources();
                } catch (Exception e) {
                    log("Ошибка удаления техники: " + e.getMessage());
                    showError("Ошибка", "Не удалось удалить технику.\n" + e.getMessage());
                }
            }
        });
    }

    private void handleDeleteBrigade(Brigade brigade) {
        if (brigade == null) return;

        if (brigade.getAssignedFlight() != null) {
            showWarning("Ошибка", "Нельзя удалить занятую бригаду. Сначала освободите её.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Удалить бригаду " + brigade.getSpecialization() + "?");
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    resourceRepository.delete(brigade);
                    log("Удалена бригада: " + brigade.getSpecialization());
                    loadResources();
                } catch (Exception e) {
                    log("Ошибка удаления бригады: " + e.getMessage());
                    showError("Ошибка", "Не удалось удалить бригаду.\n" + e.getMessage());
                }
            }
        });
    }

    private void log(String msg) {
        String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        Platform.runLater(() -> {
            logArea.appendText("[" + time + "] " + msg + "\n");
        });
    }

    private void showError(String title, String content) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(content);
            alert.showAndWait();
        });
    }

    private void showWarning(String title, String content) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(content);
            alert.showAndWait();
        });
    }
}