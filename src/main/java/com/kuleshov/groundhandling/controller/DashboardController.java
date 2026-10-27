package com.kuleshov.groundhandling.controller;

import com.kuleshov.groundhandling.MainApp;
import com.kuleshov.groundhandling.domain.*;
import com.kuleshov.groundhandling.repository.AircraftRepository;
import com.kuleshov.groundhandling.repository.FlightRepository;
import com.kuleshov.groundhandling.repository.ResourceRepository;
import com.kuleshov.groundhandling.service.*;
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

    private static final int AUTO_REFRESH_INTERVAL_SECONDS = 5;
    private static final double DEFAULT_CARGO_WEIGHT = 500.0;
    private static final double MIN_CARGO_WEIGHT = 0.1;
    private static final double MAX_CARGO_WEIGHT = 50000.0;

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
    private FlightManagementService flightManagementService;

    private Flight selectedFlight;
    private Timeline autoRefreshTimeline;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        flightRepository = new FlightRepository();
        resourceRepository = new ResourceRepository();
        weightBalanceService = new WeightBalanceService();
        allocationService = new ResourceAllocationService(resourceRepository);
        aircraftRepository = new AircraftRepository();
        flightManagementService = new FlightManagementServiceImpl(
                flightRepository, resourceRepository, aircraftRepository, weightBalanceService
        );

        setupTableColumns();
        setupStatusComboBox();
        setupCompartmentComboBox();
        setupEquipmentComboBoxDisplay();
        setupBrigadeComboBoxDisplay();
        loadFlights();

        btnFlights.setOnAction(e -> switchToFlights());
        btnResources.setOnAction(e -> switchToResources());

        startAutoRefresh();
        log("Система инициализирована. Автообновление каждые " + AUTO_REFRESH_INTERVAL_SECONDS + " сек.");
    }

    private void startAutoRefresh() {
        autoRefreshTimeline = new Timeline(new KeyFrame(Duration.seconds(AUTO_REFRESH_INTERVAL_SECONDS), e -> {
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
                if (empty || item == null) setText(null);
                else {
                    String status = item.getAssignedFlight() != null ? " (Занят)" : " (Свободен)";
                    setText(item.getModelName() + status);
                }
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
                if (empty || item == null) setText(null);
                else {
                    String status = item.getAssignedFlight() != null ? " (Занята)" : " (Свободна)";
                    setText(item.getSpecialization() + status);
                }
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
                List<Resource> res = flightManagementService.getAssignedResources(data.getValue());
                for (Resource r : res) if (r instanceof Equipment) return new javafx.beans.property.SimpleStringProperty(((Equipment) r).getModelName());
            } catch (Exception ignored) {}
            return new javafx.beans.property.SimpleStringProperty("—");
        });

        brigadeColumn.setCellValueFactory(data -> {
            try {
                List<Resource> res = flightManagementService.getAssignedResources(data.getValue());
                for (Resource r : res) if (r instanceof Brigade) return new javafx.beans.property.SimpleStringProperty(((Brigade) r).getSpecialization());
            } catch (Exception ignored) {}
            return new javafx.beans.property.SimpleStringProperty("—");
        });

        actionsColumn.setCellFactory(col -> new TableCell<>() {
            private final Button viewButton = new Button("Детали");
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
        } catch (Exception e) {
            log("Ошибка загрузки рейсов: " + e.getMessage());
            showError("Ошибка загрузки данных", "Не удалось загрузить список рейсов из базы данных.\n" + e.getMessage());
        }
    }

    private void showFlightDetails(Flight flight) {
        selectedFlight = flightRepository.findByIdWithDetails(flight.getId());
        if (selectedFlight == null) {
            showError("Ошибка", "Не удалось загрузить детали рейса.");
            return;
        }

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

        List<Resource> assigned = flightManagementService.getAssignedResources(selectedFlight);
        for (Resource r : assigned) {
            if (r instanceof Equipment) equipmentComboBox.getSelectionModel().select((Equipment) r);
            if (r instanceof Brigade) brigadeComboBox.getSelectionModel().select((Brigade) r);
        }

        addCargoHistoryDisplay();

        detailsPanel.setVisible(true);
        detailsPanel.setManaged(true);
        detailsPanel.getParent().requestLayout();
        log("Открыты детали рейса " + selectedFlight.getFlightNumber());
    }

    private void addCargoHistoryDisplay() {
        if (selectedFlight.getCargoList() != null && !selectedFlight.getCargoList().isEmpty()) {
            Label historyLabel = new Label("История загрузок:");
            historyLabel.setStyle("-fx-font-weight: bold; -fx-padding: 10 0 5 0; -fx-font-size: 13px;");
            compartmentsContainer.getChildren().add(historyLabel);

            for (Cargo cargo : selectedFlight.getCargoList()) {
                Label cargoItem = new Label(String.format("- %.0f кг: %s (отсек: %s)",
                        cargo.getWeight(),
                        cargo.getDescription() != null ? cargo.getDescription() : "Груз",
                        cargo.getCompartment().getName()));
                cargoItem.setStyle("-fx-padding: 2 0 2 10; -fx-text-fill: #555; -fx-font-size: 12px;");
                compartmentsContainer.getChildren().add(cargoItem);
            }
        }
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

    private boolean validateCargoWeight() {
        if (cargoWeightField == null) return true;

        String weightText = cargoWeightField.getText().trim();
        if (weightText.isEmpty()) return true;

        try {
            double weight = Double.parseDouble(weightText);
            if (weight < MIN_CARGO_WEIGHT) {
                showWarning("Неверный вес", "Вес груза должен быть не менее " + MIN_CARGO_WEIGHT + " кг.");
                return false;
            }
            if (weight > MAX_CARGO_WEIGHT) {
                showWarning("Неверный вес", "Вес груза не может превышать " + MAX_CARGO_WEIGHT + " кг.");
                return false;
            }
            return true;
        } catch (NumberFormatException e) {
            showWarning("Неверный формат", "Введите числовое значение веса.");
            return false;
        }
    }

    @FXML
    private void handleAddCargo() {
        if (selectedFlight == null || compartmentComboBox.getValue() == null) {
            showWarning("Внимание", "Выберите отсек!");
            return;
        }

        if (!validateCargoWeight()) return;

        double weight = DEFAULT_CARGO_WEIGHT;
        if (cargoWeightField != null) {
            String weightText = cargoWeightField.getText().trim();
            if (!weightText.isEmpty()) {
                weight = Double.parseDouble(weightText);
            }
        }

        OperationResult result = flightManagementService.addCargoToFlight(
                selectedFlight.getId(),
                compartmentComboBox.getValue().getId(),
                weight
        );

        if (result.isSuccess()) {
            log(result.getMessage());
            showFlightDetails(selectedFlight);
            loadFlights();
            cargoWeightField.clear();
        } else {
            showWarning("Ошибка операции", result.getMessage());
            log("Ошибка: " + result.getMessage());
        }
    }

    @FXML
    private void handleUnloadCargo() {
        if (selectedFlight == null || compartmentComboBox.getValue() == null) {
            showWarning("Внимание", "Выберите отсек!");
            return;
        }

        OperationResult result = flightManagementService.unloadLastCargoFromFlight(
                selectedFlight.getId(),
                compartmentComboBox.getValue().getId()
        );

        if (result.isSuccess()) {
            log(result.getMessage());
            showFlightDetails(selectedFlight);
            loadFlights();
        } else {
            showWarning("Ошибка операции", result.getMessage());
            log("Ошибка: " + result.getMessage());
        }
    }

    @FXML
    private void handleSave() {
        if (selectedFlight == null) return;

        Long equipmentId = equipmentComboBox.getValue() != null ? equipmentComboBox.getValue().getId() : null;
        Long brigadeId = brigadeComboBox.getValue() != null ? brigadeComboBox.getValue().getId() : null;

        OperationResult result = flightManagementService.assignResources(
                selectedFlight.getId(),
                equipmentId,
                brigadeId
        );

        if (result.isSuccess()) {
            log(result.getMessage());
            loadFlights();
        } else {
            showError("Ошибка сохранения", result.getMessage());
            log("Ошибка: " + result.getMessage());
        }
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
            log("Авто-распределение выполнено");
            loadFlights();
            showFlightDetails(selectedFlight);
        } catch (Exception e) {
            log("Ошибка: " + e.getMessage());
            showError("Ошибка распределения", "Не удалось выполнить авто-распределение ресурсов.\n" + e.getMessage());
        }
    }

    @FXML
    private void handleDelete() {
        if (selectedFlight == null) return;
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Удалить рейс " + selectedFlight.getFlightNumber() + "?");
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                OperationResult result = flightManagementService.deleteFlight(selectedFlight.getId());
                if (result.isSuccess()) {
                    log(result.getMessage());
                    handleCloseDetails();
                    loadFlights();
                } else {
                    showError("Ошибка удаления", result.getMessage());
                    log("Ошибка: " + result.getMessage());
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
            controller.setOnFlightCreated(() -> { loadFlights(); log("Новый рейс создан"); });
            Stage stage = new Stage();
            stage.setTitle("Новый рейс");
            stage.setScene(new Scene(root));
            stage.initOwner(MainApp.getPrimaryStage());
            stage.setResizable(false);
            stage.show();
        } catch (Exception e) {
            log("Ошибка открытия формы: " + e.getMessage());
            showError("Ошибка", "Не удалось открыть форму создания рейса.");
        }
    }

    @FXML
    private void handleRefresh() { log("Обновление данных..."); loadFlights(); }

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
            log("Переключено на экран ресурсов");
        } catch (Exception e) {
            log("Ошибка загрузки экрана ресурсов: " + e.getMessage());
            showError("Ошибка", "Не удалось загрузить экран ресурсов.");
        }
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

    private void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showWarning(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}