package com.kuleshov.groundhandling.controller;

import com.kuleshov.groundhandling.HibernateUtil;
import com.kuleshov.groundhandling.domain.Aircraft;
import com.kuleshov.groundhandling.domain.AircraftType;
import com.kuleshov.groundhandling.domain.Compartment;
import com.kuleshov.groundhandling.domain.Flight;
import com.kuleshov.groundhandling.domain.FlightStatus;
import com.kuleshov.groundhandling.repository.AircraftRepository;
import com.kuleshov.groundhandling.repository.FlightRepository;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ResourceBundle;

public class FlightFormController implements Initializable {

    @FXML private TextField flightNumberField;
    @FXML private ComboBox<AircraftType> aircraftTypeComboBox;
    @FXML private ComboBox<FlightStatus> statusComboBox;
    @FXML private DatePicker datePicker;
    @FXML private Label errorLabel;
    @FXML private Button cancelButton;
    @FXML private Button createButton;

    private FlightRepository flightRepository;
    private AircraftRepository aircraftRepository;
    private Runnable onFlightCreated;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        flightRepository = new FlightRepository();
        aircraftRepository = new AircraftRepository();

        ObservableList<AircraftType> aircraftTypes = FXCollections.observableArrayList(AircraftType.values());
        aircraftTypeComboBox.setItems(aircraftTypes);
        aircraftTypeComboBox.getSelectionModel().selectFirst();

        ObservableList<FlightStatus> statuses = FXCollections.observableArrayList(FlightStatus.values());
        statusComboBox.setItems(statuses);
        statusComboBox.getSelectionModel().select(FlightStatus.SCHEDULED);

        datePicker.setValue(LocalDate.now());
    }

    public void setOnFlightCreated(Runnable onFlightCreated) {
        this.onFlightCreated = onFlightCreated;
    }

    @FXML
    private void handleCreate() {
        errorLabel.setText("");

        String flightNumber = flightNumberField.getText().trim();
        if (flightNumber.isEmpty()) {
            errorLabel.setText("Введите номер рейса");
            return;
        }

        AircraftType aircraftType = aircraftTypeComboBox.getValue();
        if (aircraftType == null) {
            errorLabel.setText("Выберите тип воздушного судна");
            return;
        }

        FlightStatus status = statusComboBox.getValue();
        if (status == null) {
            errorLabel.setText("Выберите статус рейса");
            return;
        }

        LocalDate selectedDate = datePicker.getValue();
        if (selectedDate == null) {
            errorLabel.setText("Выберите дату");
            return;
        }

        try {
            LocalDateTime flightDateTime = selectedDate.atStartOfDay();

            Aircraft existingAircraft = findOrCreateAircraft(aircraftType);

            Flight flight = new Flight(flightNumber, aircraftType, flightDateTime);
            flight.setAircraft(existingAircraft);
            flight.setStatus(status);

            flightRepository.save(flight);

            if (onFlightCreated != null) {
                onFlightCreated.run();
            }

            Stage stage = (Stage) createButton.getScene().getWindow();
            stage.close();

        } catch (Exception e) {
            errorLabel.setText("Ошибка: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private Aircraft findOrCreateAircraft(AircraftType aircraftType) {
        Aircraft existingAircraft = aircraftRepository.findByType(aircraftType)
                .stream()
                .findFirst()
                .orElse(null);

        if (existingAircraft != null) {
            return existingAircraft;
        }

        Aircraft newAircraft = new Aircraft(aircraftType);
        if (aircraftType == AircraftType.BOEING_737) {
            newAircraft.addCompartment(new Compartment("Носовой отсек", 3000.0));
            newAircraft.addCompartment(new Compartment("Хвостовой отсек", 4000.0));
        } else if (aircraftType == AircraftType.AIRBUS_A320) {
            newAircraft.addCompartment(new Compartment("Носовой отсек", 2500.0));
            newAircraft.addCompartment(new Compartment("Хвостовой отсек", 3500.0));
        } else if (aircraftType == AircraftType.BOEING_777) {
            newAircraft.addCompartment(new Compartment("Носовой отсек", 10000.0));
            newAircraft.addCompartment(new Compartment("Хвостовой отсек", 15000.0));
        } else if (aircraftType == AircraftType.AIRBUS_A350) {
            newAircraft.addCompartment(new Compartment("Носовой отсек", 12000.0));
            newAircraft.addCompartment(new Compartment("Хвостовой отсек", 18000.0));
        }

        return aircraftRepository.save(newAircraft);
    }

    @FXML
    private void handleCancel() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }
}