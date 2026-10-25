package com.kuleshov.groundhandling.domain;

import jakarta.persistence.*;

@Entity
@DiscriminatorValue("EQUIPMENT")
public class Equipment extends Resource {

    @Column(name = "model_name")
    private String modelName;

    @Column(name = "load_capacity")
    private Double loadCapacity;

    public Equipment() {
    }

    public Equipment(String modelName, Double loadCapacity) {
        this.modelName = modelName;
        this.loadCapacity = loadCapacity;
    }


    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public Double getLoadCapacity() {
        return loadCapacity;
    }

    public void setLoadCapacity(Double loadCapacity) {
        this.loadCapacity = loadCapacity;
    }

    @Override
    public String toString() {
        return "Equipment{" +
                "modelName='" + modelName + '\'' +
                ", loadCapacity=" + loadCapacity +
                ", isAvailable=" + getIsAvailable() +
                '}';
    }
}