package com.kuleshov.groundhandling.service;

import com.kuleshov.groundhandling.domain.*;
import com.kuleshov.groundhandling.repository.AircraftRepository;
import com.kuleshov.groundhandling.repository.FlightRepository;
import com.kuleshov.groundhandling.repository.ResourceRepository;

import java.util.List;

public class FlightManagementServiceImpl implements FlightManagementService {

    private final FlightRepository flightRepository;
    private final ResourceRepository resourceRepository;
    private final AircraftRepository aircraftRepository;
    private final WeightBalanceService weightBalanceService;

    public FlightManagementServiceImpl(FlightRepository flightRepo, ResourceRepository resRepo,
                                       AircraftRepository aircraftRepo, WeightBalanceService weightService) {
        this.flightRepository = flightRepo;
        this.resourceRepository = resRepo;
        this.aircraftRepository = aircraftRepo;
        this.weightBalanceService = weightService;
    }

    @Override
    public OperationResult addCargoToFlight(Long flightId, Long compartmentId, double weight) {
        Flight flight = flightRepository.findByIdWithDetails(flightId);
        if (flight == null) return OperationResult.error("Рейс не найден");

        Compartment targetCompartment = flight.getAircraft().getCompartments().stream()
                .filter(c -> c.getId().equals(compartmentId))
                .findFirst().orElse(null);

        if (targetCompartment == null) return OperationResult.error("Отсек не найден");

        Cargo cargo = new Cargo(weight, "Груз", targetCompartment, flight);

        if (weightBalanceService.validateAndAddCargo(flight, cargo)) {
            try {
                aircraftRepository.update(flight.getAircraft());
                flightRepository.update(flight);
                return OperationResult.success("Добавлено " + weight + " кг в отсек '" + targetCompartment.getName() + "'");
            } catch (Exception e) {
                return OperationResult.error("Ошибка сохранения в БД: " + e.getMessage());
            }
        }
        return OperationResult.error("Превышен лимит веса отсека или самолета!");
    }

    @Override
    public OperationResult unloadLastCargoFromFlight(Long flightId, Long compartmentId) {
        Flight flight = flightRepository.findByIdWithDetails(flightId);
        if (flight == null) return OperationResult.error("Рейс не найден");

        Compartment targetCompartment = flight.getAircraft().getCompartments().stream()
                .filter(c -> c.getId().equals(compartmentId))
                .findFirst().orElse(null);

        if (targetCompartment == null) return OperationResult.error("Отсек не найден");

        if (weightBalanceService.unloadLastCargo(flight, targetCompartment)) {
            try {
                aircraftRepository.update(flight.getAircraft());
                flightRepository.update(flight);
                return OperationResult.success("Груз из отсека '" + targetCompartment.getName() + "' разгружен");
            } catch (Exception e) {
                return OperationResult.error("Ошибка сохранения в БД: " + e.getMessage());
            }
        }
        return OperationResult.error("Отсек пуст!");
    }

    @Override
    public OperationResult assignResources(Long flightId, Long equipmentId, Long brigadeId) {
        Flight flight = flightRepository.findByIdWithDetails(flightId);
        if (flight == null) return OperationResult.error("Рейс не найден");

        try {
            List<Resource> old = resourceRepository.findByAssignedFlight(flight);
            for (Resource r : old) {
                r.setAssignedFlight(null);
                resourceRepository.update(r);
            }

            if (equipmentId != null) {
                Equipment eq = (Equipment) resourceRepository.findById(equipmentId);
                if (eq != null) {
                    eq.setAssignedFlight(flight);
                    resourceRepository.update(eq);
                }
            }
            if (brigadeId != null) {
                Brigade br = (Brigade) resourceRepository.findById(brigadeId);
                if (br != null) {
                    br.setAssignedFlight(flight);
                    resourceRepository.update(br);
                }
            }

            flightRepository.update(flight);
            return OperationResult.success("Ресурсы успешно обновлены");
        } catch (Exception e) {
            return OperationResult.error("Ошибка назначения ресурсов: " + e.getMessage());
        }
    }

    @Override
    public OperationResult deleteFlight(Long flightId) {
        Flight flight = flightRepository.findByIdWithDetails(flightId);
        if (flight == null) return OperationResult.error("Рейс не найден");

        try {
            flightRepository.delete(flight);
            return OperationResult.success("Рейс удален");
        } catch (Exception e) {
            return OperationResult.error("Ошибка удаления рейса: " + e.getMessage());
        }
    }

    @Override
    public List<Resource> getAssignedResources(Flight flight) {
        return flightRepository.findAssignedResources(flight);
    }
}