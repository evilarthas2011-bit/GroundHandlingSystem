package com.kuleshov.groundhandling.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "flights")
public class Flight {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "flight_number", nullable = false, unique = true)
    private String flightNumber;

    @Column(name = "aircraft_type", nullable = false)
    private String aircraftType;

    @Column(name = "scheduled_time", nullable = false)
    private LocalDateTime scheduledTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private FlightStatus status;

    @Column(name = "total_cargo_weight")
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
