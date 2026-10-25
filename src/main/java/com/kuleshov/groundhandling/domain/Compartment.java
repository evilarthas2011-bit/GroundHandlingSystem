package com.kuleshov.groundhandling.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "compartments")
public class Compartment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "max_weight", nullable = false)
    private Double maxWeight;

    @Column(name = "current_weight", nullable = false)
    private Double currentWeight;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "aircraft_id", nullable = false)
    private Aircraft aircraft;

    public Compartment() {
        this.currentWeight = 0.0;
    }

    public Compartment(String name, Double maxWeight) {
        this.name = name;
        this.maxWeight = maxWeight;
        this.currentWeight = 0.0;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getMaxWeight() {
        return maxWeight;
    }

    public void setMaxWeight(Double maxWeight) {
        this.maxWeight = maxWeight;
    }

    public Double getCurrentWeight() {
        return currentWeight;
    }

    public void setCurrentWeight(Double currentWeight) {
        this.currentWeight = currentWeight;
    }

    public Aircraft getAircraft() {
        return aircraft;
    }

    public void setAircraft(Aircraft aircraft) {
        this.aircraft = aircraft;
    }


    public boolean addWeight(Double weight) {
        if (this.currentWeight + weight <= this.maxWeight) {
            this.currentWeight += weight;
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        return "Compartment{" +
                "name='" + name + '\'' +
                ", currentWeight=" + currentWeight +
                ", maxWeight=" + maxWeight +
                '}';
    }
}
