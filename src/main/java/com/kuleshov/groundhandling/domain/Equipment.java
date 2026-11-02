package com.kuleshov.groundhandling.domain;

import jakarta.persistence.*;

@Entity
@DiscriminatorValue("EQUIPMENT")
public class Equipment extends Resource {

    @Column(name = "model_name")
    private String modelName;

    @Column(name = "capacity")
    private Double capacity;

    public String getModelName() { return modelName; }
    public void setModelName(String modelName) { this.modelName = modelName; }
    public Double getCapacity() { return capacity; }
    public void setCapacity(Double capacity) { this.capacity = capacity; }
}