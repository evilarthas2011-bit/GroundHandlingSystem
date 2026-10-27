package com.kuleshov.groundhandling.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "flights")
public class Flight {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "flight_number")
    private String flightNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "aircraft_type")
    private AircraftType aircraftType;

    @Column(name = "scheduled_time")
    private LocalDateTime scheduledTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private FlightStatus status = FlightStatus.SCHEDULED;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "aircraft_id")
    private Aircraft aircraft;

    @Column(name = "total_cargo_weight")
    private Double totalCargoWeight = 0.0;

    @OneToMany(mappedBy = "flight", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Cargo> cargoList = new ArrayList<>();

    public Flight() {}

    public Flight(String flightNumber, AircraftType aircraftType, LocalDateTime scheduledTime) {
        this.flightNumber = flightNumber;
        this.aircraftType = aircraftType;
        this.scheduledTime = scheduledTime;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getFlightNumber() { return flightNumber; }
    public void setFlightNumber(String flightNumber) { this.flightNumber = flightNumber; }
    public AircraftType getAircraftType() { return aircraftType; }
    public void setAircraftType(AircraftType aircraftType) { this.aircraftType = aircraftType; }
    public LocalDateTime getScheduledTime() { return scheduledTime; }
    public void setScheduledTime(LocalDateTime scheduledTime) { this.scheduledTime = scheduledTime; }
    public FlightStatus getStatus() { return status; }
    public void setStatus(FlightStatus status) { this.status = status; }
    public Aircraft getAircraft() { return aircraft; }
    public void setAircraft(Aircraft aircraft) { this.aircraft = aircraft; }
    public Double getTotalCargoWeight() { return totalCargoWeight; }
    public void setTotalCargoWeight(Double totalCargoWeight) { this.totalCargoWeight = totalCargoWeight; }
    public List<Cargo> getCargoList() { return cargoList; }
    public void setCargoList(List<Cargo> cargoList) { this.cargoList = cargoList; }
}