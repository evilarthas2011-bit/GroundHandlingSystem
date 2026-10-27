package com.kuleshov.groundhandling.controller;

import com.kuleshov.groundhandling.domain.*;
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
    @FXML private TextField tailNumberField;
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

        aircraftTypeComboBox.setItems(FXCollections.observableArrayList(AircraftType.values()));
        aircraftTypeComboBox.getSelectionModel().selectFirst();

        statusComboBox.setItems(FXCollections.observableArrayList(FlightStatus.values()));
        statusComboBox.getSelectionModel().select(FlightStatus.SCHEDULED);

        datePicker.setValue(LocalDate.now());
    }

    public void setOnFlightCreated(Runnable onFlightCreated) { this.onFlightCreated = onFlightCreated; }

    @FXML
    private void handleCreate() {
        errorLabel.setText("");

        String flightNumber = flightNumberField.getText().trim();
        String tailNumber = tailNumberField.getText().trim();

        if (flightNumber.isEmpty() || tailNumber.isEmpty()) {
            errorLabel.setText("Введите номер рейса и бортовой номер ВС");
            return;
        }

        AircraftType aircraftType = aircraftTypeComboBox.getValue();
        FlightStatus status = statusComboBox.getValue();
        LocalDate selectedDate = datePicker.getValue();

        try {
            LocalDateTime flightDateTime = selectedDate.atStartOfDay();

            Aircraft newAircraft = new Aircraft(aircraftType, tailNumber);
            if (aircraftType == AircraftType.BOEING_737) {
                newAircraft.addCompartment(new Compartment("Носовой отсек", 3000.0));
                newAircraft.addCompartment(new Compartment("Хвостовой отсек", 4000.0));
            } else if (aircraftType == AircraftType.AIRBUS_A320) {
                newAircraft.addCompartment(new Compartment("Носовой отсек", 2500.0));
                newAircraft.addCompartment(new Compartment("Хвостовой отсек", 3500.0));
            } else if (aircraftType == AircraftType.BOEING_777) {
                newAircraft.addCompartment(new Compartment("Носовой отсек", 10000.0));
                newAircraft.addCompartment(new Compartment("Хвостовой отсек", 15000.0));
            } else {
                newAircraft.addCompartment(new Compartment("Носовой отсек", 12000.0));
                newAircraft.addCompartment(new Compartment("Хвостовой отсек", 18000.0));
            }

            aircraftRepository.save(newAircraft);

            Flight flight = new Flight(flightNumber, aircraftType, flightDateTime);
            flight.setAircraft(newAircraft);
            flight.setStatus(status);
            flight.setTotalCargoWeight(0.0);

            flightRepository.save(flight);

            if (onFlightCreated != null) onFlightCreated.run();

            Stage stage = (Stage) createButton.getScene().getWindow();
            stage.close();

        } catch (Exception e) {
            errorLabel.setText("Ошибка: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleCancel() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }
}