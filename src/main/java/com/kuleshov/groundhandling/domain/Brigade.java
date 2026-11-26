package com.kuleshov.groundhandling.domain;

import jakarta.persistence.*;

@Entity
@DiscriminatorValue("BRIGADE")
public class Brigade extends Resource {

    @Column(name = "specialization")
    private String specialization;

    @Column(name = "size")
    private Integer size = 1;

    public String getSpecialization() { return specialization; }

    public void setSpecialization(String specialization) {
        if (specialization == null || specialization.isBlank()) {
            throw new IllegalArgumentException("Специализация не может быть пустой");
        }
        this.specialization = specialization.trim();
    }

    public Integer getSize() { return size; }

    public void setSize(Integer size) {
        if (size == null || size < 1) {
            throw new IllegalArgumentException("Размер бригады должен быть не менее одного человека");
        }
        this.size = size;
    }
}
