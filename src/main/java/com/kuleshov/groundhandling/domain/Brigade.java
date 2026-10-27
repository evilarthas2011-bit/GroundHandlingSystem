package com.kuleshov.groundhandling.domain;

import jakarta.persistence.*;

@Entity
@DiscriminatorValue("BRIGADE")
public class Brigade extends Resource {

    @Column(name = "size")
    private Integer size;

    @Column(name = "specialization")
    private String specialization;

    public Brigade() {}

    public Brigade(Integer size, String specialization) {
        this.size = size;
        this.specialization = specialization;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    public String getSpecialization() {
        return specialization;
    }

    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }

    @Override
    public String toString() {
        return specialization;
    }
}