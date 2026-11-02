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
    public OperationResult updateFlightStatus(Long flightId, FlightStatus status) {
        Flight flight = flightRepository.findByIdWithDetails(flightId);
        if (flight == null) return OperationResult.error("Рейс не найден");
        flight.setStatus(status);
        flightRepository.save(flight);
        return OperationResult.success("Статус обновлён");
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
            flightRepository.save(flight);
            return OperationResult.success("Добавлено " + weight + " кг в отсек '" + targetCompartment.getName() + "'");
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
            flightRepository.save(flight);
            return OperationResult.success("Груз из отсека '" + targetCompartment.getName() + "' разгружен");
        }
        return OperationResult.error("Отсек пуст!");
    }

    @Override
    public OperationResult assignResources(Long flightId, Long equipmentId, Long brigadeId) {
        Flight flight = flightRepository.findByIdWithDetails(flightId);
        if (flight == null) return OperationResult.error("Рейс не найден");

        List<Resource> old = resourceRepository.findByAssignedFlight(flight);
        for (Resource r : old) {
            r.setAssignedFlight(null);
            resourceRepository.save(r);
        }

        if (equipmentId != null) {
            Resource eq = resourceRepository.findById(equipmentId);
            if (eq != null) {
                eq.setAssignedFlight(flight);
                resourceRepository.save(eq);
            }
        }
        if (brigadeId != null) {
            Resource br = resourceRepository.findById(brigadeId);
            if (br != null) {
                br.setAssignedFlight(flight);
                resourceRepository.save(br);
            }
        }
        return OperationResult.success("Ресурсы успешно обновлены");
    }

    @Override
    public OperationResult deleteFlight(Long flightId) {
        Flight flight = flightRepository.findByIdWithDetails(flightId);
        if (flight == null) return OperationResult.error("Рейс не найден");

        List<Resource> assigned = resourceRepository.findByAssignedFlight(flight);
        for (Resource r : assigned) {
            r.setAssignedFlight(null);
            resourceRepository.save(r);
        }

        flightRepository.delete(flight);
        return OperationResult.success("Рейс удален");
    }

    @Override
    public List<Resource> getAssignedResources(Flight flight) {
        if (flight == null) return List.of();
        return resourceRepository.findByAssignedFlight(flight);
    }
}