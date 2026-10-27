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
        List<Equipment> availableEquipment = resourceRepository.findAvailableEquipment();
        List<Brigade> availableBrigades = resourceRepository.findAvailableBrigades();

        if (!availableEquipment.isEmpty()) {
            Equipment equipment = availableEquipment.get(0);
            equipment.setAssignedFlight(flight);
            resourceRepository.update(equipment);
        }

        if (!availableBrigades.isEmpty()) {
            Brigade brigade = availableBrigades.get(0);
            brigade.setAssignedFlight(flight);
            resourceRepository.update(brigade);
        }
    }
}