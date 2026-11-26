package com.kuleshov.groundhandling.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "cargo")
public class Cargo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Double weight;

    @Column
    private String description;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "flight_id", nullable = false)
    private Flight flight;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "compartment_id", nullable = false)
    private Compartment compartment;

    @Column(name = "added_at", nullable = false)
    private LocalDateTime addedAt = LocalDateTime.now();

    public Cargo() {
    }

    public Cargo(Double weight, String description, Compartment compartment, Flight flight) {
        setWeight(weight);
        this.description = description;
        this.compartment = compartment;
        this.flight = flight;
        this.addedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }

    public Double getWeight() { return weight; }

    public void setWeight(Double weight) {
        if (weight == null || weight <= 0) {
            throw new IllegalArgumentException("Вес груза должен быть положительным числом");
        }
        this.weight = weight;
    }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Flight getFlight() { return flight; }
    public void setFlight(Flight flight) { this.flight = flight; }

    public Compartment getCompartment() { return compartment; }
    public void setCompartment(Compartment compartment) { this.compartment = compartment; }

    public LocalDateTime getAddedAt() { return addedAt; }
    public void setAddedAt(LocalDateTime addedAt) { this.addedAt = addedAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Cargo)) return false;
        Cargo other = (Cargo) o;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
