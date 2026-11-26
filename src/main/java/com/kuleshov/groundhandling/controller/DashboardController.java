package com.kuleshov.groundhandling.controller;

import com.kuleshov.groundhandling.CSVExporter;
import com.kuleshov.groundhandling.MainApp;
import com.kuleshov.groundhandling.domain.*;
import com.kuleshov.groundhandling.repository.AircraftRepositoryImpl;
import com.kuleshov.groundhandling.repository.FlightRepositoryImpl;
import com.kuleshov.groundhandling.repository.ResourceRepositoryImpl;
import com.kuleshov.groundhandling.service.*;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    @FXML private Label totalFlightsLabel;
    @FXML private Label activeFlightsLabel;
    @FXML private Label completedFlightsLabel;

    @FXML private VBox detailsPanel;
    @FXML private Label detailFlightNumber;
    @FXML private Label detailTailNumber;
    @FXML private Label detailAircraftType;
    @FXML private Label detailTotalWeight;

    @FXML private ComboBox<FlightStatus> statusComboBox;
    @FXML private ComboBox<Compartment> compartmentComboBox;
    @FXML private TextField cargoWeightField;
    @FXML private VBox compartmentsContainer;

    @FXML private ComboBox<Equipment> equipmentComboBox;
    @FXML private ComboBox<Brigade> brigadeComboBox;

    @FXML private TextField passengerWeightField;

    @FXML private TextArea logArea;

    private final FlightRepositoryImpl flightRepository = new FlightRepositoryImpl();
    private final ResourceRepositoryImpl resourceRepository = new ResourceRepositoryImpl();
    private final AircraftRepositoryImpl aircraftRepository = new AircraftRepositoryImpl();
    private final WeightBalanceService weightBalanceService = new WeightBalanceService();
    private ResourceAllocationService allocationService;
    private FlightManagementService flightManagementService;

    /** Кеш для колонок таблицы: заполняется одним запросом при loadFlights(). */
    private final Map<Long, Equipment> equipmentByFlightId = new HashMap<>();
    private final Map<Long, Brigade> brigadeByFlightId = new HashMap<>();

    /** Флаг: setValue на статусе вызывается программно, а не пользователем. */
    private boolean updatingStatus = false;

    private Flight selectedFlight;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        allocationService = new ResourceAllocationService(resourceRepository);
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
        Timeline autoRefreshTimeline = new Timeline(new KeyFrame(
                Duration.seconds(AUTO_REFRESH_INTERVAL_SECONDS), e -> {
            if (selectedFlight == null) {
                loadFlights();
            }
        }));
        autoRefreshTimeline.setCycleCount(Timeline.INDEFINITE);
        autoRefreshTimeline.play();
    }

    private void setupStatusComboBox() {
        statusComboBox.setItems(FXCollections.observableArrayList(FlightStatus.values()));
        statusComboBox.setOnAction(e -> {
            if (updatingStatus || selectedFlight == null) return;
            FlightStatus newStatus = statusComboBox.getValue();
            if (newStatus == null) return;

            OperationResult result = flightManagementService.updateFlightStatus(
                    selectedFlight.getId(), newStatus);
            if (result.isSuccess()) {
                selectedFlight.setStatus(newStatus);
                log("Статус рейса " + selectedFlight.getFlightNumber() + " изменён на " + newStatus);
                loadFlights();
            } else {
                showWarning("Ошибка", result.getMessage());
                // откатываем визуально на фактический статус — программно, без всплывающих событий
                updatingStatus = true;
                statusComboBox.setValue(selectedFlight.getStatus());
                updatingStatus = false;
            }
        });
    }

    private void setupCompartmentComboBox() {
        compartmentComboBox.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Compartment item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null
                        : String.format("%s (%.0f / %.0f кг)",
                        item.getName(), item.getCurrentWeight(), item.getMaxWeight()));
            }
        });
        compartmentComboBox.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Compartment item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null
                        : String.format("%s (%.0f / %.0f кг)",
                        item.getName(), item.getCurrentWeight(), item.getMaxWeight()));
            }
        });
    }

    private void setupEquipmentComboBoxDisplay() {
        equipmentComboBox.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Equipment item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    String status = item.isFree() ? " (Свободен)" : " (Занят)";
                    setText(item.getModelName() + status);
                }
            }
        });
        equipmentComboBox.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Equipment item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getModelName());
            }
        });
    }

    private void setupBrigadeComboBoxDisplay() {
        brigadeComboBox.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Brigade item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    String status = item.isFree() ? " (Свободна)" : " (Занята)";
                    setText(item.getSpecialization() + status);
                }
            }
        });
        brigadeComboBox.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Brigade item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getSpecialization());
            }
        });
    }

    private void setupTableColumns() {
        flightNumberColumn.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getFlightNumber()));

        tailNumberColumn.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getAircraft() != null
                        ? data.getValue().getAircraft().getTailNumber() : "—"));

        aircraftTypeColumn.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getAircraft() != null
                        ? data.getValue().getAircraft().getAircraftType().getDisplayName() : "—"));

        statusColumn.setCellValueFactory(data ->
                new SimpleStringProperty(
                        data.getValue().getStatus() != null ? data.getValue().getStatus().toString() : "—"));

        statusColumn.setCellFactory(col -> new TableCell<>() {
            private final Label badge = new Label();
            {
                badge.getStyleClass().add("status-badge");
                setAlignment(Pos.CENTER);
            }
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    return;
                }
                Flight flight = getTableView().getItems().get(getIndex());
                badge.setText(item);
                badge.getStyleClass().removeIf(s -> s.startsWith("status-") && !s.equals("status-badge"));
                badge.getStyleClass().add(getStatusStyleClass(flight.getStatus()));
                setGraphic(badge);
            }
        });

        weightColumn.setCellValueFactory(data -> {
            Flight f = data.getValue();
            double max = f.getAircraft() != null ? f.getAircraft().getMaxPayload() : 0;
            return new SimpleStringProperty(String.format("%.0f / %.0f кг", f.getTotalWeight(), max));
        });

        // Колонки читают из кеша, собранного одним запросом в loadFlights() —
        // вместо запроса к БД на каждую ячейку
        equipmentColumn.setCellValueFactory(data -> {
            Equipment e = equipmentByFlightId.get(data.getValue().getId());
            return new SimpleStringProperty(e != null ? e.getModelName() : "—");
        });

        brigadeColumn.setCellValueFactory(data -> {
            Brigade b = brigadeByFlightId.get(data.getValue().getId());
            return new SimpleStringProperty(b != null ? b.getSpecialization() : "—");
        });

        actionsColumn.setCellFactory(col -> new TableCell<>() {
            private final Button viewButton = new Button("Детали");
            {
                viewButton.setOnAction(event ->
                        showFlightDetails(getTableView().getItems().get(getIndex())));
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                setGraphic(empty ? null : viewButton);
            }
        });
    }

    private void loadFlights() {
        try {
            List<Flight> flights = flightRepository.findAll();

            // Один запрос за всеми ресурсами вместо запроса на каждую ячейку таблицы
            equipmentByFlightId.clear();
            brigadeByFlightId.clear();
            for (Resource r : resourceRepository.findAll()) {
                if (r.getAssignedFlight() == null) continue;
                Long flightId = r.getAssignedFlight().getId();
                if (r instanceof Equipment e) {
                    equipmentByFlightId.put(flightId, e);
                } else if (r instanceof Brigade b) {
                    brigadeByFlightId.put(flightId, b);
                }
            }

            flightsTable.setItems(FXCollections.observableArrayList(flights));
            totalFlightsLabel.setText(String.valueOf(flights.size()));
            activeFlightsLabel.setText(String.valueOf(
                    flights.stream().filter(f -> f.getStatus() != FlightStatus.DEPARTED).count()));
            completedFlightsLabel.setText(String.valueOf(
                    flights.stream().filter(f -> f.getStatus() == FlightStatus.DEPARTED).count()));
            flightsTable.refresh();
        } catch (Exception e) {
            log("Ошибка загрузки рейсов: " + e.getMessage());
            showError("Ошибка загрузки данных",
                    "Не удалось загрузить список рейсов из базы данных.\n" + e.getMessage());
        }
    }

    private void showFlightDetails(Flight flight) {
        selectedFlight = flightRepository.findByIdWithDetails(flight.getId());
        if (selectedFlight == null) {
            showError("Ошибка", "Не удалось загрузить детали рейса.");
            return;
        }

        detailFlightNumber.setText(selectedFlight.getFlightNumber());
        detailTailNumber.setText(selectedFlight.getAircraft() != null
                ? selectedFlight.getAircraft().getTailNumber() : "—");
        detailAircraftType.setText(selectedFlight.getAircraft() != null
                ? selectedFlight.getAircraft().getAircraftType().getDisplayName() : "—");

        // Программная установка: флаг гасит событие обработчика
        updatingStatus = true;
        statusComboBox.setValue(selectedFlight.getStatus());
        updatingStatus = false;

        double maxPayload = selectedFlight.getAircraft() != null
                ? selectedFlight.getAircraft().getMaxPayload() : 0.0;
        detailTotalWeight.setText(String.format("Груз: %.0f кг | Пасс.: %.0f кг\nВсего: %.0f / %.0f кг",
                selectedFlight.getTotalCargoWeight(),
                selectedFlight.getPassengerWeight(),
                selectedFlight.getTotalWeight(),
                maxPayload));

        compartmentsContainer.getChildren().clear();
        compartmentComboBox.getItems().clear();

        if (selectedFlight.getAircraft() != null && !selectedFlight.getAircraft().getCompartments().isEmpty()) {
            for (Compartment comp : selectedFlight.getAircraft().getCompartments()) {
                compartmentComboBox.getItems().add(comp);
                compartmentsContainer.getChildren().add(createCompartmentBox(comp));
            }
            compartmentComboBox.getSelectionModel().selectFirst();
        }

        List<Resource> allResources = resourceRepository.findAll();
        equipmentComboBox.setItems(FXCollections.observableArrayList(
                allResources.stream().filter(r -> r instanceof Equipment).map(r -> (Equipment) r).toList()));
        brigadeComboBox.setItems(FXCollections.observableArrayList(
                allResources.stream().filter(r -> r instanceof Brigade).map(r -> (Brigade) r).toList()));

        // Выбор по equals(id) — сущности из разных сессий, но сравнение работает корректно
        for (Resource r : flightManagementService.getAssignedResources(selectedFlight)) {
            if (r instanceof Equipment e) {
                equipmentComboBox.getSelectionModel().select(e);
            } else if (r instanceof Brigade b) {
                brigadeComboBox.getSelectionModel().select(b);
            }
        }

        Double passengerWeight = selectedFlight.getPassengerWeight();
        passengerWeightField.setText(passengerWeight != null
                ? String.valueOf(passengerWeight.intValue()) : "0");

        addCargoHistoryDisplay();

        detailsPanel.setVisible(true);
        detailsPanel.setManaged(true);
        detailsPanel.getParent().requestLayout();
        log("Открыты детали рейса " + selectedFlight.getFlightNumber());
    }

    private void addCargoHistoryDisplay() {
        if (selectedFlight.getCargoList().isEmpty()) {
            return;
        }
        Label historyLabel = new Label("История загрузок:");
        historyLabel.setStyle("-fx-font-weight: bold; -fx-padding: 10 0 5 0; -fx-font-size: 13px;");
        compartmentsContainer.getChildren().add(historyLabel);

        for (Cargo cargo : selectedFlight.getCargoList()) {
            Label cargoItem = new Label(String.format("- %.0f кг: %s (отсек: %s)",
                    cargo.getWeight(),
                    cargo.getDescription() != null ? cargo.getDescription() : "Груз",
                    cargo.getCompartment() != null ? cargo.getCompartment().getName() : "Неизвестно"));
            cargoItem.setStyle("-fx-padding: 2 0 2 10; -fx-text-fill: #555; -fx-font-size: 12px;");
            compartmentsContainer.getChildren().add(cargoItem);
        }
    }

    private VBox createCompartmentBox(Compartment compartment) {
        VBox box = new VBox(5);
        box.setStyle("-fx-padding: 10; -fx-background-color: #f8f9fa; -fx-background-radius: 5;");
        double max = compartment.getMaxWeight();
        double ratio = max == 0 ? 0 : compartment.getCurrentWeight() / max;

        Label nameLabel = new Label(compartment.getName());
        nameLabel.setStyle("-fx-font-weight: bold;");
        Label weightLabel = new Label(String.format("Загрузка: %.0f / %.0f кг (%.0f%%)",
                compartment.getCurrentWeight(), max, ratio * 100));

        ProgressBar progressBar = new ProgressBar();
        progressBar.setProgress(Math.min(ratio, 1.0));
        progressBar.setPrefWidth(250);
        String color = ratio > 0.9 ? "#e74c3c" : (ratio > 0.7 ? "#f39c12" : "#27ae60");
        progressBar.setStyle("-fx-accent: " + color + ";");

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
        String weightText = cargoWeightField.getText().trim();
        if (!weightText.isEmpty()) {
            weight = Double.parseDouble(weightText);
        }

        OperationResult result = flightManagementService.addCargoToFlight(
                selectedFlight.getId(),
                compartmentComboBox.getValue().getId(),
                weight,
                null // описание груза; при желании добавьте поле в форму
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
                selectedFlight.getId(), equipmentId, brigadeId);

        if (result.isSuccess()) {
            log(result.getMessage());
            showFlightDetails(selectedFlight); // перечитываем: занятость ресурсов обновилась
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
        OperationResult result = allocationService.allocateResources(selectedFlight.getId());
        if (result.isSuccess()) {
            log(result.getMessage());
            loadFlights();
            showFlightDetails(selectedFlight);
        } else {
            showWarning("Ошибка распределения", result.getMessage());
            log("Ошибка: " + result.getMessage());
        }
    }

    @FXML
    private void handleDelete() {
        if (selectedFlight == null) return;
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Удалить рейс " + selectedFlight.getFlightNumber() + "?");
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
            controller.setOnFlightCreated(() -> {
                loadFlights();
                log("Новый рейс создан");
            });
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
    private void handleRefresh() {
        log("Обновление данных...");
        loadFlights();
    }
    @FXML
    private void handleExportCsv() {
        try {
            List<Flight> flights = flightsTable.getItems();

            if (flights.isEmpty()) {
                log("Экспорт отменён: список рейсов пуст");
                return;
            }

            FileChooser chooser = new FileChooser();
            chooser.setTitle("Сохранить отчёт по рейсам");
            String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            chooser.setInitialFileName("рейсы_" + date + ".csv");
            chooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("CSV файлы", "*.csv"));

            File file = chooser.showSaveDialog(flightsTable.getScene().getWindow());
            if (file == null) {
                return; // пользователь передумал — не ошибка
            }

            CSVExporter.exportFlights(flights, file.getAbsolutePath());
            log("CSV-отчёт сохранён: " + file.getAbsolutePath());

        } catch (Exception e) {
            e.printStackTrace();
            log("Ошибка экспорта: " + e.getMessage());
            showError("Ошибка экспорта", "Не удалось сохранить файл.\n" + e.getMessage());
        }
    }



    @FXML
    private void handleUpdatePassengerWeight() {
        if (selectedFlight == null) return;

        double weight;
        try {
            weight = Double.parseDouble(passengerWeightField.getText().trim());
        } catch (NumberFormatException e) {
            showWarning("Неверный формат", "Введите корректное числовое значение веса.");
            return;
        }
        if (weight < 0) {
            showWarning("Ошибка", "Вес не может быть отрицательным");
            return;
        }

        // Доменная проверка лимита и сохранение — в сервисе, не здесь
        OperationResult result = flightManagementService.updatePassengerWeight(
                selectedFlight.getId(), weight);

        if (result.isSuccess()) {
            log(result.getMessage());
            showFlightDetails(selectedFlight);
            loadFlights();
        } else {
            showWarning("Превышение лимита!", result.getMessage());
            log("Ошибка: " + result.getMessage());
        }
    }

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
        Platform.runLater(() -> logArea.appendText("[" + time + "] " + msg + "\n"));
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
