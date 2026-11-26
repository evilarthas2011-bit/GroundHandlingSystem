package com.kuleshov.groundhandling.controller;

import com.kuleshov.groundhandling.domain.Aircraft;
import com.kuleshov.groundhandling.domain.AircraftType;
import com.kuleshov.groundhandling.repository.AircraftRepositoryImpl;
import com.kuleshov.groundhandling.service.FleetManagementService;
import com.kuleshov.groundhandling.service.FleetManagementServiceImpl;
import com.kuleshov.groundhandling.service.OperationResult;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

public class FleetViewController {

    @FXML private TableView<Aircraft> fleetTable;
    @FXML private TableColumn<Aircraft, String> tailColumn;
    @FXML private TableColumn<Aircraft, String> typeColumn;
    @FXML private TableColumn<Aircraft, String> payloadColumn;
    @FXML private TableColumn<Aircraft, String> fwdColumn;
    @FXML private TableColumn<Aircraft, String> aftColumn;
    @FXML private ComboBox<AircraftType> fleetTypeComboBox;
    @FXML private TextField newTailField;
    @FXML private Label fleetStatusLabel;

    private final FleetManagementService fleetService =
            new FleetManagementServiceImpl(new AircraftRepositoryImpl());

    /** Колбэк: дашборд обновляет данные, когда парк изменился. */
    private Runnable onFleetChanged;

    public void setOnFleetChanged(Runnable onFleetChanged) {
        this.onFleetChanged = onFleetChanged;
    }

    @FXML
    public void initialize() {
        setupTableColumns();

        fleetTypeComboBox.setItems(FXCollections.observableArrayList(AircraftType.values()));
        fleetTypeComboBox.getSelectionModel().selectFirst();

        loadFleet();
    }

    private void setupTableColumns() {
        tailColumn.setCellValueFactory(new PropertyValueFactory<>("tailNumber"));
        typeColumn.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(
                        data.getValue().getAircraftType().getDisplayName()));
        payloadColumn.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(
                        String.format("%.0f кг", data.getValue().getMaxPayload())));
        fwdColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
                String.format("%.0f", data.getValue().getCompartments().get(0).getMaxWeight())));
        aftColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
                String.format("%.0f", data.getValue().getCompartments().get(1).getMaxWeight())));
    }

    private void loadFleet() {
        fleetTable.setItems(FXCollections.observableArrayList(fleetService.getFleet()));
    }

    @FXML
    private void handleAddAircraft() {
        OperationResult result = fleetService.registerAircraft(
                fleetTypeComboBox.getValue(), newTailField.getText());
        fleetStatusLabel.setText(result.getMessage());
        fleetStatusLabel.setStyle(result.isSuccess()
                ? "-fx-text-fill: #2e7d32;" : "-fx-text-fill: #c62828;");
        if (result.isSuccess()) {
            newTailField.clear();
            loadFleet();
            if (onFleetChanged != null) {
                onFleetChanged.run();
            }
        }
    }

    @FXML
    private void handleClose() {
        fleetTable.getScene().getWindow().hide();
    }
}
