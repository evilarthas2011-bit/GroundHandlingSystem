package com.kuleshov.groundhandling.domain;

import jakarta.persistence.*;

/**
 * Абстрактный класс, представляющий ресурс перрона.
 * Является родителем для Equipment (техника) и Brigade (бригады).
 * Используется наследование типа SINGLE_TABLE - все в одной таблице с дискриминатором.
 */
@Entity
@Table(name = "resources")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "resource_type", discriminatorType = DiscriminatorType.STRING)
public abstract class Resource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "is_available", nullable = false)
    private Boolean isAvailable;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_flight_id")
    private Flight assignedFlight;

    public Resource() {
        this.isAvailable = true;
    }

    // Getters и Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Boolean getIsAvailable() {
        return isAvailable;
    }

    public void setIsAvailable(Boolean available) {
        isAvailable = available;
    }

    public Flight getAssignedFlight() {
        return assignedFlight;
    }

    public void setAssignedFlight(Flight assignedFlight) {
        this.assignedFlight = assignedFlight;
    }


    public void assignToFlight(Flight flight) {
        this.assignedFlight = flight;
        this.isAvailable = false;
    }


    public void release() {
        this.assignedFlight = null;
        this.isAvailable = true;
    }

    @Override
    public String toString() {
        return "Resource{" +
                "id=" + id +
                ", isAvailable=" + isAvailable +
                '}';
    }
}