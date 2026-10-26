package com.kuleshov.groundhandling.service;

import com.kuleshov.groundhandling.domain.Cargo;
import com.kuleshov.groundhandling.domain.Compartment;
import com.kuleshov.groundhandling.domain.Flight;


public class WeightBalanceService {

    public boolean validateAndAddCargo(Flight flight, Cargo cargo) {
        Compartment compartment = cargo.getCompartment();
        if (compartment.getCurrentWeight() + cargo.getWeight() > compartment.getMaxWeight()) {
            System.err.println("Превышен  лимит веса отсека '" + compartment.getName() + "'!");
            return false;
        }
        double newTotalWeight = flight.getTotalCargoWeight() + cargo.getWeight();
        if (newTotalWeight > flight.getAircraft().getMaxPayload()) {
            System.err.println("Превышена максимальная грузоподъемность ВС " + flight.getAircraft().getType().getDisplayName() + "!");
            return false;
        }
        compartment.addWeight(cargo.getWeight());
        flight.setTotalCargoWeight(newTotalWeight);
        System.out.println("Груз " + cargo.getWeight() + " кг добавлен в отсек '" + compartment.getName() + "'.");
        return true;
    }
}