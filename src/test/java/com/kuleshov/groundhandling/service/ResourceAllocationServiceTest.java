package com.kuleshov.groundhandling.service;

import com.kuleshov.groundhandling.domain.*;
import com.kuleshov.groundhandling.repository.ResourceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ResourceAllocationServiceTest {

    private ResourceRepository resourceRepository;
    private ResourceAllocationService allocationService;
    private Flight flight;

    @BeforeEach
    void setUp() {
        resourceRepository = Mockito.mock(ResourceRepository.class);
        allocationService = new ResourceAllocationService(resourceRepository);

        Aircraft aircraft = new Aircraft(AircraftType.BOEING_737, "RA-TEST");
        flight = new Flight("TEST-001", AircraftType.BOEING_737, LocalDateTime.now());
        flight.setAircraft(aircraft);
    }

    @Test
    void testAutoAllocate_WithAvailableResources() {
        Equipment equipment = new Equipment("Тягач T-100", 5000.0);
        Brigade brigade = new Brigade(6, "Погрузочная бригада");

        when(resourceRepository.findAvailableEquipment()).thenReturn(Collections.singletonList(equipment));
        when(resourceRepository.findAvailableBrigades()).thenReturn(Collections.singletonList(brigade));

        allocationService.autoAllocate(flight);

        assertEquals(flight, equipment.getAssignedFlight(), "Техника должна быть назначена на рейс");
        assertEquals(flight, brigade.getAssignedFlight(), "Бригада должна быть назначена на рейс");
        verify(resourceRepository, times(2)).update(any(Resource.class));
    }

    @Test
    void testAutoAllocate_NoAvailableEquipment() {
        Equipment equipment = new Equipment("Тягач T-100", 5000.0);
        Brigade brigade = new Brigade(6, "Погрузочная бригада");

        when(resourceRepository.findAvailableEquipment()).thenReturn(Collections.emptyList());
        when(resourceRepository.findAvailableBrigades()).thenReturn(Collections.singletonList(brigade));

        allocationService.autoAllocate(flight);

        assertNull(equipment.getAssignedFlight(), "Техника не должна быть назначена");
        assertEquals(flight, brigade.getAssignedFlight(), "Бригада должна быть назначена");
        verify(resourceRepository, times(1)).update(any(Resource.class));
    }

    @Test
    void testAutoAllocate_NoAvailableResources() {
        when(resourceRepository.findAvailableEquipment()).thenReturn(Collections.emptyList());
        when(resourceRepository.findAvailableBrigades()).thenReturn(Collections.emptyList());

        allocationService.autoAllocate(flight);

        verify(resourceRepository, never()).update(any(Resource.class));
    }
}