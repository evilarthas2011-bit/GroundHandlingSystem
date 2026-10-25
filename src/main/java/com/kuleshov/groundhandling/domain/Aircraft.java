package com.kuleshov.groundhandling.domain;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "aircraft")
public class Aircraft {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, unique = true)
    private AircraftType type;

    @Column(name = "max_payload", nullable = false)
    private Double maxPayload;

    @OneToMany(mappedBy = "aircraft", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Compartment> compartments = new ArrayList<>();

    public Aircraft() {
    }

    public Aircraft(AircraftType type) {
        this.type = type;
        this.maxPayload = type.getDefaultMaxPayload();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public AircraftType getType() {
        return type;
    }

    public void setType(AircraftType type) {
        this.type = type;
    }

    public Double getMaxPayload() {
        return maxPayload;
    }

    public void setMaxPayload(Double maxPayload) {
        this.maxPayload = maxPayload;
    }

    public List<Compartment> getCompartments() {
        return compartments;
    }

    public void setCompartments(List<Compartment> compartments) {
        this.compartments = compartments;
    }

    public void addCompartment(Compartment compartment) {
        compartments.add(compartment);
        compartment.setAircraft(this);
    }

    @Override
    public String toString() {
        return "Aircraft{" +
                "type=" + type.getDisplayName() +
                ", maxPayload=" + maxPayload +
                '}';
    }
}