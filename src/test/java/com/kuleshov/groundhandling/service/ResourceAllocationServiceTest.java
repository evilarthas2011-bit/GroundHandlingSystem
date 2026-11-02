package com.kuleshov.groundhandling.service;

import com.kuleshov.groundhandling.domain.*;
import com.kuleshov.groundhandling.repository.ResourceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ResourceAllocationServiceTest {

    @Mock
    private ResourceRepository resourceRepository;

    @InjectMocks
    private ResourceAllocationService allocationService;

    private Flight flight;
    private Equipment equipment;
    private Brigade brigade;

    @BeforeEach
    void setUp() {
        flight = new Flight();
        flight.setId(1L);
        flight.setFlightNumber("SU-1234");

        equipment = new Equipment();
        equipment.setId(1L);
        equipment.setModelName("Тягач T-100");

        brigade = new Brigade();
        brigade.setId(1L);
        brigade.setSpecialization("Грузовая бригада №1");
    }

    @Test
    void allocateResources_success_whenResourcesAvailable() {
        List<Equipment> availableEquipment = new ArrayList<>();
        availableEquipment.add(equipment);

        List<Brigade> availableBrigades = new ArrayList<>();
        availableBrigades.add(brigade);

        when(resourceRepository.findAvailableEquipment()).thenReturn(availableEquipment);
        when(resourceRepository.findAvailableBrigades()).thenReturn(availableBrigades);

        OperationResult result = allocationService.allocateResources(flight);

        assertTrue(result.isSuccess());
        assertEquals(flight, equipment.getAssignedFlight());
        assertEquals(flight, brigade.getAssignedFlight());
        verify(resourceRepository, times(1)).save(equipment);
        verify(resourceRepository, times(1)).save(brigade);
    }

    @Test
    void allocateResources_fail_whenNoEquipmentAvailable() {
        when(resourceRepository.findAvailableEquipment()).thenReturn(Collections.emptyList());
        when(resourceRepository.findAvailableBrigades()).thenReturn(List.of(brigade));

        OperationResult result = allocationService.allocateResources(flight);

        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("техники"));
        verify(resourceRepository, never()).save(any(Resource.class));
    }

    @Test
    void allocateResources_fail_whenNoBrigadeAvailable() {
        List<Equipment> availableEquipment = new ArrayList<>();
        availableEquipment.add(equipment);

        when(resourceRepository.findAvailableEquipment()).thenReturn(availableEquipment);
        when(resourceRepository.findAvailableBrigades()).thenReturn(Collections.emptyList());

        OperationResult result = allocationService.allocateResources(flight);

        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("бригад"));
        verify(resourceRepository, never()).save(any(Resource.class));
    }
}