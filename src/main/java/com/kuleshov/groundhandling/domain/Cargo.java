package com.kuleshov.groundhandling.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "cargo")
public class Cargo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "weight", nullable = false)
    private Double weight;

    @Column(name = "description")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "compartment_id", nullable = false)
    private Compartment compartment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "flight_id", nullable = false)
    private Flight flight;

    @Column(name = "added_at")
    private LocalDateTime addedAt;

    public Cargo() {
        this.addedAt = LocalDateTime.now();
    }

    public Cargo(Double weight, String description, Compartment compartment, Flight flight) {
        this.weight = weight;
        this.description = description;
        this.compartment = compartment;
        this.flight = flight;
        this.addedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Double getWeight() { return weight; }
    public void setWeight(Double weight) { this.weight = weight; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Compartment getCompartment() { return compartment; }
    public void setCompartment(Compartment compartment) { this.compartment = compartment; }
    public Flight getFlight() { return flight; }
    public void setFlight(Flight flight) { this.flight = flight; }
    public LocalDateTime getAddedAt() { return addedAt; }
    public void setAddedAt(LocalDateTime addedAt) { this.addedAt = addedAt; }
}