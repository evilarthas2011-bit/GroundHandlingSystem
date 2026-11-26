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
    @Column(name = "aircraft_type", nullable = false, length = 32)
    private AircraftType aircraftType;

    @Column(name = "tail_number", nullable = false, unique = true, length = 32)
    private String tailNumber;

    @OneToMany(mappedBy = "aircraft", cascade = CascadeType.ALL, orphanRemoval = true,
            fetch = FetchType.EAGER)
    @OrderBy("id")
    private List<Compartment> compartments = new ArrayList<>();

    public Aircraft() {
    }

    public static Aircraft createFor(AircraftType type, String tailNumber) {
        Aircraft aircraft = new Aircraft();
        aircraft.setAircraftType(type);
        aircraft.setTailNumber(tailNumber);
        for (CompartmentSpec spec : type.getCompartments()) {
            aircraft.compartments.add(Compartment.create(spec.name(), spec.maxWeight(), aircraft));
        }
        return aircraft;
    }

    public Long getId() { return id; }

    public AircraftType getAircraftType() { return aircraftType; }

    public void setAircraftType(AircraftType aircraftType) {
        if (aircraftType == null) {
            throw new IllegalArgumentException("Тип воздушного судна не может быть пустым");
        }
        this.aircraftType = aircraftType;
    }

    public String getTailNumber() { return tailNumber; }
    public double getMaxPayload() {
        return compartments.stream().mapToDouble(Compartment::getMaxWeight).sum();
    }

    public void setTailNumber(String tailNumber) {
        if (tailNumber == null || tailNumber.isBlank()) {
            throw new IllegalArgumentException("Бортовой номер не может быть пустым");
        }
        this.tailNumber = tailNumber.trim().toUpperCase();
    }

    public List<Compartment> getCompartments() { return compartments; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Aircraft)) return false;
        Aircraft other = (Aircraft) o;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
