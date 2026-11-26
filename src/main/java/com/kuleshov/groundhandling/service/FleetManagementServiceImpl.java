package com.kuleshov.groundhandling.service;

import com.kuleshov.groundhandling.HibernateUtil;
import com.kuleshov.groundhandling.domain.Aircraft;
import com.kuleshov.groundhandling.domain.AircraftType;
import com.kuleshov.groundhandling.repository.AircraftRepository;
import com.kuleshov.groundhandling.repository.AircraftRepositoryImpl;

import java.util.List;

public class FleetManagementServiceImpl implements FleetManagementService {

    private final AircraftRepository aircraftRepository;

    public FleetManagementServiceImpl(AircraftRepository aircraftRepository) {
        this.aircraftRepository = aircraftRepository;
    }

    @Override
    public OperationResult registerAircraft(AircraftType type, String tailNumber) {
        if (type == null) {
            return OperationResult.error("Выберите тип воздушного судна");
        }
        String tail = tailNumber == null ? "" : tailNumber.trim().toUpperCase();
        if (tail.isEmpty()) {
            return OperationResult.error("Введите бортовой номер");
        }
        if (!tail.matches("[A-Z0-9-]{3,12}")) {
            return OperationResult.error("Некорректный формат бортового номера: " +
                    "только латиница, цифры и дефис, от 3 до 12 символов");
        }

        return HibernateUtil.inTransaction(session -> {
            Aircraft existing = aircraftRepository.findByTailNumber(session, tail);
            if (existing != null) {
                return OperationResult.error("Борт " + tail + " уже зарегистрирован в парке");
            }
            Aircraft aircraft = Aircraft.createFor(type, tail);
            session.persist(aircraft);
            return OperationResult.success("Борт " + tail + " добавлен в парк");
        });
    }

    @Override
    public List<Aircraft> getFleet() {
        return aircraftRepository.findAll();
    }
}
