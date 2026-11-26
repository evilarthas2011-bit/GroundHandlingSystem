package com.kuleshov.groundhandling.domain;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

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

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "aircraft_id", nullable = false)
    private Aircraft aircraft;

    /** Обратная сторона связи. Заполняется репозиторием при загрузке и доменными методами. */
    @OneToMany(mappedBy = "compartment", fetch = FetchType.LAZY)
    private List<Cargo> cargoList = new ArrayList<>();

    protected Compartment() {
    }

    public Compartment(String name, Double maxWeight) {
        setName(name);
        setMaxWeight(maxWeight);
    }
    public static Compartment create(String name, double maxWeight, Aircraft aircraft) {
        Compartment compartment = new Compartment();
        compartment.setName(name);
        compartment.setMaxWeight(maxWeight);
        compartment.setAircraft(aircraft);
        return compartment;
    }

    public double getCurrentWeight() {
        return cargoList.stream().mapToDouble(Cargo::getWeight).sum();
    }

    public double getLoadRatio() {
        return maxWeight == null || maxWeight == 0 ? 0 : getCurrentWeight() / maxWeight;
    }

    public Long getId() { return id; }
    public String getName() { return name; }

    public void setName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Имя отсека не может быть пустым");
        }
        this.name = name.trim();
    }

    public Double getMaxWeight() { return maxWeight; }

    public void setMaxWeight(Double maxWeight) {
        if (maxWeight == null || maxWeight <= 0) {
            throw new IllegalArgumentException("Лимит отсека должен быть положительным числом");
        }
        this.maxWeight = maxWeight;
    }

    public Aircraft getAircraft() { return aircraft; }
    public void setAircraft(Aircraft aircraft) { this.aircraft = aircraft; }

    public List<Cargo> getCargoList() { return cargoList; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Compartment)) return false;
        Compartment other = (Compartment) o;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
