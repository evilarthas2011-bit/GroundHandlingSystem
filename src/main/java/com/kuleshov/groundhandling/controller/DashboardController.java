package com.kuleshov.groundhandling.controller;

import com.kuleshov.groundhandling.HibernateUtil;
import com.kuleshov.groundhandling.domain.*;
import com.kuleshov.groundhandling.repository.AircraftRepository;
import com.kuleshov.groundhandling.repository.FlightRepository;
import com.kuleshov.groundhandling.repository.ResourceRepository;
import com.kuleshov.groundhandling.service.ResourceAllocationService;
import com.kuleshov.groundhandling.service.WeightBalanceService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;


import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

public class DashboardController implements Initializable {

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
    @FXML private Label detailStatus;
    @FXML private Label detailTotalWeight;
    @FXML private VBox compartmentsContainer;
    @FXML private ComboBox<Equipment> equipmentComboBox;
    @FXML private ComboBox<Brigade> brigadeComboBox;

    @FXML private Button refreshButton;
    @FXML private Button closeDetailsButton;
    @FXML private Button autoAllocateButton;
    @FXML private Button addCargoButton;
    @FXML private Button saveButton;

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
        loadFlights();

        log("✅ Система инициализирована. Подключение к БД успешно.");
    }

    private void setupTableColumns() {
        flightNumberColumn.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(data.getValue().getFlightNumber()));

        aircraftTypeColumn.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(data.getValue().getAircraftType().getDisplayName()));

        statusColumn.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(data.getValue().getStatus().toString()));

        weightColumn.setCellValueFactory(data -> {
            Flight flight = data.getValue();
            double current = flight.getTotalCargoWeight() != null ? flight.getTotalCargoWeight() : 0;
            double max = flight.getAircraft() != null ? flight.getAircraft().getMaxPayload() : 0;
            return new javafx.beans.property.SimpleStringProperty(String.format("%.0f / %.0f кг", current, max));
        });

        equipmentColumn.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty("—"));

        brigadeColumn.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty("—"));

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
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(viewButton);
                }
            }
        });
    }

    private void loadFlights() {
        try {
            List<Flight> flights = flightRepository.findAll();
            ObservableList<Flight> flightList = FXCollections.observableArrayList(flights);
            flightsTable.setItems(flightList);

            totalFlightsLabel.setText(String.valueOf(flights.size()));

            long activeCount = flights.stream()
                    .filter(f -> f.getStatus() != FlightStatus.DEPARTED)
                    .count();
            activeFlightsLabel.setText(String.valueOf(activeCount));

            long completedCount = flights.stream()
                    .filter(f -> f.getStatus() == FlightStatus.DEPARTED)
                    .count();
            completedFlightsLabel.setText(String.valueOf(completedCount));

            log("📊 Загружено " + flights.size() + " рейсов из базы данных.");
        } catch (Exception e) {
            log(" Ошибка загрузки рейсов: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showFlightDetails(Flight flight) {
        selectedFlight = flightRepository.findByIdWithDetails(flight.getId());

        if (selectedFlight == null) {
            log(" Не удалось загрузить детали рейса!");
            return;
        }

        detailFlightNumber.setText(selectedFlight.getFlightNumber());
        detailAircraftType.setText(selectedFlight.getAircraftType().getDisplayName());
        detailStatus.setText(selectedFlight.getStatus().toString());

        double totalWeight = selectedFlight.getTotalCargoWeight() != null
                ? selectedFlight.getTotalCargoWeight() : 0;
        detailTotalWeight.setText(String.format("%.0f кг", totalWeight));

        compartmentsContainer.getChildren().clear();
        if (selectedFlight.getAircraft() != null &&
                selectedFlight.getAircraft().getCompartments() != null) {

            for (Compartment compartment : selectedFlight.getAircraft().getCompartments()) {
                VBox compartmentBox = new VBox(5);
                compartmentBox.setStyle("-fx-padding: 10; -fx-background-color: #f8f9fa; -fx-background-radius: 5;");

                Label nameLabel = new Label(compartment.getName());
                nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");

                double current = compartment.getCurrentWeight() != null ? compartment.getCurrentWeight() : 0;
                double max = compartment.getMaxWeight() != null ? compartment.getMaxWeight() : 1;

                Label weightLabel = new Label(String.format("Загрузка: %.0f / %.0f кг (%.0f%%)",
                        current, max, (current / max) * 100));

                ProgressBar progressBar = new ProgressBar();
                progressBar.setProgress(current / max);
                progressBar.setPrefWidth(250);

                if (current / max > 0.9) {
                    progressBar.setStyle("-fx-accent: #e74c3c;"); // Красный — почти полный
                } else if (current / max > 0.7) {
                    progressBar.setStyle("-fx-accent: #f39c12;"); // Жёлтый — средне
                } else {
                    progressBar.setStyle("-fx-accent: #27ae60;"); // Зелёный — нормально
                }

                compartmentBox.getChildren().addAll(nameLabel, weightLabel, progressBar);
                compartmentsContainer.getChildren().add(compartmentBox);
            }
        } else {
            Label noAircraft = new Label("⚠ ВС не назначено");
            noAircraft.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 13px;");
            compartmentsContainer.getChildren().add(noAircraft);
        }

        List<Resource> assignedResources = resourceRepository.findByAssignedFlight(selectedFlight);

        String assignedEquipmentName = "— Не назначено —";
        String assignedBrigadeName = "— Не назначено —";

        for (Resource resource : assignedResources) {
            if (resource instanceof Equipment) {
                assignedEquipmentName = ((Equipment) resource).getModelName();
            } else if (resource instanceof Brigade) {
                assignedBrigadeName = ((Brigade) resource).getSpecialization();
            }
        }
        List<Equipment> availableEquipment = resourceRepository.findAvailableEquipment();
        ObservableList<Equipment> equipmentList = FXCollections.observableArrayList(availableEquipment);
        equipmentComboBox.setItems(equipmentList);

        List<Brigade> availableBrigades = resourceRepository.findAvailableBrigades();
        ObservableList<Brigade> brigadeList = FXCollections.observableArrayList(availableBrigades);
        brigadeComboBox.setItems(brigadeList);
        detailsPanel.setVisible(true);
        detailsPanel.setManaged(true);
        detailsPanel.getParent().requestLayout();

        log("🔍 Открыты детали рейса " + selectedFlight.getFlightNumber());
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

        if (selectedFlight.getAircraft() == null ||
                selectedFlight.getAircraft().getCompartments() == null ||
                selectedFlight.getAircraft().getCompartments().isEmpty()) {
            log("⚠ У рейса нет ВС или отсеков!");
            return;
        }

        Flight freshFlight = flightRepository.findByIdWithDetails(selectedFlight.getId());
        if (freshFlight == null || freshFlight.getAircraft() == null) {
            log(" Ошибка: не удалось загрузить актуальные данные рейса!");
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

            updateDetailsPanel(freshFlight);

            loadFlights();
        } else {
            log("❌ Не удалось добавить груз: превышен лимит веса!");
        }
    }
    @FXML
    private void handleRefresh() {
        log("🔄 Обновление данных...");
        loadFlights();
    }

    @FXML
    private void handleSave() {
        if (selectedFlight == null) {
            log(" Нечего сохранять!");
            return;
        }

        try {
            flightRepository.update(selectedFlight);
            log("💾 Рейс " + selectedFlight.getFlightNumber() + " сохранён в БД.");
        } catch (Exception e) {
            log("❌ Ошибка сохранения: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void updateDetailsPanel(Flight flight) {

        detailFlightNumber.setText(flight.getFlightNumber());
        detailAircraftType.setText(flight.getAircraftType().getDisplayName());
        detailStatus.setText(flight.getStatus().toString());

        double totalWeight = flight.getTotalCargoWeight() != null
                ? flight.getTotalCargoWeight() : 0;
        detailTotalWeight.setText(String.format("%.0f кг", totalWeight));

        compartmentsContainer.getChildren().clear();
        if (flight.getAircraft() != null && flight.getAircraft().getCompartments() != null) {
            for (Compartment compartment : flight.getAircraft().getCompartments()) {
                VBox compartmentBox = new VBox(5);
                compartmentBox.setStyle("-fx-padding: 10; -fx-background-color: #f8f9fa; -fx-background-radius: 5;");

                Label nameLabel = new Label(compartment.getName());
                nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");

                double current = compartment.getCurrentWeight() != null ? compartment.getCurrentWeight() : 0;
                double max = compartment.getMaxWeight() != null ? compartment.getMaxWeight() : 1;

                Label weightLabel = new Label(String.format("Загрузка: %.0f / %.0f кг (%.0f%%)",
                        current, max, (current / max) * 100));

                ProgressBar progressBar = new ProgressBar();
                progressBar.setProgress(current / max);
                progressBar.setPrefWidth(250);


                if (current / max > 0.9) {
                    progressBar.setStyle("-fx-accent: #e74c3c;");
                } else if (current / max > 0.7) {
                    progressBar.setStyle("-fx-accent: #f39c12;");
                } else {
                    progressBar.setStyle("-fx-accent: #27ae60;");
                }

                compartmentBox.getChildren().addAll(nameLabel, weightLabel, progressBar);
                compartmentsContainer.getChildren().add(compartmentBox);
            }
        }

        log("🔄 Панель обновлена. Общий вес: " + totalWeight + " кг");
    }
    private void log(String message) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        logArea.appendText("[" + timestamp + "] " + message + "\n");
    }
}