package com.kuleshov.groundhandling.controller;

import com.kuleshov.groundhandling.domain.*;
import com.kuleshov.groundhandling.repository.ResourceRepository;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class ResourcesController implements Initializable {

    @FXML private Button addResourceButton;
    @FXML private Button refreshButton;
    @FXML private Label totalResourcesLabel;
    @FXML private Label equipmentCountLabel;
    @FXML private Label brigadeCountLabel;
    @FXML private Label freeResourcesLabel;
    @FXML private Label busyResourcesLabel;
    @FXML private ComboBox<String> filterTypeComboBox;
    @FXML private ComboBox<String> filterStatusComboBox;
    @FXML private TextField searchField;

    @FXML private TableView<Equipment> equipmentTable;
    @FXML private TableColumn<Equipment, String> equipmentNameColumn;
    @FXML private TableColumn<Equipment, Double> equipmentCapacityColumn;
    @FXML private TableColumn<Equipment, String> equipmentStatusColumn;
    @FXML private TableColumn<Equipment, String> equipmentFlightColumn;
    @FXML private TableColumn<Equipment, Void> equipmentActionColumn;

    @FXML private TableView<Brigade> brigadeTable;
    @FXML private TableColumn<Brigade, String> brigadeNameColumn;
    @FXML private TableColumn<Brigade, Integer> brigadeSizeColumn;
    @FXML private TableColumn<Brigade, String> brigadeStatusColumn;
    @FXML private TableColumn<Brigade, String> brigadeFlightColumn;
    @FXML private TableColumn<Brigade, Void> brigadeActionColumn;

    private ResourceRepository resourceRepository;
    private ObservableList<Equipment> allEquipment;
    private ObservableList<Brigade> allBrigades;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        resourceRepository = new ResourceRepository();
        allEquipment = FXCollections.observableArrayList();
        allBrigades = FXCollections.observableArrayList();

        setupFilters();
        setupEquipmentTable();
        setupBrigadeTable();
        loadResources();

        searchField.textProperty().addListener((obs, oldVal, newVal) -> filterResources());
    }

    private void setupFilters() {
        filterTypeComboBox.setItems(FXCollections.observableArrayList("Все", "Техника", "Бригады"));
        filterTypeComboBox.setValue("Все");
        filterTypeComboBox.setOnAction(e -> filterResources());

        filterStatusComboBox.setItems(FXCollections.observableArrayList("Все", "Свободен", "Занят"));
        filterStatusComboBox.setValue("Все");
        filterStatusComboBox.setOnAction(e -> filterResources());
    }

    private void setupEquipmentTable() {
        equipmentNameColumn.setCellValueFactory(new PropertyValueFactory<>("modelName"));
        equipmentCapacityColumn.setCellValueFactory(data ->
                new javafx.beans.property.SimpleObjectProperty<>(data.getValue().getLoadCapacity()));

        equipmentStatusColumn.setCellValueFactory(data -> {
            String status = data.getValue().getAssignedFlight() != null ? "Занят" : "Свободен";
            return new javafx.beans.property.SimpleStringProperty(status);
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
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    badge.setText(item);
                    badge.getStyleClass().removeIf(s -> s.startsWith("status-") && !s.equals("status-badge"));
                    badge.getStyleClass().add(item.equals("Свободен") ? "status-scheduled" : "status-delayed");
                    setGraphic(badge);
                }
            }
        });

        equipmentFlightColumn.setCellValueFactory(data -> {
            Flight flight = data.getValue().getAssignedFlight();
            return new javafx.beans.property.SimpleStringProperty(flight != null ? flight.getFlightNumber() : "—");
        });

        equipmentActionColumn.setCellFactory(col -> new TableCell<>() {
            private final Button releaseButton = new Button("Освободить");
            {
                releaseButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-padding: 5 10; -fx-background-radius: 3; -fx-cursor: hand;");
                releaseButton.setOnAction(event -> {
                    Equipment equipment = getTableView().getItems().get(getIndex());
                    if (equipment.getAssignedFlight() != null) {
                        equipment.setAssignedFlight(null);
                        resourceRepository.update(equipment);
                        loadResources();
                    }
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Equipment eq = getTableView().getItems().get(getIndex());
                    setGraphic(eq.getAssignedFlight() != null ? releaseButton : null);
                }
            }
        });
    }

    private void setupBrigadeTable() {
        brigadeNameColumn.setCellValueFactory(new PropertyValueFactory<>("specialization"));
        brigadeSizeColumn.setCellValueFactory(new PropertyValueFactory<>("size"));

        brigadeStatusColumn.setCellValueFactory(data -> {
            String status = data.getValue().getAssignedFlight() != null ? "Занят" : "Свободен";
            return new javafx.beans.property.SimpleStringProperty(status);
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
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    badge.setText(item);
                    badge.getStyleClass().removeIf(s -> s.startsWith("status-") && !s.equals("status-badge"));
                    badge.getStyleClass().add(item.equals("Свободен") ? "status-scheduled" : "status-delayed");
                    setGraphic(badge);
                }
            }
        });

        brigadeFlightColumn.setCellValueFactory(data -> {
            Flight flight = data.getValue().getAssignedFlight();
            return new javafx.beans.property.SimpleStringProperty(flight != null ? flight.getFlightNumber() : "—");
        });

        brigadeActionColumn.setCellFactory(col -> new TableCell<>() {
            private final Button releaseButton = new Button("Освободить");
            {
                releaseButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-padding: 5 10; -fx-background-radius: 3; -fx-cursor: hand;");
                releaseButton.setOnAction(event -> {
                    Brigade brigade = getTableView().getItems().get(getIndex());
                    if (brigade.getAssignedFlight() != null) {
                        brigade.setAssignedFlight(null);
                        resourceRepository.update(brigade);
                        loadResources();
                    }
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Brigade br = getTableView().getItems().get(getIndex());
                    setGraphic(br.getAssignedFlight() != null ? releaseButton : null);
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
                if (resource instanceof Equipment) {
                    allEquipment.add((Equipment) resource);
                } else if (resource instanceof Brigade) {
                    allBrigades.add((Brigade) resource);
                }
            }

            filterResources();
            updateStatistics();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void filterResources() {
        String typeFilter = filterTypeComboBox.getValue();
        String statusFilter = filterStatusComboBox.getValue();
        String searchText = searchField.getText() != null ? searchField.getText().toLowerCase() : "";

        ObservableList<Equipment> filteredEquipment = allEquipment.filtered(eq -> {
            boolean typeMatch = typeFilter == null || typeFilter.equals("Все") || typeFilter.equals("Техника");
            boolean statusMatch = statusFilter == null || statusFilter.equals("Все") ||
                    (statusFilter.equals("Свободен") == (eq.getAssignedFlight() == null));
            boolean searchMatch = searchText.isEmpty() || eq.getModelName().toLowerCase().contains(searchText);
            return typeMatch && statusMatch && searchMatch;
        });

        ObservableList<Brigade> filteredBrigades = allBrigades.filtered(br -> {
            boolean typeMatch = typeFilter == null || typeFilter.equals("Все") || typeFilter.equals("Бригады");
            boolean statusMatch = statusFilter == null || statusFilter.equals("Все") ||
                    (statusFilter.equals("Свободен") == (br.getAssignedFlight() == null));
            boolean searchMatch = searchText.isEmpty() || br.getSpecialization().toLowerCase().contains(searchText);
            return typeMatch && statusMatch && searchMatch;
        });

        equipmentTable.setItems(filteredEquipment);
        brigadeTable.setItems(filteredBrigades);
    }

    private void updateStatistics() {
        int total = allEquipment.size() + allBrigades.size();
        int free = (int)(allEquipment.stream().filter(eq -> eq.getAssignedFlight() == null).count() +
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

        ButtonType equipmentType = new ButtonType("🚜 Техника");
        ButtonType brigadeType = new ButtonType("👷 Бригада");
        ButtonType cancelType = new ButtonType("Отмена", ButtonBar.ButtonData.CANCEL_CLOSE);

        alert.getButtonTypes().setAll(equipmentType, brigadeType, cancelType);

        alert.showAndWait().ifPresent(type -> {
            if (type == equipmentType) {
                showEquipmentForm();
            } else if (type == brigadeType) {
                showBrigadeForm();
            }
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
        grid.add(new Label("Грузоподъёмность (кг):"), 0, 1);
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