package com.kuleshov.groundhandling.controller;

import com.kuleshov.groundhandling.domain.AircraftType;
import com.kuleshov.groundhandling.repository.AircraftRepositoryImpl;
import com.kuleshov.groundhandling.repository.FlightRepositoryImpl;
import com.kuleshov.groundhandling.repository.ResourceRepositoryImpl;
import com.kuleshov.groundhandling.service.FlightManagementService;
import com.kuleshov.groundhandling.service.FlightManagementServiceImpl;
import com.kuleshov.groundhandling.service.OperationResult;
import com.kuleshov.groundhandling.service.WeightBalanceService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.time.LocalDateTime;
import java.time.LocalTime;

public class FlightFormController {

    @FXML private TextField flightNumberField;
    @FXML private DatePicker departureDatePicker;
    @FXML private ComboBox<Integer> hourComboBox;
    @FXML private ComboBox<Integer> minuteComboBox;
    @FXML private ComboBox<AircraftType> aircraftTypeComboBox;
    @FXML private TextField tailNumberField;
    @FXML private Button createButton;
    @FXML private Label statusLabel;

    private FlightManagementService flightManagementService;

    /** Колбэк: дашборд подписывается на событие «рейс создан», чтобы обновить таблицу. */
    private Runnable onFlightCreated;

    public void setOnFlightCreated(Runnable onFlightCreated) {
        this.onFlightCreated = onFlightCreated;
    }

    @FXML
    public void initialize() {
        flightManagementService = new FlightManagementServiceImpl(
                new FlightRepositoryImpl(),
                new ResourceRepositoryImpl(),
                new AircraftRepositoryImpl(),
                new WeightBalanceService());

        aircraftTypeComboBox.setItems(FXCollections.observableArrayList(AircraftType.values()));
        aircraftTypeComboBox.getSelectionModel().selectFirst();

        hourComboBox.setItems(FXCollections.observableArrayList(
                0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11,
                12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23));
        hourComboBox.getSelectionModel().select(Integer.valueOf(LocalTime.now().getHour()));

        minuteComboBox.setItems(FXCollections.observableArrayList(0, 15, 30, 45));
        minuteComboBox.getSelectionModel().select(Integer.valueOf(0));
    }

    @FXML
    private void handleCreate() {
        try {
            AircraftType selectedType = aircraftTypeComboBox.getValue();
            if (selectedType == null) {
                showStatus("Выберите тип воздушного судна", false);
                return;
            }

            LocalDateTime departureDateTime = buildDepartureDateTime();
            if (departureDateTime == null) {
                return;
            }

            OperationResult result = flightManagementService.createFlight(
                    flightNumberField.getText(), departureDateTime,
                    selectedType, tailNumberField.getText());

            showStatus(result.getMessage(), result.isSuccess());
            if (result.isSuccess()) {
                clearForm();
                if (onFlightCreated != null) {
                    onFlightCreated.run();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            showStatus("Ошибка: " + e.getMessage(), false);
        }
    }


    @FXML
    private void handleCancel() {
        flightNumberField.getScene().getWindow().hide();
    }

    private LocalDateTime buildDepartureDateTime() {
        if (departureDatePicker.getValue() == null) {
            showStatus("Выберите дату вылета", false);
            return null;
        }
        Integer hour = hourComboBox.getValue();
        Integer minute = minuteComboBox.getValue();
        if (hour == null || minute == null) {
            showStatus("Выберите время вылета", false);
            return null;
        }
        return departureDatePicker.getValue().atTime(hour, minute);
    }

    private void clearForm() {
        flightNumberField.clear();
        tailNumberField.clear();
        departureDatePicker.setValue(null);
    }

    private void showStatus(String message, boolean success) {
        statusLabel.setText(message);
        statusLabel.setStyle(success
                ? "-fx-text-fill: #2e7d32;"
                : "-fx-text-fill: #c62828;");
    }
}
