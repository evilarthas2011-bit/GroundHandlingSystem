package com.kuleshov.groundhandling.controller;

import com.kuleshov.groundhandling.MainApp;
import com.kuleshov.groundhandling.domain.*;
import com.kuleshov.groundhandling.repository.AircraftRepository;
import com.kuleshov.groundhandling.repository.FlightRepository;
import com.kuleshov.groundhandling.repository.ResourceRepository;
import com.kuleshov.groundhandling.service.ResourceAllocationService;
import com.kuleshov.groundhandling.service.WeightBalanceService;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
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
import javafx.util.Duration;

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
    @FXML private TableColumn<Flight, String> tailNumberColumn;
    @FXML private TableColumn<Flight, String> aircraftTypeColumn;
    @FXML private TableColumn<Flight, String> statusColumn;
    @FXML private TableColumn<Flight, String> weightColumn;
    @FXML private TableColumn<Flight, String> equipmentColumn;
    @FXML private TableColumn<Flight, String> brigadeColumn;
    @FXML private TableColumn<Flight, Void> actionsColumn;

    @FXML private Label totalFlightsLabel, activeFlightsLabel, completedFlightsLabel;
    @FXML private VBox detailsPanel;
    @FXML private Label detailFlightNumber, detailTailNumber, detailAircraftType, detailTotalWeight;
    @FXML private ComboBox<FlightStatus> statusComboBox;
    @FXML private ComboBox<Compartment> compartmentComboBox;
    @FXML private TextField cargoWeightField;
    @FXML private VBox compartmentsContainer;
    @FXML private ComboBox<Equipment> equipmentComboBox;
    @FXML private ComboBox<Brigade> brigadeComboBox;

    @FXML private Button refreshButton, newFlightButton, closeDetailsButton;
    @FXML private Button autoAllocateButton, addCargoButton, unloadCargoButton, saveButton, deleteButton;
    @FXML private TextArea logArea;

    private FlightRepository flightRepository;
    private ResourceRepository resourceRepository;
    private WeightBalanceService weightBalanceService;
    private ResourceAllocationService allocationService;
    private AircraftRepository aircraftRepository;
    private Flight selectedFlight;
    private Timeline autoRefreshTimeline;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        flightRepository = new FlightRepository();
        resourceRepository = new ResourceRepository();
        weightBalanceService = new WeightBalanceService();
        allocationService = new ResourceAllocationService(resourceRepository);
        aircraftRepository = new AircraftRepository();

        setupTableColumns();
        setupStatusComboBox();
        setupCompartmentComboBox();
        setupEquipmentComboBoxDisplay();
        setupBrigadeComboBoxDisplay();
        loadFlights();

        btnFlights.setOnAction(e -> switchToFlights());
        btnResources.setOnAction(e -> switchToResources());

        startAutoRefresh();
        log("✅ Система инициализирована. Автообновление каждые 5 сек.");
    }

    private void startAutoRefresh() {
        autoRefreshTimeline = new Timeline(new KeyFrame(Duration.seconds(5), e -> {
            if (selectedFlight == null) loadFlights();
        }));
        autoRefreshTimeline.setCycleCount(Timeline.INDEFINITE);
        autoRefreshTimeline.play();
    }

    private void setupStatusComboBox() {
        statusComboBox.setItems(FXCollections.observableArrayList(FlightStatus.values()));
        statusComboBox.setOnAction(e -> {
            if (selectedFlight != null) selectedFlight.setStatus(statusComboBox.getValue());
        });
    }

    private void setupCompartmentComboBox() {
        compartmentComboBox.setCellFactory(param -> new ListCell<Compartment>() {
            @Override
            protected void updateItem(Compartment item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) setText(null);
                else {
                    double cur = item.getCurrentWeight() != null ? item.getCurrentWeight() : 0;
                    double max = item.getMaxWeight() != null ? item.getMaxWeight() : 0;
                    setText(String.format("%s (%.0f / %.0f кг)", item.getName(), cur, max));
                }
            }
        });
        compartmentComboBox.setButtonCell(new ListCell<Compartment>() {
            @Override
            protected void updateItem(Compartment item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) setText(null);
                else {
                    double cur = item.getCurrentWeight() != null ? item.getCurrentWeight() : 0;
                    double max = item.getMaxWeight() != null ? item.getMaxWeight() : 0;
                    setText(String.format("%s (%.0f / %.0f кг)", item.getName(), cur, max));
                }
            }
        });
    }

    private void setupEquipmentComboBoxDisplay() {
        equipmentComboBox.setCellFactory(param -> new ListCell<Equipment>() {
            @Override
            protected void updateItem(Equipment item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    String status = item.getAssignedFlight() != null ? " (Занят)" : " (Свободен)";
                    setText(item.getModelName() + status);
                }
            }
        });
        equipmentComboBox.setButtonCell(new ListCell<Equipment>() {
            @Override
            protected void updateItem(Equipment item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getModelName());
                }
            }
        });
    }

    private void setupBrigadeComboBoxDisplay() {
        brigadeComboBox.setCellFactory(param -> new ListCell<Brigade>() {
            @Override
            protected void updateItem(Brigade item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    String status = item.getAssignedFlight() != null ? " (Занята)" : " (Свободна)";
                    setText(item.getSpecialization() + status);
                }
            }
        });
        brigadeComboBox.setButtonCell(new ListCell<Brigade>() {
            @Override
            protected void updateItem(Brigade item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getSpecialization());
                }
            }
        });
    }

    private void setupTableColumns() {
        flightNumberColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getFlightNumber()));
        tailNumberColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
                data.getValue().getAircraft() != null ? data.getValue().getAircraft().getTailNumber() : "—"));
        aircraftTypeColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getAircraftType().getDisplayName()));
        statusColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getStatus().toString()));

        statusColumn.setCellFactory(col -> new TableCell<>() {
            private final Label badge = new Label();
            { badge.getStyleClass().add("status-badge"); setGraphic(badge); setAlignment(javafx.geometry.Pos.CENTER); }
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setGraphic(null); return; }
                Flight flight = getTableView().getItems().get(getIndex());
                badge.setText(item);
                badge.getStyleClass().removeIf(s -> s.startsWith("status-") && !s.equals("status-badge"));
                badge.getStyleClass().add(getStatusStyleClass(flight.getStatus()));
                setGraphic(badge);
            }
        });

        weightColumn.setCellValueFactory(data -> {
            Flight f = data.getValue();
            double cur = f.getTotalCargoWeight() != null ? f.getTotalCargoWeight() : 0;
            double max = f.getAircraft() != null ? f.getAircraft().getMaxPayload() : 0;
            return new javafx.beans.property.SimpleStringProperty(String.format("%.0f / %.0f кг", cur, max));
        });

        equipmentColumn.setCellValueFactory(data -> {
            try {
                List<Resource> res = flightRepository.findAssignedResources(data.getValue());
                for (Resource r : res) if (r instanceof Equipment) return new javafx.beans.property.SimpleStringProperty(((Equipment) r).getModelName());
            } catch (Exception ignored) {}
            return new javafx.beans.property.SimpleStringProperty("—");
        });

        brigadeColumn.setCellValueFactory(data -> {
            try {
                List<Resource> res = flightRepository.findAssignedResources(data.getValue());
                for (Resource r : res) if (r instanceof Brigade) return new javafx.beans.property.SimpleStringProperty(((Brigade) r).getSpecialization());
            } catch (Exception ignored) {}
            return new javafx.beans.property.SimpleStringProperty("—");
        });

        actionsColumn.setCellFactory(col -> new TableCell<>() {
            private final Button viewButton = new Button("👁 Детали");
            { viewButton.setOnAction(event -> showFlightDetails(getTableView().getItems().get(getIndex()))); }
            @Override
            protected void updateItem(Void item, boolean empty) { setGraphic(empty ? null : viewButton); }
        });
    }

    private void loadFlights() {
        try {
            List<Flight> flights = flightRepository.findAll();
            flightsTable.setItems(FXCollections.observableArrayList(flights));
            totalFlightsLabel.setText(String.valueOf(flights.size()));
            activeFlightsLabel.setText(String.valueOf(flights.stream().filter(f -> f.getStatus() != FlightStatus.DEPARTED).count()));
            completedFlightsLabel.setText(String.valueOf(flights.stream().filter(f -> f.getStatus() == FlightStatus.DEPARTED).count()));
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void showFlightDetails(Flight flight) {
        selectedFlight = flightRepository.findByIdWithDetails(flight.getId());
        if (selectedFlight == null) return;

        detailFlightNumber.setText(selectedFlight.getFlightNumber());
        detailTailNumber.setText(selectedFlight.getAircraft() != null ? selectedFlight.getAircraft().getTailNumber() : "—");
        detailAircraftType.setText(selectedFlight.getAircraftType().getDisplayName());
        statusComboBox.setValue(selectedFlight.getStatus());

        double totalWeight = selectedFlight.getTotalCargoWeight() != null ? selectedFlight.getTotalCargoWeight() : 0;
        detailTotalWeight.setText(String.format("%.0f кг", totalWeight));

        compartmentsContainer.getChildren().clear();
        compartmentComboBox.getItems().clear();

        if (selectedFlight.getAircraft() != null) {
            for (Compartment comp : selectedFlight.getAircraft().getCompartments()) {
                compartmentComboBox.getItems().add(comp);
                compartmentsContainer.getChildren().add(createCompartmentBox(comp));
            }
            if (!compartmentComboBox.getItems().isEmpty()) compartmentComboBox.getSelectionModel().selectFirst();
        }

        List<Equipment> allEquipment = resourceRepository.findAll().stream()
                .filter(r -> r instanceof Equipment).map(r -> (Equipment) r).toList();
        equipmentComboBox.setItems(FXCollections.observableArrayList(allEquipment));

        List<Brigade> allBrigades = resourceRepository.findAll().stream()
                .filter(r -> r instanceof Brigade).map(r -> (Brigade) r).toList();
        brigadeComboBox.setItems(FXCollections.observableArrayList(allBrigades));


        setupEquipmentComboBoxDisplay();
        setupBrigadeComboBoxDisplay();

        List<Resource> assigned = resourceRepository.findByAssignedFlight(selectedFlight);
        for (Resource r : assigned) {
            if (r instanceof Equipment) equipmentComboBox.getSelectionModel().select((Equipment) r);
            if (r instanceof Brigade) brigadeComboBox.getSelectionModel().select((Brigade) r);
        }

        detailsPanel.setVisible(true);
        detailsPanel.setManaged(true);
        detailsPanel.getParent().requestLayout();
        log("🔍 Открыты детали рейса " + selectedFlight.getFlightNumber());
    }
    private VBox createCompartmentBox(Compartment compartment) {
        VBox box = new VBox(5);
        box.setStyle("-fx-padding: 10; -fx-background-color: #f8f9fa; -fx-background-radius: 5;");
        double cur = compartment.getCurrentWeight() != null ? compartment.getCurrentWeight() : 0;
        double max = compartment.getMaxWeight() != null ? compartment.getMaxWeight() : 1;
        double ratio = cur / max;

        Label nameLabel = new Label(compartment.getName());
        nameLabel.setStyle("-fx-font-weight: bold;");
        Label weightLabel = new Label(String.format("Загрузка: %.0f / %.0f кг (%.0f%%)", cur, max, ratio * 100));

        ProgressBar progressBar = new ProgressBar();
        progressBar.setProgress(ratio);
        progressBar.setPrefWidth(250);
        progressBar.setStyle("-fx-accent: " + (ratio > 0.9 ? "#e74c3c" : ratio > 0.7 ? "#f39c12" : "#27ae60") + ";");

        box.getChildren().addAll(nameLabel, weightLabel, progressBar);
        return box;
    }

    @FXML
    private void handleAddCargo() {
        if (selectedFlight == null || compartmentComboBox.getValue() == null) {
            log("⚠ Выберите отсек!");
            return;
        }

        double weight = 500.0;
        if (cargoWeightField != null) {
            try {
                String weightText = cargoWeightField.getText().trim();
                if (!weightText.isEmpty()) {
                    weight = Double.parseDouble(weightText);
                    if (weight <= 0) {
                        log("❌ Вес должен быть больше 0!");
                        return;
                    }
                }
            } catch (NumberFormatException e) {
                log("❌ Неверный формат веса!");
                return;
            }
        }

        Compartment comp = compartmentComboBox.getValue();
        Cargo cargo = new Cargo(weight, "Груз", comp, selectedFlight);

        if (weightBalanceService.validateAndAddCargo(selectedFlight, cargo)) {
            aircraftRepository.update(selectedFlight.getAircraft());
            flightRepository.update(selectedFlight);
            log("✅ +" + weight + " кг в '" + comp.getName() + "'");
            showFlightDetails(selectedFlight);
            loadFlights();
            if (cargoWeightField != null) cargoWeightField.clear();
        } else {
            log("❌ Превышен лимит веса!");
        }
    }

    @FXML
    private void handleUnloadCargo() {
        if (selectedFlight == null || compartmentComboBox.getValue() == null) {
            log("⚠ Выберите отсек!");
            return;
        }

        Compartment comp = compartmentComboBox.getValue();
        if (weightBalanceService.unloadLastCargo(selectedFlight, comp)) {
            aircraftRepository.update(selectedFlight.getAircraft());
            flightRepository.update(selectedFlight);
            log("➖ Груз из '" + comp.getName() + "' разгружен");
            showFlightDetails(selectedFlight);
            loadFlights();
        } else {
            log(" Отсек пуст!");
        }
    }

    @FXML
    private void handleSave() {
        if (selectedFlight == null) return;
        try {
            List<Resource> old = resourceRepository.findByAssignedFlight(selectedFlight);
            for (Resource r : old) { r.setAssignedFlight(null); resourceRepository.update(r); }

            Equipment eq = equipmentComboBox.getValue();
            Brigade br = brigadeComboBox.getValue();
            if (eq != null) { eq.setAssignedFlight(selectedFlight); resourceRepository.update(eq); log("✅ Техника: " + eq.getModelName()); }
            if (br != null) { br.setAssignedFlight(selectedFlight); resourceRepository.update(br); log("✅ Бригада: " + br.getSpecialization()); }

            flightRepository.update(selectedFlight);
            log("💾 Рейс " + selectedFlight.getFlightNumber() + " сохранён.");
            loadFlights();
        } catch (Exception e) { log("❌ Ошибка: " + e.getMessage()); }
    }

    @FXML
    private void handleCloseDetails() {
        detailsPanel.setVisible(false);
        detailsPanel.setManaged(false);
        selectedFlight = null;
        flightsTable.getSelectionModel().clearSelection();
    }

    @FXML
    private void handleAutoAllocate() {
        if (selectedFlight == null) return;
        try {
            allocationService.autoAllocate(selectedFlight);
            log("✨ Авто-распределение выполнено");
            loadFlights();
            showFlightDetails(selectedFlight);
        } catch (Exception e) { log("❌ Ошибка: " + e.getMessage()); }
    }

    @FXML
    private void handleDelete() {
        if (selectedFlight == null) return;
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Удалить рейс " + selectedFlight.getFlightNumber() + "?");
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                flightRepository.delete(selectedFlight);
                log("🗑 Рейс удалён.");
                handleCloseDetails();
                loadFlights();
            }
        });
    }

    @FXML
    private void handleNewFlight() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/flight-form.fxml"));
            Parent root = loader.load();
            FlightFormController controller = loader.getController();
            controller.setOnFlightCreated(() -> { loadFlights(); log("✅ Новый рейс создан"); });
            Stage stage = new Stage();
            stage.setTitle("Новый рейс");
            stage.setScene(new Scene(root));
            stage.initOwner(MainApp.getPrimaryStage());
            stage.setResizable(false);
            stage.show();
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML
    private void handleRefresh() { log("🔄 Обновление..."); loadFlights(); }

    private void switchToFlights() {
        btnFlights.getStyleClass().add("active");
        btnResources.getStyleClass().remove("active");
        mainContent.getChildren().clear();
        mainContent.getChildren().add(flightsView);
    }

    private void switchToResources() {
        btnResources.getStyleClass().add("active");
        btnFlights.getStyleClass().remove("active");
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/resources.fxml"));
            Parent resourcesView = loader.load();
            mainContent.getChildren().clear();
            mainContent.getChildren().add(resourcesView);
            log("🚚 Экран ресурсов");
        } catch (Exception e) { e.printStackTrace(); }
    }

    private String getStatusStyleClass(FlightStatus s) {
        if (s == null) return "status-scheduled";
        return switch (s) {
            case SCHEDULED -> "status-scheduled";
            case ARRIVED -> "status-arrived";
            case LOADING -> "status-loading";
            case BOARDING -> "status-boarding";
            case READY -> "status-ready";
            case DEPARTED -> "status-departed";
            case DELAYED -> "status-delayed";
        };
    }

    private void log(String msg) {
        String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        logArea.appendText("[" + time + "] " + msg + "\n");
    }
}