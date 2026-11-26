package com.kuleshov.groundhandling.domain;

import java.util.List;

public enum AircraftType {

    BOEING_737("Boeing 737", List.of(
            new CompartmentSpec("Передний отсек", 3400.0),
            new CompartmentSpec("Задний отсек", 2800.0)
    )),

    AIRBUS_A320("Airbus A320", List.of(
            new CompartmentSpec("Передний отсек", 3200.0),
            new CompartmentSpec("Задний отсек", 2600.0)
    ));

    private final String displayName;
    private final List<CompartmentSpec> compartments;

    AircraftType(String displayName, List<CompartmentSpec> compartments) {
        this.displayName = displayName;
        this.compartments = compartments;
    }

    public String getDisplayName() {
        return displayName;
    }

    public List<CompartmentSpec> getCompartments() {
        return compartments;
    }
}
