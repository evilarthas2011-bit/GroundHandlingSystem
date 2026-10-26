package com.kuleshov.groundhandling.service;

import com.kuleshov.groundhandling.domain.*;
import com.kuleshov.groundhandling.repository.ResourceRepository;

import java.util.List;

public class ResourceAllocationService {

    private final ResourceRepository resourceRepository;

    public ResourceAllocationService(ResourceRepository resourceRepository) {
        this.resourceRepository = resourceRepository;
    }

    public void autoAllocate(Flight flight) {
        System.out.println("Начало автоматического распределения для рейса " + flight.getFlightNumber());

        List<Equipment> availableEquipment = resourceRepository.findAvailableEquipment();
        List<Brigade> availableBrigades = resourceRepository.findAvailableBrigades();

        if (!availableEquipment.isEmpty()) {
            Equipment equipment = availableEquipment.get(0);
            equipment.assignToFlight(flight);
            resourceRepository.update(equipment);
            System.out.println("Назначена техника: " + equipment.getModelName());
        } else {

            System.out.println("Не найдено свободной техники!");
        }
        if (!availableBrigades.isEmpty()) {
            Brigade brigade = availableBrigades.get(0);
            brigade.assignToFlight(flight);
            resourceRepository.update(brigade);
            System.out.println("Назначена бригада: " + brigade.getSpecialization());
        } else {
            System.out.println("Не найдено свободной бригады !");
        }
        System.out.println("Распределение завершено");
    }
}