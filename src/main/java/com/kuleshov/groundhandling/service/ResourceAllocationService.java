package com.kuleshov.groundhandling.service;

import com.kuleshov.groundhandling.HibernateUtil;
import com.kuleshov.groundhandling.domain.Brigade;
import com.kuleshov.groundhandling.domain.Equipment;
import com.kuleshov.groundhandling.domain.Flight;
import com.kuleshov.groundhandling.repository.ResourceRepository;

import java.util.List;

public class ResourceAllocationService {

    private final ResourceRepository resourceRepository;

    public ResourceAllocationService(ResourceRepository resourceRepository) {
        this.resourceRepository = resourceRepository;
    }

    public OperationResult allocateResources(Long flightId) {
        return HibernateUtil.inTransaction(session -> {
            Flight flight = session.find(Flight.class, flightId);
            if (flight == null) {
                return OperationResult.error("Рейс не найден");
            }

            List<Equipment> equipment = resourceRepository.findAvailableEquipment(session);
            if (equipment.isEmpty()) {
                return OperationResult.error("Нет свободной техники для рейса " + flight.getFlightNumber());
            }
            List<Brigade> brigades = resourceRepository.findAvailableBrigades(session);
            if (brigades.isEmpty()) {
                return OperationResult.error("Нет свободных бригад для рейса " + flight.getFlightNumber());
            }

            equipment.get(0).setAssignedFlight(flight);
            brigades.get(0).setAssignedFlight(flight);
            resourceRepository.save(session, equipment.get(0));
            resourceRepository.save(session, brigades.get(0));
            return OperationResult.success(
                    "Назначено: " + equipment.get(0).getName()
                            + " и " + brigades.get(0).getName()
                            + " для рейса " + flight.getFlightNumber());
        });
    }

    public OperationResult allocateResources(Flight flight) {
        return allocateResources(flight.getId());
    }
}
