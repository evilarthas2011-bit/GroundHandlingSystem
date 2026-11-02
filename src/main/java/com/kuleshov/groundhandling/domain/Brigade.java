package com.kuleshov.groundhandling.domain;

import jakarta.persistence.*;

@Entity
@DiscriminatorValue("BRIGADE")
public class Brigade extends Resource {

    @Column(name = "specialization")
    private String specialization;

    @Column(name = "size")
    private Integer size;

    public String getSpecialization() { return specialization; }
    public void setSpecialization(String specialization) { this.specialization = specialization; }
    public Integer getSize() { return size; }
    public void setSize(Integer size) { this.size = size; }
}