package com.kuleshov.groundhandling.service;

import com.kuleshov.groundhandling.domain.Flight;
import com.kuleshov.groundhandling.domain.Resource;
import java.util.List;

public interface FlightManagementService {
    OperationResult addCargoToFlight(Long flightId, Long compartmentId, double weight);
    OperationResult unloadLastCargoFromFlight(Long flightId, Long compartmentId);
    OperationResult assignResources(Long flightId, Long equipmentId, Long brigadeId);
    OperationResult deleteFlight(Long flightId);
    List<Resource> getAssignedResources(Flight flight);
}