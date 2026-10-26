package com.kuleshov.groundhandling.controller;

import com.kuleshov.groundhandling.HibernateUtil;
import com.kuleshov.groundhandling.domain.*;
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
    //иаблицы рейсов
    @FXML
    private TableView<Flight> flightsTable;
    @FXML
    private TableColumn<Flight, String> flightNumberColumn;
    @FXML
    private TableColumn<Flight, String> aircraftTypeColumn;
    @FXML
    private TableColumn<Flight, String> statusColumn;
    @FXML
    private TableColumn<Flight, String> weightColumn;
    @FXML
    private TableColumn<Flight, String> equipmentColumn;
    @FXML
    private TableColumn<Flight, String> brigadeColumn;
    @FXML
    private TableColumn<Flight, Void> actionsColumn;

    // статистики
    @FXML
    private Label totalFlightsLabel;
    @FXML
    private Label activeFlightsLabel;
    @FXML
    private Label completedFlightsLabel;

    // Боковая панель деталей
    @FXML
    private VBox detailsPanel;
    @FXML
    private Label detailFlightNumber;
    @FXML
    private Label detailAircraftType;
    @FXML
    private Label detailStatus;
    @FXML
    private Label detailTotalWeight;
    @FXML
    private VBox compartmentsContainer;
    @FXML
    private ComboBox<Equipment> equipmentComboBox;
    @FXML
    private ComboBox<Brigade> brigadeComboBox;

    // Кнопки
    @FXML
    private Button refreshButton;
    @FXML
    private Button closeDetailsButton;
    @FXML
    private Button autoAllocateButton;
    @FXML
    private Button addCargoButton;
    @FXML
    private Button saveButton;

    // Лог событий
    @FXML
    private TextArea logArea;

    //Сервисы и репозитории
    private FlightRepository flightRepository;
    private ResourceRepository resourceRepository;
    private WeightBalanceService weightBalanceService;
    private ResourceAllocationService allocationService;

    // Выбранный рейс
    private Flight selectedFlight;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Инициализация репозиториев и сервисов
        flightRepository = new FlightRepository();
        resourceRepository = new ResourceRepository();
        weightBalanceService = new WeightBalanceService();
        allocationService = new ResourceAllocationService(resourceRepository);

        // Настройка таблицы
        setupTableColumns();

        // Загрузка данных
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

        // Колонки с ресурсами (техника и бригада)
        equipmentColumn.setCellValueFactory(data -> {
            Flight flight = data.getValue();
            // Найти назначенную технику
            List<Resource> resources = resourceRepository.findAvailable(); // Здесь нужен другой метод
            return new javafx.beans.property.SimpleStringProperty("—"); // Заглушка
        });

        brigadeColumn.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty("—")); // Заглушка

        // Колонка действий с кнопкой
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

        // Обработчик клика на строку таблицы
        flightsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                showFlightDetails(newSelection);
            }
        });
    }

// рейсы из БД
    private void loadFlights() {
        try {
            List<Flight> flights = flightRepository.findAll();
            ObservableList<Flight> flightList = FXCollections.observableArrayList(flights);
            flightsTable.setItems(flightList);

            // Обновление статистики
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
            log("❌ Ошибка загрузки рейсов: " + e.getMessage());
            e.printStackTrace();
        }
    }

//Детали рейсов
    private void showFlightDetails(Flight flight) {
        selectedFlight = flight;

        // Заполняем информацию
        detailFlightNumber.setText(flight.getFlightNumber());
        detailAircraftType.setText(flight.getAircraftType().getDisplayName());
        detailStatus.setText(flight.getStatus().toString());

        double totalWeight = flight.getTotalCargoWeight() != null ? flight.getTotalCargoWeight() : 0;
        detailTotalWeight.setText(String.format("%.0f кг", totalWeight));

        // Загружаем отсеки
        compartmentsContainer.getChildren().clear();
        if (flight.getAircraft() != null) {
            for (Compartment compartment : flight.getAircraft().getCompartments()) {
                VBox compartmentBox = new VBox(5);
                compartmentBox.setStyle("-fx-padding: 10; -fx-background-color: #f8f9fa; -fx-background-radius: 5;");

                Label nameLabel = new Label(compartment.getName());
                nameLabel.setStyle("-fx-font-weight: bold;");

                Label weightLabel = new Label(String.format("Загрузка: %.0f / %.0f кг",
                        compartment.getCurrentWeight(), compartment.getMaxWeight()));

                ProgressBar progressBar = new ProgressBar();
                progressBar.setProgress(compartment.getCurrentWeight() / compartment.getMaxWeight());
                progressBar.setPrefWidth(200);

                compartmentBox.getChildren().addAll(nameLabel, weightLabel, progressBar);
                compartmentsContainer.getChildren().add(compartmentBox);
            }
        }

        // Загружаем доступные ресурсы в ComboBox
        List<Equipment> availableEquipment = resourceRepository.findAvailableEquipment();
        equipmentComboBox.setItems(FXCollections.observableArrayList(availableEquipment));

        List<Brigade> availableBrigades = resourceRepository.findAvailableBrigades();
        brigadeComboBox.setItems(FXCollections.observableArrayList(availableBrigades));

        // Показываем панель
        detailsPanel.setVisible(true);
        detailsPanel.setManaged(true);

        log("🔍 Открыты детали рейса " + flight.getFlightNumber());
    }

//Закрытие
    @FXML
    private void handleCloseDetails() {
        detailsPanel.setVisible(false);
        detailsPanel.setManaged(false);
        selectedFlight = null;
        flightsTable.getSelectionModel().clearSelection();
    }

//Автораспределение
    @FXML
    private void handleAutoAllocate() {
        if (selectedFlight == null) {
            log("⚠ Сначала выберите рейс в таблице!");
            return;
        }

        try {
            allocationService.autoAllocate(selectedFlight);
            log("✅ Авто-распределение выполнено для рейса " + selectedFlight.getFlightNumber());

            // Обновляем таблицу
            loadFlights();

            // Обновляем детали
            showFlightDetails(selectedFlight);
        } catch (Exception e) {
            log("❌ Ошибка авто-распределения: " + e.getMessage());
            e.printStackTrace();
        }
    }

//Добавить груз
    @FXML
    private void handleAddCargo() {
        if (selectedFlight == null) {
            log("⚠ Сначала выберите рейс в таблице!");
            return;
        }

        if (selectedFlight.getAircraft() == null || selectedFlight.getAircraft().getCompartments().isEmpty()) {
            log("⚠ У рейса нет воздушного судна или отсеков!");
            return;
        }

        // Тестовый груз для демонстрации
        Compartment firstCompartment = selectedFlight.getAircraft().getCompartments().get(0);
        Cargo testCargo = new Cargo(500.0, "Тестовый груз", firstCompartment, selectedFlight);

        boolean success = weightBalanceService.validateAndAddCargo(selectedFlight, testCargo);

        if (success) {
            log("✅ Груз 500 кг добавлен в отсек '" + firstCompartment.getName() + "'");
            showFlightDetails(selectedFlight); // Обновляем панель
        } else {
            log(" Не удалось добавить груз: превышен лимит веса!");
        }
    }

//Сохрнаить зименения
    @FXML
    private void handleSave() {
        if (selectedFlight == null) {
            log(" Нечего сохранять!");
            return;
        }

        try {
            flightRepository.update(selectedFlight);
            log("💾 Рейс " + selectedFlight.getFlightNumber() + " сохранён в базу данных.");
        } catch (Exception e) {
            log("❌ Ошибка сохранения: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleRefresh() {
        log("🔄 Обновление данных...");
        loadFlights();
    }

    private void log(String message) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        logArea.appendText("[" + timestamp + "] " + message + "\n");
    }
}