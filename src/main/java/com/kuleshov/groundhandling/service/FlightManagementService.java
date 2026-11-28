package com.kuleshov.groundhandling.service;

import com.kuleshov.groundhandling.domain.AircraftType;
import com.kuleshov.groundhandling.domain.Flight;
import com.kuleshov.groundhandling.domain.FlightStatus;
import com.kuleshov.groundhandling.domain.Resource;

import java.time.LocalDateTime;
import java.util.List;

public interface FlightManagementService {
    OperationResult updateFlightStatus(Long flightId, FlightStatus newStatus);
    OperationResult updatePassengerWeight(Long flightId, double passengerWeight);
    OperationResult addCargoToFlight(Long flightId, Long compartmentId, double weight, String description);
    OperationResult unloadLastCargoFromFlight(Long flightId, Long compartmentId);
    OperationResult assignResources(Long flightId, Long equipmentId, Long brigadeId);
    OperationResult deleteFlight(Long flightId);
    List<Resource> getAssignedResources(Flight flight);
    OperationResult createFlight(String flightNumber, LocalDateTime departureDateTime,
                                 AircraftType aircraftType, String tailNumber);

}
