package com.kuleshov.groundhandling.service;

import com.kuleshov.groundhandling.domain.*;
import com.kuleshov.groundhandling.repository.ResourceRepository;
import java.util.List;

public class ResourceAllocationService {

    private final ResourceRepository resourceRepository;

    public ResourceAllocationService(ResourceRepository resourceRepository) {
        this.resourceRepository = resourceRepository;
    }

    public OperationResult allocateResources(Flight flight) {
        List<Equipment> availableEquipment = resourceRepository.findAvailableEquipment();
        List<Brigade> availableBrigades = resourceRepository.findAvailableBrigades();

        if (availableEquipment.isEmpty()) {
            return OperationResult.error("Нет свободной техники для назначения на рейс " + flight.getFlightNumber());
        }
        if (availableBrigades.isEmpty()) {
            return OperationResult.error("Нет свободных бригад для назначения на рейс " + flight.getFlightNumber());
        }

        Equipment equipment = availableEquipment.get(0);
        Brigade brigade = availableBrigades.get(0);

        equipment.setAssignedFlight(flight);
        resourceRepository.save(equipment);

        brigade.setAssignedFlight(flight);
        resourceRepository.save(brigade);

        return OperationResult.success("Ресурсы успешно распределены: " + equipment.getModelName() + " и бригада " + brigade.getSpecialization());
    }
}