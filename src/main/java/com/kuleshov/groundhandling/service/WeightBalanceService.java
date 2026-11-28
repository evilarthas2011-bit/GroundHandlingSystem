package com.kuleshov.groundhandling.service;

import com.kuleshov.groundhandling.domain.Cargo;
import com.kuleshov.groundhandling.domain.Compartment;
import com.kuleshov.groundhandling.domain.Flight;

public class WeightBalanceService {


    public boolean validateAndAddCargo(Flight flight, Cargo cargo) {
        Compartment compartment = cargo.getCompartment();
        if (compartment != null
                && compartment.getCurrentWeight() + cargo.getWeight() > compartment.getMaxWeight()) {
            return false;
        }
        double maxPayload = flight.getAircraft() != null ? flight.getAircraft().getMaxPayload() : 0.0;
        double passengerWeight = flight.getPassengerWeight() != null ? flight.getPassengerWeight() : 0.0;
        if (flight.getTotalCargoWeight() + passengerWeight + cargo.getWeight() > maxPayload) {
            return false;
        }
        flight.addCargo(cargo);
        return true;
    }

    public boolean unloadLastCargo(Flight flight, Compartment compartment) {
        return flight.getCargoList().stream()
                .filter(c -> compartment.equals(c.getCompartment()))
                .reduce((first, second) -> second)
                .map(last -> {
                    flight.removeCargo(last);
                    return true;
                })
                .orElse(false);
    }
}
