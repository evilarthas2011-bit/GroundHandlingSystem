package com.kuleshov.groundhandling.domain;

import jakarta.persistence.*;

/**
 * Класс, представляющий грузовую бригаду.
 * Наследуется от Resource.
 */
@Entity
@DiscriminatorValue("BRIGADE")
public class Brigade extends Resource {

    @Column(name = "team_size")
    private Integer teamSize;

    @Column(name = "specialization")
    private String specialization;

    public Brigade() {
    }

    public Brigade(Integer teamSize, String specialization) {
        this.teamSize = teamSize;
        this.specialization = specialization;
    }


    public Integer getTeamSize() {
        return teamSize;
    }

    public void setTeamSize(Integer teamSize) {
        this.teamSize = teamSize;
    }

    public String getSpecialization() {
        return specialization;
    }

    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }

    @Override
    public String toString() {
        return "Brigade{" +
                "teamSize=" + teamSize +
                ", specialization='" + specialization + '\'' +
                ", isAvailable=" + getIsAvailable() +
                '}';
    }
}