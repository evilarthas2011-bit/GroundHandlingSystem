package com.kuleshov.groundhandling.domain;

public enum AircraftType {
    BOEING_737("Boeing 737-800", 20000.0),
    AIRBUS_A320("Airbus A320", 18000.0),
    BOEING_777("Boeing 777-300", 50000.0),
    AIRBUS_A350("Airbus A350-900", 55000.0);

    private final String displayName;
    private final double maxPayload;

    AircraftType(String displayName, double maxPayload) {
        this.displayName = displayName;
        this.maxPayload = maxPayload;
    }

    public String getDisplayName() { return displayName; }
    public double getMaxPayload() { return maxPayload; }
}