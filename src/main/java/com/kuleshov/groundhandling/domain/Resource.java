package com.kuleshov.groundhandling.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "resources")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "resource_type", discriminatorType = DiscriminatorType.STRING)
public abstract class Resource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_flight_id")
    private Flight assignedFlight;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Flight getAssignedFlight() { return assignedFlight; }
    public void setAssignedFlight(Flight assignedFlight) { this.assignedFlight = assignedFlight; }
}