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
    @Column(name = "type", nullable = false)
    private AircraftType type;

    @Column(name = "tail_number", unique = true, nullable = false)
    private String tailNumber;

    @OneToMany(mappedBy = "aircraft", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Compartment> compartments = new ArrayList<>();

    public Aircraft() {}

    public Aircraft(AircraftType type) {
        this.type = type;
        this.tailNumber = "AUTO-" + System.currentTimeMillis();
    }

    public Aircraft(AircraftType type, String tailNumber) {
        this.type = type;
        this.tailNumber = tailNumber;
    }

    public void addCompartment(Compartment compartment) {
        compartments.add(compartment);
        compartment.setAircraft(this);
    }

    public void removeCompartment(Compartment compartment) {
        compartments.remove(compartment);
        compartment.setAircraft(null);
    }

    public double getMaxPayload() {
        return type != null ? type.getMaxPayload() : 0.0;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public AircraftType getType() { return type; }
    public void setType(AircraftType type) { this.type = type; }
    public String getTailNumber() { return tailNumber; }
    public void setTailNumber(String tailNumber) { this.tailNumber = tailNumber; }
    public List<Compartment> getCompartments() { return compartments; }
    public void setCompartments(List<Compartment> compartments) { this.compartments = compartments; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Aircraft aircraft = (Aircraft) o;
        return id != null && id.equals(aircraft.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}