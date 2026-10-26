package com.kuleshov.groundhandling.controller;

import com.kuleshov.groundhandling.HibernateUtil;
import com.kuleshov.groundhandling.MainApp;
import com.kuleshov.groundhandling.domain.*;
import com.kuleshov.groundhandling.repository.AircraftRepository;
import com.kuleshov.groundhandling.repository.FlightRepository;
import com.kuleshov.groundhandling.repository.ResourceRepository;
import com.kuleshov.groundhandling.service.ResourceAllocationService;
import com.kuleshov.groundhandling.service.WeightBalanceService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

public class DashboardController implements Initializable {

    @FXML private StackPane mainContent;
    @FXML private VBox flightsView;
    @FXML private Button btnFlights;
    @FXML private Button btnResources;

    @FXML private TableView<Flight> flightsTable;
    @FXML private TableColumn<Flight, String> flightNumberColumn;
    @FXML private TableColumn<Flight, String> aircraftTypeColumn;
    @FXML private TableColumn<Flight, String> statusColumn;
    @FXML private TableColumn<Flight, String> weightColumn;
    @FXML private TableColumn<Flight, String> equipmentColumn;
    @FXML private TableColumn<Flight, String> brigadeColumn;
    @FXML private TableColumn<Flight, Void> actionsColumn;

    @FXML private Label totalFlightsLabel;
    @FXML private Label activeFlightsLabel;
    @FXML private Label completedFlightsLabel;

    @FXML private VBox detailsPanel;
    @FXML private Label detailFlightNumber;
    @FXML private Label detailAircraftType;
    @FXML private ComboBox<FlightStatus> statusComboBox;
    @FXML private Label detailTotalWeight;
    @FXML private VBox compartmentsContainer;
    @FXML private ComboBox<Equipment> equipmentComboBox;
    @FXML private ComboBox<Brigade> brigadeComboBox;

    @FXML private Button refreshButton;
    @FXML private Button newFlightButton;
    @FXML private Button closeDetailsButton;
    @FXML private Button autoAllocateButton;
    @FXML private Button addCargoButton;
    @FXML private Button saveButton;
    @FXML private Button deleteButton;

    @FXML private TextArea logArea;

    private FlightRepository flightRepository;
    private ResourceRepository resourceRepository;
    private WeightBalanceService weightBalanceService;
    private ResourceAllocationService allocationService;
    private AircraftRepository aircraftRepository;

    private Flight selectedFlight;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        flightRepository = new FlightRepository();
        resourceRepository = new ResourceRepository();
        weightBalanceService = new WeightBalanceService();
        allocationService = new ResourceAllocationService(resourceRepository);
        aircraftRepository = new AircraftRepository();

        setupTableColumns();
        setupStatusComboBox();
        loadFlights();

        btnFlights.setOnAction(e -> switchToFlights());
        btnResources.setOnAction(e -> switchToResources());

        log("✅ Система инициализирована. Подключение к БД успешно.");
    }

    private void setupStatusComboBox() {
        statusComboBox.setItems(FXCollections.observableArrayList(FlightStatus.values()));
        statusComboBox.setOnAction(e -> {
            if (selectedFlight != null) {
                selectedFlight.setStatus(statusComboBox.getValue());
            }
        });
    }

    private void setupTableColumns() {
        flightNumberColumn.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(data.getValue().getFlightNumber()));

        aircraftTypeColumn.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(data.getValue().getAircraftType().getDisplayName()));

        statusColumn.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(data.getValue().getStatus().toString()));

        statusColumn.setCellFactory(col -> new TableCell<Flight, String>() {
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
                    Flight flight = getTableView().getItems().get(getIndex());
                    badge.setText(item);
                    badge.getStyleClass().removeIf(s -> s.startsWith("status-") && !s.equals("status-badge"));
                    badge.getStyleClass().add(getStatusStyleClass(flight.getStatus()));
                    setGraphic(badge);
                }
            }
        });

        weightColumn.setCellValueFactory(data -> {
            Flight flight = data.getValue();
            double current = flight.getTotalCargoWeight() != null ? flight.getTotalCargoWeight() : 0;
            double max = flight.getAircraft() != null ? flight.getAircraft().getMaxPayload() : 0;
            return new javafx.beans.property.SimpleStringProperty(String.format("%.0f / %.0f кг", current, max));
        });

        equipmentColumn.setCellValueFactory(data -> {
            Flight flight = data.getValue();
            List<Resource> resources = flightRepository.findAssignedResources(flight);
            for (Resource resource : resources) {
                if (resource instanceof Equipment) {
                    return new javafx.beans.property.SimpleStringProperty(((Equipment) resource).getModelName());
                }
            }
            return new javafx.beans.property.SimpleStringProperty("—");
        });

        brigadeColumn.setCellValueFactory(data -> {
            Flight flight = data.getValue();
            List<Resource> resources = flightRepository.findAssignedResources(flight);
            for (Resource resource : resources) {
                if (resource instanceof Brigade) {
                    return new javafx.beans.property.SimpleStringProperty(((Brigade) resource).getSpecialization());
                }
            }
            return new javafx.beans.property.SimpleStringProperty("—");
        });

        actionsColumn.setCellFactory(col -> new TableCell<Flight, Void>() {
            private final Button viewButton = new Button("👁 Детали");
            {
                viewButton.setOnAction(event -> {
                    Flight flight = getTableView().getItems().get(getIndex());
                    showFlightDetails(flight);
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : viewButton);
            }
        });
    }

    private void loadFlights() {
        try {
            List<Flight> flights = flightRepository.findAll();
            flightsTable.setItems(FXCollections.observableArrayList(flights));
            totalFlightsLabel.setText(String.valueOf(flights.size()));
            activeFlightsLabel.setText(String.valueOf(flights.stream().filter(f -> f.getStatus() != FlightStatus.DEPARTED).count()));
            completedFlightsLabel.setText(String.valueOf(flights.stream().filter(f -> f.getStatus() == FlightStatus.DEPARTED).count()));
            log("📊 Загружено " + flights.size() + " рейсов из базы данных.");
        } catch (Exception e) {
            log("❌ Ошибка загрузки рейсов: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showFlightDetails(Flight flight) {
        selectedFlight = flightRepository.findByIdWithDetails(flight.getId());
        if (selectedFlight == null) {
            log("❌ Не удалось загрузить детали рейса!");
            return;
        }

        detailFlightNumber.setText(selectedFlight.getFlightNumber());
        detailAircraftType.setText(selectedFlight.getAircraftType().getDisplayName());
        statusComboBox.setValue(selectedFlight.getStatus());

        double totalWeight = selectedFlight.getTotalCargoWeight() != null ? selectedFlight.getTotalCargoWeight() : 0;
        detailTotalWeight.setText(String.format("%.0f кг", totalWeight));

        compartmentsContainer.getChildren().clear();
        if (selectedFlight.getAircraft() != null && selectedFlight.getAircraft().getCompartments() != null) {
            for (Compartment compartment : selectedFlight.getAircraft().getCompartments()) {
                compartmentsContainer.getChildren().add(createCompartmentBox(compartment));
            }
        }

        List<Resource> assignedResources = resourceRepository.findByAssignedFlight(selectedFlight);
        Equipment assignedEquipment = null;
        Brigade assignedBrigade = null;
        for (Resource resource : assignedResources) {
            if (resource instanceof Equipment) assignedEquipment = (Equipment) resource;
            else if (resource instanceof Brigade) assignedBrigade = (Brigade) resource;
        }

        List<Equipment> availableEquipment = resourceRepository.findAvailableEquipment();
        if (assignedEquipment != null && !availableEquipment.contains(assignedEquipment)) {
            availableEquipment.add(assignedEquipment);
        }
        equipmentComboBox.setItems(FXCollections.observableArrayList(availableEquipment));
        equipmentComboBox.getSelectionModel().select(assignedEquipment);
        setupEquipmentComboBoxDisplay();

        List<Brigade> availableBrigades = resourceRepository.findAvailableBrigades();
        if (assignedBrigade != null && !availableBrigades.contains(assignedBrigade)) {
            availableBrigades.add(assignedBrigade);
        }
        brigadeComboBox.setItems(FXCollections.observableArrayList(availableBrigades));
        brigadeComboBox.getSelectionModel().select(assignedBrigade);
        setupBrigadeComboBoxDisplay();

        detailsPanel.setVisible(true);
        detailsPanel.setManaged(true);
        detailsPanel.getParent().requestLayout();

        log("🔍 Открыты детали рейса " + selectedFlight.getFlightNumber());
    }

    private VBox createCompartmentBox(Compartment compartment) {
        VBox box = new VBox(5);
        box.setStyle("-fx-padding: 10; -fx-background-color: #f8f9fa; -fx-background-radius: 5;");

        Label nameLabel = new Label(compartment.getName());
        nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");

        double current = compartment.getCurrentWeight() != null ? compartment.getCurrentWeight() : 0;
        double max = compartment.getMaxWeight() != null ? compartment.getMaxWeight() : 1;
        double ratio = current / max;

        Label weightLabel = new Label(String.format("Загрузка: %.0f / %.0f кг (%.0f%%)", current, max, ratio * 100));

        ProgressBar progressBar = new ProgressBar();
        progressBar.setProgress(ratio);
        progressBar.setPrefWidth(250);
        progressBar.setStyle("-fx-accent: " + getProgressBarColor(ratio) + ";");

        box.getChildren().addAll(nameLabel, weightLabel, progressBar);
        return box;
    }

    private String getProgressBarColor(double ratio) {
        if (ratio > 0.9) return "#e74c3c";
        if (ratio > 0.7) return "#f39c12";
        return "#27ae60";
    }

    private void setupEquipmentComboBoxDisplay() {
        equipmentComboBox.setCellFactory(param -> new ListCell<Equipment>() {
            @Override
            protected void updateItem(Equipment item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getModelName());
            }
        });
        equipmentComboBox.setButtonCell(new ListCell<Equipment>() {
            @Override
            protected void updateItem(Equipment item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getModelName());
            }
        });
    }

    private void setupBrigadeComboBoxDisplay() {
        brigadeComboBox.setCellFactory(param -> new ListCell<Brigade>() {
            @Override
            protected void updateItem(Brigade item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getSpecialization());
            }
        });
        brigadeComboBox.setButtonCell(new ListCell<Brigade>() {
            @Override
            protected void updateItem(Brigade item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getSpecialization());
            }
        });
    }

    @FXML
    private void handleCloseDetails() {
        detailsPanel.setVisible(false);
        detailsPanel.setManaged(false);
        selectedFlight = null;
        flightsTable.getSelectionModel().clearSelection();
        detailsPanel.getParent().requestLayout();
    }

    @FXML
    private void handleAutoAllocate() {
        if (selectedFlight == null) {
            log("⚠ Сначала выберите рейс!");
            return;
        }
        try {
            allocationService.autoAllocate(selectedFlight);
            log("✅ Авто-распределение выполнено для рейса " + selectedFlight.getFlightNumber());
            loadFlights();
            showFlightDetails(selectedFlight);
        } catch (Exception e) {
            log("❌ Ошибка авто-распределения: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleAddCargo() {
        if (selectedFlight == null) {
            log(" Сначала выберите рейс!");
            return;
        }
        if (selectedFlight.getAircraft() == null || selectedFlight.getAircraft().getCompartments() == null || selectedFlight.getAircraft().getCompartments().isEmpty()) {
            log(" У рейса нет ВС или отсеков!");
            return;
        }

        Flight freshFlight = flightRepository.findByIdWithDetails(selectedFlight.getId());
        if (freshFlight == null || freshFlight.getAircraft() == null) {
            log("❌ Ошибка: не удалось загрузить актуальные данные рейса!");
            return;
        }

        Compartment firstCompartment = freshFlight.getAircraft().getCompartments().get(0);
        Cargo testCargo = new Cargo(500.0, "Тестовый груз", firstCompartment, freshFlight);
        boolean success = weightBalanceService.validateAndAddCargo(freshFlight, testCargo);

        if (success) {
            log("✅ Груз 500 кг добавлен в отсек '" + firstCompartment.getName() + "'");
            aircraftRepository.update(freshFlight.getAircraft());
            flightRepository.update(freshFlight);
            log("💾 Данные сохранены в базу данных.");
            selectedFlight = freshFlight;
            showFlightDetails(freshFlight);
            loadFlights();
        } else {
            log("❌ Не удалось добавить груз: превышен лимит веса!");
        }
    }

    @FXML
    private void handleSave() {
        if (selectedFlight == null) {
            log("⚠ Нечего сохранять!");
            return;
        }
        try {
            List<Resource> oldResources = resourceRepository.findByAssignedFlight(selectedFlight);
            for (Resource oldResource : oldResources) {
                oldResource.setAssignedFlight(null);
                resourceRepository.update(oldResource);
            }

            Equipment selectedEquipment = equipmentComboBox.getValue();
            Brigade selectedBrigade = brigadeComboBox.getValue();

            if (selectedEquipment != null) {
                selectedEquipment.setAssignedFlight(selectedFlight);
                resourceRepository.update(selectedEquipment);
                log("✅ Назначена техника: " + selectedEquipment.getModelName());
            }

            if (selectedBrigade != null) {
                selectedBrigade.setAssignedFlight(selectedFlight);
                resourceRepository.update(selectedBrigade);
                log("✅ Назначена бригада: " + selectedBrigade.getSpecialization());
            }

            flightRepository.update(selectedFlight);
            log("💾 Рейс " + selectedFlight.getFlightNumber() + " сохранён в БД.");

            loadFlights();
            flightsTable.refresh();
        } catch (Exception e) {
            log("❌ Ошибка сохранения: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleDelete() {
        if (selectedFlight == null) {
            log("⚠ Сначала выберите рейс!");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Подтверждение удаления");
        confirm.setHeaderText("Удалить рейс " + selectedFlight.getFlightNumber() + "?");
        confirm.setContentText("Это действие нельзя отменить.");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    List<Resource> assignedResources = resourceRepository.findByAssignedFlight(selectedFlight);
                    for (Resource resource : assignedResources) {
                        resource.setAssignedFlight(null);
                        resourceRepository.update(resource);
                    }

                    flightRepository.delete(selectedFlight);
                    log("🗑 Рейс " + selectedFlight.getFlightNumber() + " удалён из БД.");

                    handleCloseDetails();
                    loadFlights();
                } catch (Exception e) {
                    log("❌ Ошибка удаления: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        });
    }

    @FXML
    private void handleNewFlight() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/flight-form.fxml"));
            Parent root = loader.load();

            FlightFormController controller = loader.getController();
            controller.setOnFlightCreated(() -> {
                loadFlights();
                log("✅ Новый рейс успешно создан");
            });

            Stage stage = new Stage();
            stage.setTitle("Создание нового рейса");
            stage.setScene(new Scene(root));
            stage.initOwner(MainApp.getPrimaryStage());
            stage.setResizable(false);
            stage.show();
        } catch (Exception e) {
            log("❌ Ошибка открытия формы: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleRefresh() {
        log("🔄 Обновление данных...");
        loadFlights();
    }

    private void switchToFlights() {
        btnFlights.getStyleClass().add("active");
        btnResources.getStyleClass().remove("active");
        mainContent.getChildren().setAll(flightsView);
        log("📋 Переключено на экран рейсов");
    }

    private void switchToResources() {
        btnResources.getStyleClass().add("active");
        btnFlights.getStyleClass().remove("active");
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/resources.fxml"));
            Parent resourcesView = loader.load();
            mainContent.getChildren().setAll(resourcesView);
            log(" Переключено на экран ресурсов");
        } catch (Exception e) {
            log("❌ Ошибка загрузки экрана ресурсов: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private String getStatusStyleClass(FlightStatus status) {
        if (status == null) return "status-scheduled";
        return switch (status) {
            case SCHEDULED -> "status-scheduled";
            case ARRIVED -> "status-arrived";
            case LOADING -> "status-loading";
            case BOARDING -> "status-boarding";
            case READY -> "status-ready";
            case DEPARTED -> "status-departed";
            case DELAYED -> "status-delayed";
        };
    }

    private void log(String message) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        logArea.appendText("[" + timestamp + "] " + message + "\n");
    }
}