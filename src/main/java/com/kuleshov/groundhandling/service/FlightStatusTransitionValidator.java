package com.kuleshov.groundhandling.service;

import com.kuleshov.groundhandling.domain.FlightStatus;

import java.util.EnumMap;
import java.util.Map;
import java.util.Set;


public class FlightStatusTransitionValidator {

    private static final Map<FlightStatus, Set<FlightStatus>> ALLOWED =
            new EnumMap<>(FlightStatus.class);

    static {
        ALLOWED.put(FlightStatus.SCHEDULED, Set.of(FlightStatus.ARRIVED, FlightStatus.DELAYED));
        ALLOWED.put(FlightStatus.DELAYED,   Set.of(FlightStatus.ARRIVED, FlightStatus.SCHEDULED));
        ALLOWED.put(FlightStatus.ARRIVED,   Set.of(FlightStatus.LOADING));
        ALLOWED.put(FlightStatus.LOADING,   Set.of(FlightStatus.BOARDING));
        ALLOWED.put(FlightStatus.BOARDING,  Set.of(FlightStatus.READY));
        ALLOWED.put(FlightStatus.READY,     Set.of(FlightStatus.DEPARTED));
        ALLOWED.put(FlightStatus.DEPARTED,  Set.of());
    }

    public boolean canTransition(FlightStatus from, FlightStatus to) {
        Set<FlightStatus> targets = ALLOWED.get(from);
        return targets != null && targets.contains(to);
    }
}
