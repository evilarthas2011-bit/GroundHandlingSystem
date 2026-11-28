package com.kuleshov.groundhandling.domain;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "flight")
public class Flight {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "flight_number", nullable = false, length = 32)
    private String flightNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 16)
    private FlightStatus status = FlightStatus.SCHEDULED;

    @Column(name = "departure_time", nullable = false)
    private LocalDateTime departureDateTime;

    @Column(name = "passenger_weight")
    private Double passengerWeight = 0.0;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "aircraft_id")
    private Aircraft aircraft;

    @OneToMany(mappedBy = "flight", cascade = CascadeType.ALL, orphanRemoval = true,
            fetch = FetchType.EAGER)
    @OrderBy("id")
    private List<Cargo> cargoList = new ArrayList<>();

    public Flight() {
    }

    public Flight(String flightNumber, LocalDateTime departureDateTime, Aircraft aircraft) {
        setFlightNumber(flightNumber);
        setDepartureDateTime(departureDateTime);
        setAircraft(aircraft);
        this.status = FlightStatus.SCHEDULED;
        this.passengerWeight = 0.0;
    }

    public double getTotalCargoWeight() {
        return cargoList.stream().mapToDouble(Cargo::getWeight).sum();
    }

    public double getTotalWeight() {
        return getTotalCargoWeight() + (passengerWeight != null ? passengerWeight : 0.0);
    }

    public void addCargo(Cargo cargo) {
        cargo.setFlight(this);
        cargoList.add(cargo);
    }

    public void removeCargo(Cargo cargo) {
        cargoList.remove(cargo);
        cargo.setFlight(null);
    }

    public Long getId() { return id; }

    public String getFlightNumber() { return flightNumber; }

    public void setFlightNumber(String flightNumber) {
        if (flightNumber == null || flightNumber.isBlank()) {
            throw new IllegalArgumentException("Номер рейса не может быть пустым");
        }
        this.flightNumber = flightNumber.trim().toUpperCase();
    }

    public LocalDateTime getDepartureDateTime() { return departureDateTime; }

    public void setDepartureDateTime(LocalDateTime departureDateTime) {
        if (departureDateTime == null) {
            throw new IllegalArgumentException("Дата вылета не может быть пустой");
        }
        this.departureDateTime = departureDateTime;
    }

    public Aircraft getAircraft() { return aircraft; }

    public void setAircraft(Aircraft aircraft) {
        if (aircraft == null) {
            throw new IllegalArgumentException("Рейс должен быть привязан к борту");
        }
        this.aircraft = aircraft;
    }

    public FlightStatus getStatus() { return status; }

    public void setStatus(FlightStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("Статус не может быть пустым");
        }
        this.status = status;
    }

    public Double getPassengerWeight() { return passengerWeight; }

    public boolean isClosedForOperations() {
        return status == FlightStatus.DEPARTED ;
    }

    public void setPassengerWeight(Double passengerWeight) {
        if (passengerWeight != null && passengerWeight < 0) {
            throw new IllegalArgumentException("Вес пассажиров не может быть отрицательным");
        }
        this.passengerWeight = passengerWeight;
    }

    public List<Cargo> getCargoList() { return cargoList; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Flight)) return false;
        Flight other = (Flight) o;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
