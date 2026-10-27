package com.kuleshov.groundhandling.controller;

import com.kuleshov.groundhandling.domain.*;
import com.kuleshov.groundhandling.repository.ResourceRepository;
import com.kuleshov.groundhandling.service.OperationResult;
import com.kuleshov.groundhandling.service.ResourceManagementService;
import com.kuleshov.groundhandling.service.ResourceManagementServiceImpl;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class ResourcesController implements Initializable {

    @FXML private Button addResourceButton;
    @FXML private Button refreshButton;
    @FXML private Label totalResourcesLabel, equipmentCountLabel, brigadeCountLabel, freeResourcesLabel, busyResourcesLabel;

    @FXML private TableView<Equipment> equipmentTable;
    @FXML private TableColumn<Equipment, String> equipmentNameColumn, equipmentStatusColumn, equipmentFlightColumn;
    @FXML private TableColumn<Equipment, Double> equipmentCapacityColumn;
    @FXML private TableColumn<Equipment, Void> equipmentActionColumn;

    @FXML private TableView<Brigade> brigadeTable;
    @FXML private TableColumn<Brigade, String> brigadeNameColumn, brigadeStatusColumn, brigadeFlightColumn;
    @FXML private TableColumn<Brigade, Integer> brigadeSizeColumn;
    @FXML private TableColumn<Brigade, Void> brigadeActionColumn;

    private ResourceRepository resourceRepository;
    private ResourceManagementService resourceManagementService;
    private ObservableList<Equipment> allEquipment;
    private ObservableList<Brigade> allBrigades;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        resourceRepository = new ResourceRepository();
        resourceManagementService = new ResourceManagementServiceImpl(resourceRepository);
        allEquipment = FXCollections.observableArrayList();
        allBrigades = FXCollections.observableArrayList();

        setupEquipmentTable();
        setupBrigadeTable();
        loadResources();
    }

    private void setupEquipmentTable() {
        equipmentNameColumn.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(data.getValue().getModelName()));

        equipmentCapacityColumn.setCellValueFactory(data ->
                new javafx.beans.property.SimpleObjectProperty<>(data.getValue().getLoadCapacity()));

        equipmentStatusColumn.setCellValueFactory(data -> {
            try {
                String status = data.getValue().getAssignedFlight() != null ? "Занят" : "Свободен";
                return new javafx.beans.property.SimpleStringProperty(status);
            } catch (Exception e) {
                return new javafx.beans.property.SimpleStringProperty("Свободен");
            }
        });

        equipmentStatusColumn.setCellFactory(col -> new TableCell<>() {
            private final Label badge = new Label();
            {
                badge.getStyleClass().add("status-badge");
                setGraphic(badge);
                setAlignment(javafx.geometry.Pos.CENTER);
            }
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setGraphic(null); return; }
                badge.setText(item);
                badge.getStyleClass().removeIf(s -> s.startsWith("status-") && !s.equals("status-badge"));
                badge.getStyleClass().add(item.equals("Свободен") ? "status-scheduled" : "status-delayed");
                setGraphic(badge);
            }
        });

        equipmentFlightColumn.setCellValueFactory(data -> {
            try {
                Flight flight = data.getValue().getAssignedFlight();
                return new javafx.beans.property.SimpleStringProperty(flight != null ? flight.getFlightNumber() : "—");
            } catch (Exception e) {
                return new javafx.beans.property.SimpleStringProperty("—");
            }
        });

        equipmentActionColumn.setCellFactory(col -> new TableCell<>() {
            private final Button releaseButton = new Button("Освободить");
            private final Button deleteButton = new Button("Удалить");

            {
                releaseButton.setStyle("-fx-background-color: #e67e22; -fx-text-fill: white; -fx-padding: 5 10; -fx-background-radius: 3; -fx-cursor: hand;");
                releaseButton.setOnAction(event -> {
                    Equipment eq = getTableView().getItems().get(getIndex());
                    OperationResult result = resourceManagementService.freeResource(eq.getId());
                    if (result.isSuccess()) {
                        loadResources();
                    } else {
                        new Alert(Alert.AlertType.WARNING, result.getMessage()).show();
                    }
                });

                deleteButton.setStyle("-fx-background-color: #c0392b; -fx-text-fill: white; -fx-padding: 5 10; -fx-background-radius: 3; -fx-cursor: hand;");
                deleteButton.setOnAction(event -> {
                    Equipment eq = getTableView().getItems().get(getIndex());
                    if (eq.getAssignedFlight() != null) {
                        new Alert(Alert.AlertType.WARNING, "Сначала освободите ресурс!").show();
                    } else {
                        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Удалить \"" + eq.getModelName() + "\"?");
                        confirm.showAndWait().ifPresent(r -> {
                            if (r == ButtonType.OK) {
                                OperationResult result = resourceManagementService.deleteResource(eq.getId());
                                if (result.isSuccess()) {
                                    loadResources();
                                } else {
                                    new Alert(Alert.AlertType.ERROR, result.getMessage()).show();
                                }
                            }
                        });
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setGraphic(null); return; }
                Equipment eq = getTableView().getItems().get(getIndex());
                if (eq.getAssignedFlight() != null) {
                    HBox buttons = new HBox(5, releaseButton, deleteButton);
                    setGraphic(buttons);
                } else {
                    setGraphic(deleteButton);
                }
            }
        });
    }

    private void setupBrigadeTable() {
        brigadeNameColumn.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(data.getValue().getSpecialization()));

        brigadeSizeColumn.setCellValueFactory(data ->
                new javafx.beans.property.SimpleObjectProperty<>(data.getValue().getSize()));

        brigadeStatusColumn.setCellValueFactory(data -> {
            try {
                String status = data.getValue().getAssignedFlight() != null ? "Занят" : "Свободен";
                return new javafx.beans.property.SimpleStringProperty(status);
            } catch (Exception e) {
                return new javafx.beans.property.SimpleStringProperty("Свободен");
            }
        });

        brigadeStatusColumn.setCellFactory(col -> new TableCell<>() {
            private final Label badge = new Label();
            {
                badge.getStyleClass().add("status-badge");
                setGraphic(badge);
                setAlignment(javafx.geometry.Pos.CENTER);
            }
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setGraphic(null); return; }
                badge.setText(item);
                badge.getStyleClass().removeIf(s -> s.startsWith("status-") && !s.equals("status-badge"));
                badge.getStyleClass().add(item.equals("Свободен") ? "status-scheduled" : "status-delayed");
                setGraphic(badge);
            }
        });

        brigadeFlightColumn.setCellValueFactory(data -> {
            try {
                Flight flight = data.getValue().getAssignedFlight();
                return new javafx.beans.property.SimpleStringProperty(flight != null ? flight.getFlightNumber() : "—");
            } catch (Exception e) {
                return new javafx.beans.property.SimpleStringProperty("—");
            }
        });

        brigadeActionColumn.setCellFactory(col -> new TableCell<>() {
            private final Button releaseButton = new Button("Освободить");
            private final Button deleteButton = new Button("Удалить");

            {
                releaseButton.setStyle("-fx-background-color: #e67e22; -fx-text-fill: white; -fx-padding: 5 10; -fx-background-radius: 3; -fx-cursor: hand;");
                releaseButton.setOnAction(event -> {
                    Brigade br = getTableView().getItems().get(getIndex());
                    OperationResult result = resourceManagementService.freeResource(br.getId());
                    if (result.isSuccess()) {
                        loadResources();
                    } else {
                        new Alert(Alert.AlertType.WARNING, result.getMessage()).show();
                    }
                });

                deleteButton.setStyle("-fx-background-color: #c0392b; -fx-text-fill: white; -fx-padding: 5 10; -fx-background-radius: 3; -fx-cursor: hand;");
                deleteButton.setOnAction(event -> {
                    Brigade br = getTableView().getItems().get(getIndex());
                    if (br.getAssignedFlight() != null) {
                        new Alert(Alert.AlertType.WARNING, "Сначала освободите ресурс!").show();
                    } else {
                        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Удалить \"" + br.getSpecialization() + "\"?");
                        confirm.showAndWait().ifPresent(r -> {
                            if (r == ButtonType.OK) {
                                OperationResult result = resourceManagementService.deleteResource(br.getId());
                                if (result.isSuccess()) {
                                    loadResources();
                                } else {
                                    new Alert(Alert.AlertType.ERROR, result.getMessage()).show();
                                }
                            }
                        });
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setGraphic(null); return; }
                Brigade br = getTableView().getItems().get(getIndex());
                if (br.getAssignedFlight() != null) {
                    HBox buttons = new HBox(5, releaseButton, deleteButton);
                    setGraphic(buttons);
                } else {
                    setGraphic(deleteButton);
                }
            }
        });
    }

    private void loadResources() {
        try {
            List<Resource> allResources = resourceRepository.findAll();
            allEquipment.clear();
            allBrigades.clear();
            for (Resource resource : allResources) {
                if (resource instanceof Equipment) allEquipment.add((Equipment) resource);
                else if (resource instanceof Brigade) allBrigades.add((Brigade) resource);
            }
            equipmentTable.setItems(allEquipment);
            brigadeTable.setItems(allBrigades);
            updateStatistics();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateStatistics() {
        int total = allEquipment.size() + allBrigades.size();
        int free = (int) (allEquipment.stream().filter(eq -> eq.getAssignedFlight() == null).count() +
                allBrigades.stream().filter(br -> br.getAssignedFlight() == null).count());
        int busy = total - free;

        totalResourcesLabel.setText(String.valueOf(total));
        equipmentCountLabel.setText(String.valueOf(allEquipment.size()));
        brigadeCountLabel.setText(String.valueOf(allBrigades.size()));
        freeResourcesLabel.setText(String.valueOf(free));
        busyResourcesLabel.setText(String.valueOf(busy));
    }

    @FXML
    private void handleAddResource() {
        Alert alert = new Alert(Alert.AlertType.NONE);
        alert.setTitle("Добавление ресурса");
        alert.setHeaderText("Выберите тип ресурса");

        ButtonType equipmentType = new ButtonType("Техника");
        ButtonType brigadeType = new ButtonType("Бригада");
        ButtonType cancelType = new ButtonType("Отмена", ButtonBar.ButtonData.CANCEL_CLOSE);

        alert.getButtonTypes().setAll(equipmentType, brigadeType, cancelType);

        alert.showAndWait().ifPresent(type -> {
            if (type == equipmentType) showEquipmentForm();
            else if (type == brigadeType) showBrigadeForm();
        });
    }

    private void showEquipmentForm() {
        Dialog<Equipment> dialog = new Dialog<>();
        dialog.setTitle("Добавление техники");
        dialog.setHeaderText("Введите параметры новой техники");

        ButtonType saveButtonType = new ButtonType("Сохранить", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField modelNameField = new TextField();
        modelNameField.setPromptText("Например: Тягач T-100");
        TextField capacityField = new TextField();
        capacityField.setPromptText("Например: 5000");

        grid.add(new Label("Модель:"), 0, 0);
        grid.add(modelNameField, 1, 0);
        grid.add(new Label("Грузоподъемность (кг):"), 0, 1);
        grid.add(capacityField, 1, 1);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                try {
                    double capacity = Double.parseDouble(capacityField.getText());
                    Equipment equipment = new Equipment(modelNameField.getText(), capacity);
                    resourceRepository.save(equipment);
                    return equipment;
                } catch (NumberFormatException e) {
                    return null;
                }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(equipment -> {
            allEquipment.add(equipment);
            updateStatistics();
        });
    }

    private void showBrigadeForm() {
        Dialog<Brigade> dialog = new Dialog<>();
        dialog.setTitle("Добавление бригады");
        dialog.setHeaderText("Введите параметры новой бригады");

        ButtonType saveButtonType = new ButtonType("Сохранить", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField specializationField = new TextField();
        specializationField.setPromptText("Например: Погрузочная бригада");
        TextField sizeField = new TextField();
        sizeField.setPromptText("Например: 6");

        grid.add(new Label("Специализация:"), 0, 0);
        grid.add(specializationField, 1, 0);
        grid.add(new Label("Численность (чел):"), 0, 1);
        grid.add(sizeField, 1, 1);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                try {
                    int size = Integer.parseInt(sizeField.getText());
                    Brigade brigade = new Brigade(size, specializationField.getText());
                    resourceRepository.save(brigade);
                    return brigade;
                } catch (NumberFormatException e) {
                    return null;
                }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(brigade -> {
            allBrigades.add(brigade);
            updateStatistics();
        });
    }

    @FXML
    private void handleRefresh() {
        loadResources();
    }
}