package com.kuleshov.groundhandling.service;

import com.kuleshov.groundhandling.domain.Cargo;
import com.kuleshov.groundhandling.domain.Compartment;
import com.kuleshov.groundhandling.domain.Flight;

import java.util.List;

public class WeightBalanceService {

    public boolean validateAndAddCargo(Flight flight, Cargo cargo) {
        Compartment compartment = cargo.getCompartment();
        double newCompartmentWeight = compartment.getCurrentWeight() + cargo.getWeight();
        double newTotalWeight = (flight.getTotalCargoWeight() != null ? flight.getTotalCargoWeight() : 0) + cargo.getWeight();

        if (newCompartmentWeight > compartment.getMaxWeight()) return false;
        if (newTotalWeight > flight.getAircraft().getMaxPayload()) return false;

        compartment.setCurrentWeight(newCompartmentWeight);
        flight.setTotalCargoWeight(newTotalWeight);
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