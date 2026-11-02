package com.kuleshov.groundhandling.service;

import com.kuleshov.groundhandling.domain.Cargo;
import com.kuleshov.groundhandling.domain.Compartment;
import com.kuleshov.groundhandling.domain.Flight;
import java.util.List;

public class WeightBalanceService {

    public boolean validateAndAddCargo(Flight flight, Cargo cargo) {
        Compartment compartment = cargo.getCompartment();

        // 1. Проверка лимита конкретного отсека
        double newCompartmentWeight = compartment.getCurrentWeight() + cargo.getWeight();
        if (newCompartmentWeight > compartment.getMaxWeight()) {
            return false;
        }

        // 2. Проверка ОБЩЕЙ грузоподъемности (Текущий груз + Пассажиры + Новый груз)
        double currentCargoWeight = flight.getTotalCargoWeight() != null ? flight.getTotalCargoWeight() : 0.0;
        double currentPassengerWeight = flight.getPassengerWeight() != null ? flight.getPassengerWeight() : 0.0;
        double maxPayload = flight.getAircraft().getMaxPayload();

        double newTotalWeight = currentCargoWeight + currentPassengerWeight + cargo.getWeight();

        if (newTotalWeight > maxPayload) {
            return false;
        }

        compartment.setCurrentWeight(newCompartmentWeight);
        flight.setTotalCargoWeight(currentCargoWeight + cargo.getWeight());
        flight.getCargoList().add(cargo);
        return true;
    }

    public boolean unloadLastCargo(Flight flight, Compartment compartment) {
        List<Cargo> cargos = flight.getCargoList();
        for (int i = cargos.size() - 1; i >= 0; i--) {
            Cargo cargo = cargos.get(i);
            if (cargo.getCompartment().equals(compartment)) {
                compartment.setCurrentWeight(compartment.getCurrentWeight() - cargo.getWeight());
                flight.setTotalCargoWeight(flight.getTotalCargoWeight() - cargo.getWeight());
                cargos.remove(i);
                return true;
            }
        }
        return false;
    }
}