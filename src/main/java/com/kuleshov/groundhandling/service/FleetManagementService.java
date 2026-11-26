package com.kuleshov.groundhandling.service;

import com.kuleshov.groundhandling.domain.Aircraft;
import com.kuleshov.groundhandling.domain.AircraftType;

import java.util.List;

public interface FleetManagementService {
    OperationResult registerAircraft(AircraftType type, String tailNumber);
    List<Aircraft> getFleet();
}
