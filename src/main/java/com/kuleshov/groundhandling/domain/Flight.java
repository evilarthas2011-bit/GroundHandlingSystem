package com.kuleshov.groundhandling.domain;

import java.time.LocalDateTime;

public class Flight {
    private Long id;
    private String flightNumber;
    private String aircraftType; //МБ тоже статусы
    private LocalDateTime scheduledTime;

    private FlightStatus status;

    private Double totalCargoWeight;

    public Flight() { //Для Hibernate обязательны!
        this.status = FlightStatus.SCHEDULED;
        this.totalCargoWeight = 0.0;
    }

    public Flight(String flightNumber, String aircraftType, LocalDateTime scheduledTime) {
        this.flightNumber = flightNumber;
        this.aircraftType = aircraftType;
        this.scheduledTime = scheduledTime;
        this.status = FlightStatus.SCHEDULED;
        this.totalCargoWeight = 0.0;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFlightNumber() {
        return flightNumber;
    }

    public void setFlightNumber(String flightNumber) {
        this.flightNumber = flightNumber;
    }

    public String getAircraftType() {
        return aircraftType;
    }

    public void setAircraftType(String aircraftType) {
        this.aircraftType = aircraftType;
    }

    public LocalDateTime getScheduledTime() {
        return scheduledTime;
    }

    public void setScheduledTime(LocalDateTime scheduledTime) {
        this.scheduledTime = scheduledTime;
    }

    public FlightStatus getStatus() {
        return status;
    }

    public void setStatus(FlightStatus status) {
        this.status = status;
    }

    public Double getTotalCargoWeight() {
        return totalCargoWeight;
    }

    public void setTotalCargoWeight(Double totalCargoWeight) {
        this.totalCargoWeight = totalCargoWeight;
    }

    @Override
    public String toString() {
        return "Flight{" +
                "flightNumber='" + flightNumber + '\'' +
                ", aircraftType='" + aircraftType + '\'' +
                ", scheduledTime=" + scheduledTime +
                ", status='" + status + '\'' +
                '}';
    }
}
