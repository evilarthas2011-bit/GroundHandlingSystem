package com.kuleshov.groundhandling.domain;

import jakarta.persistence.*;

@Entity
@DiscriminatorValue("EQUIPMENT")
public class Equipment extends Resource {

    @Column(name = "model_name")
    private String modelName;

    @Column(name = "capacity")
    private Double capacity = 0.0;

    public String getModelName() { return modelName; }

    public void setModelName(String modelName) {
        if (modelName == null || modelName.isBlank()) {
            throw new IllegalArgumentException("Модель техники не может быть пустой");
        }
        this.modelName = modelName.trim();
    }

    public Double getCapacity() { return capacity; }

    public void setCapacity(Double capacity) {
        if (capacity == null || capacity < 0) {
            throw new IllegalArgumentException("Ёмкость техники не может быть отрицательной");
        }
        this.capacity = capacity;
    }
}
