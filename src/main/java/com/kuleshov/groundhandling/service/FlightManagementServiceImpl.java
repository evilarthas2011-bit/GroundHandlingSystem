package com.kuleshov.groundhandling.service;

import com.kuleshov.groundhandling.HibernateUtil;
import com.kuleshov.groundhandling.domain.*;
import com.kuleshov.groundhandling.repository.AircraftRepository;
import com.kuleshov.groundhandling.repository.FlightRepository;
import com.kuleshov.groundhandling.repository.ResourceRepository;
import org.hibernate.Session;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class FlightManagementServiceImpl implements FlightManagementService {

    private final FlightRepository flightRepository;
    private final ResourceRepository resourceRepository;
    private final AircraftRepository aircraftRepository;
    private final WeightBalanceService weightBalanceService;
    private final FlightStatusTransitionValidator statusValidator = new FlightStatusTransitionValidator();

    public FlightManagementServiceImpl(FlightRepository flightRepository,
                                       ResourceRepository resourceRepository,
                                       AircraftRepository aircraftRepository,
                                       WeightBalanceService weightBalanceService) {
        this.flightRepository = flightRepository;
        this.resourceRepository = resourceRepository;
        this.aircraftRepository = aircraftRepository;
        this.weightBalanceService = weightBalanceService;
    }

    @Override
    public OperationResult updateFlightStatus(Long flightId, FlightStatus newStatus) {
        return HibernateUtil.inTransaction(session -> {
            Flight flight = flightRepository.findByIdWithDetails(session, flightId);
            if (flight == null) {
                return OperationResult.error("Рейс не найден");
            }
            if (!statusValidator.canTransition(flight.getStatus(), newStatus)) {
                return OperationResult.error(String.format(
                        "Недопустимый переход статуса: %s → %s",
                        flight.getStatus(), newStatus));
            }
            flight.setStatus(newStatus);
            flightRepository.save(session, flight);
            return OperationResult.success(
                    "Статус рейса " + flight.getFlightNumber() + " изменён на " + newStatus);
        });
    }

    @Override
    public OperationResult updatePassengerWeight(Long flightId, double passengerWeight) {
        return HibernateUtil.inTransaction(session -> {
            Flight flight = flightRepository.findByIdWithDetails(session, flightId);
            if (flight == null) {
                return OperationResult.error("Рейс не найден");
            }
            if (flight.getStatus() == FlightStatus.DEPARTED) {
                return OperationResult.error("Рейс уже вылетел — изменение веса недоступно");
            }
            double cargo = flight.getTotalCargoWeight();
            double maxPayload = flight.getAircraft() != null ? flight.getAircraft().getMaxPayload() : 0.0;
            if (cargo + passengerWeight > maxPayload) {
                return OperationResult.error(String.format(
                        "Общий вес (груз %.0f кг + пассажиры %.0f кг) превышает грузоподъёмность ВС (%.0f кг)",
                        cargo, passengerWeight, maxPayload));
            }
            flight.setPassengerWeight(passengerWeight);
            flightRepository.save(session, flight);
            return OperationResult.success("Установлен вес пассажиров: " + (int) passengerWeight + " кг");
        });
    }

    @Override
    public OperationResult addCargoToFlight(Long flightId, Long compartmentId, double weight, String description) {
        return HibernateUtil.inTransaction(session -> {
            Flight flight = flightRepository.findByIdWithDetails(session, flightId);
            if (flight == null) {
                return OperationResult.error("Рейс не найден");
            }
            if (flight.getStatus() == FlightStatus.DEPARTED) {
                return OperationResult.error("Рейс уже вылетел — загрузка груза недоступна");
            }
            Optional<Compartment> compartment = flight.getAircraft().getCompartments().stream()
                    .filter(c -> c.getId().equals(compartmentId))
                    .findFirst();
            if (compartment.isEmpty()) {
                return OperationResult.error("Отсек не принадлежит рейсу");
            }

            Cargo cargo = new Cargo(weight, description, compartment.get(), flight);
            if (!weightBalanceService.validateAndAddCargo(flight, cargo)) {
                return OperationResult.error(String.format(
                        "Превышен лимит. Отсек: %.0f / %.0f кг; грузоподъёмность ВС: %.0f кг",
                        compartment.get().getCurrentWeight(), compartment.get().getMaxWeight(),
                        flight.getAircraft().getMaxPayload()));
            }
            flightRepository.save(session, flight);
            return OperationResult.success(String.format(
                    "Груз %.0f кг добавлен в %s", weight, compartment.get().getName()));
        });
    }

    @Override
    public OperationResult unloadLastCargoFromFlight(Long flightId, Long compartmentId) {
        return HibernateUtil.inTransaction(session -> {
            Flight flight = flightRepository.findByIdWithDetails(session, flightId);
            if (flight == null) {
                return OperationResult.error("Рейс не найден");
            }
            Optional<Compartment> compartment = flight.getAircraft().getCompartments().stream()
                    .filter(c -> c.getId().equals(compartmentId))
                    .findFirst();
            if (compartment.isEmpty()) {
                return OperationResult.error("Отсек не принадлежит рейсу");
            }
            if (!weightBalanceService.unloadLastCargo(flight, compartment.get())) {
                return OperationResult.error("В отсеке нет грузов для разгрузки");
            }
            flightRepository.save(session, flight);
            return OperationResult.success(
                    "Последний груз выгружен из " + compartment.get().getName());
        });
    }


    @Override
    public OperationResult assignResources(Long flightId, Long equipmentId, Long brigadeId) {
        return HibernateUtil.inTransaction(session -> {
            Flight flight = flightRepository.findById(session, flightId);
            if (flight == null) {
                return OperationResult.error("Рейс не найден");
            }
            if (flight.getStatus() == FlightStatus.DEPARTED) {
                return OperationResult.error("Рейс уже вылетел — назначение ресурсов недоступно");
            }

            if (equipmentId != null) {
                Resource equipment = resourceRepository.findById(session, equipmentId);
                if (!(equipment instanceof Equipment)) {
                    return OperationResult.error("Техника не найдена");
                }
                if (!equipment.isFree() && !flight.equals(equipment.getAssignedFlight())) {
                    return OperationResult.error("Техника «" + equipment.getName() + "» занята другим рейсом");
                }
                equipment.setAssignedFlight(flight);
                resourceRepository.save(session, equipment);
            }
            if (brigadeId != null) {
                Resource brigade = resourceRepository.findById(session, brigadeId);
                if (!(brigade instanceof Brigade)) {
                    return OperationResult.error("Бригада не найдена");
                }
                if (!brigade.isFree() && !flight.equals(brigade.getAssignedFlight())) {
                    return OperationResult.error("Бригада «" + brigade.getName() + "» занята другим рейсом");
                }
                brigade.setAssignedFlight(flight);
                resourceRepository.save(session, brigade);
            }
            return OperationResult.success("Ресурсы назначены рейсу " + flight.getFlightNumber());
        });
    }

    @Override
    public OperationResult deleteFlight(Long flightId) {
        return HibernateUtil.inTransaction(session -> {
            Flight flight = flightRepository.findById(session, flightId);
            if (flight == null) {
                return OperationResult.error("Рейс не найден");
            }
            // Освобождаем ресурсы до удаления рейса — иначе FK-ограничение не даст удалить
            List<Resource> assigned = resourceRepository.findByAssignedFlight(session, flight);
            for (Resource resource : assigned) {
                resource.setAssignedFlight(null);
                resourceRepository.save(session, resource);
            }
            flightRepository.delete(session, flight);
            return OperationResult.success("Рейс " + flight.getFlightNumber() + " удалён");
        });
    }

    @Override
    public List<Resource> getAssignedResources(Flight flight) {
        return resourceRepository.findByAssignedFlight(flight);
    }

    @Override
    public OperationResult createFlight(String flightNumber, LocalDateTime departureDateTime,
                                        AircraftType aircraftType, String tailNumber) {
        if (flightNumber == null || flightNumber.isBlank()) {
            return OperationResult.error("Введите номер рейса");
        }
        String number = flightNumber.trim().toUpperCase();

        String tail = tailNumber == null ? "" : tailNumber.trim();
        if (tail.isBlank()) {
            return OperationResult.error("Введите бортовой номер");
        }
        if (aircraftType == null) {
            return OperationResult.error("Выберите тип воздушного судна");
        }
        if (departureDateTime == null) {
            return OperationResult.error("Укажите дату и время вылета");
        }
        if (departureDateTime.isBefore(LocalDateTime.now())) {
            return OperationResult.error("Дата вылета не может быть в прошлом");
        }

        if (flightRepository.findByFlightNumber(number) != null) {
            return OperationResult.error("Рейс с номером " + number + " уже существует");
        }

        return HibernateUtil.inTransaction(session -> {
            Aircraft aircraft = aircraftRepository.findByTailNumber(session, tail);

            if (aircraft == null) {
                return OperationResult.error("Борт " + tail + " не найден в системе");
            }

            if (aircraft.getAircraftType() != aircraftType) {
                return OperationResult.error("Борт " + tail + " относится к типу "
                        + aircraft.getAircraftType().getDisplayName()
                        + ", нельзя назначить его рейсу типа " + aircraftType.getDisplayName());
            }

            Flight flight = new Flight(number, departureDateTime, aircraft);
            session.persist(flight);
            return OperationResult.success("Рейс " + number + " создан (борт " + tail + ")");
        });
    }

}
