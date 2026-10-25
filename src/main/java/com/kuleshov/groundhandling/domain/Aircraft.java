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

    @Column(name = "type", nullable = false, unique = true)
    private String type;

    @Column(name = "max_payload", nullable = false)
    private Double maxPayload;

    @OneToMany(mappedBy = "aircraft", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Compartment> compartments = new ArrayList<>();

    public Aircraft() {
    }

    public Aircraft(String type, Double maxPayload) {
        this.type = type;
        this.maxPayload = maxPayload;
    }


    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public Double getMaxPayload() { return maxPayload; }
    public void setMaxPayload(Double maxPayload) { this.maxPayload = maxPayload; }

    public List<Compartment> getCompartments() { return compartments; }
    public void setCompartments(List<Compartment> compartments) { this.compartments = compartments; }

    public void addCompartment(Compartment compartment) {
        compartments.add(compartment);
        compartment.setAircraft(this);
    }

    @Override
    public String toString() {
        return "Aircraft{" +
                "type='" + type + '\'' +
                ", maxPayload=" + maxPayload +
                '}';
    }
}