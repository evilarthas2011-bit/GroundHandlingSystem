package com.kuleshov.groundhandling.service;

import com.kuleshov.groundhandling.domain.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class WeightBalanceServiceTest {

    private WeightBalanceService weightBalanceService;
    private Flight flight;
    private Compartment compartment;

    @BeforeEach
    void setUp() {
        weightBalanceService = new WeightBalanceService();

        flight = new Flight();
        flight.setId(1L);
        flight.setFlightNumber("SU-1234");
        flight.setTotalCargoWeight(0.0);
        flight.setCargoList(new ArrayList<>());

        Aircraft aircraft = new Aircraft(AircraftType.BOEING_737, "RA-12345");
        flight.setAircraft(aircraft);

        compartment = new Compartment();
        compartment.setId(1L);
        compartment.setName("Носовой отсек");
        compartment.setMaxWeight(5000.0);
        compartment.setCurrentWeight(3000.0);
    }

    @Test
    void validateAndAddCargo_success_withinLimits() {
        Cargo cargo = new Cargo();
        cargo.setWeight(1000.0);
        cargo.setCompartment(compartment);

        boolean result = weightBalanceService.validateAndAddCargo(flight, cargo);

        assertTrue(result, "Груз должен быть успешно добавлен в пределах лимитов");
        assertEquals(4000.0, compartment.getCurrentWeight(), "Вес отсека должен увеличиться");
        assertEquals(1000.0, flight.getTotalCargoWeight(), "Общий вес рейса должен увеличиться");
        assertEquals(1, flight.getCargoList().size(), "Список грузов должен содержать 1 элемент");
    }

    @Test
    void validateAndAddCargo_fail_exceedsCompartmentLimit() {
        Cargo cargo = new Cargo();
        cargo.setWeight(2500.0);
        cargo.setCompartment(compartment);

        boolean result = weightBalanceService.validateAndAddCargo(flight, cargo);

        assertFalse(result, "Операция должна быть отклонена при превышении лимита отсека");
        assertEquals(3000.0, compartment.getCurrentWeight(), "Вес отсека не должен измениться");
        assertEquals(0.0, flight.getTotalCargoWeight(), "Общий вес рейса не должен измениться");
        assertEquals(0, flight.getCargoList().size(), "Список грузов должен остаться пустым");
    }

    @Test
    void validateAndAddCargo_fail_exceedsAircraftPayload() {
        compartment.setMaxWeight(999999.0);
        flight.setTotalCargoWeight(999999.0);

        Cargo cargo = new Cargo();
        cargo.setWeight(1000.0);
        cargo.setCompartment(compartment);

        boolean result = weightBalanceService.validateAndAddCargo(flight, cargo);

        assertFalse(result, "Операция должна быть отклонена при превышении грузоподъемности ВС");
    }

    @Test
    void unloadLastCargo_success() {
        Cargo cargo1 = new Cargo();
        cargo1.setWeight(500.0);
        cargo1.setCompartment(compartment);

        Cargo cargo2 = new Cargo();
        cargo2.setWeight(300.0);
        cargo2.setCompartment(compartment);

        flight.getCargoList().add(cargo1);
        flight.getCargoList().add(cargo2);
        flight.setTotalCargoWeight(800.0);
        compartment.setCurrentWeight(3800.0);

        boolean result = weightBalanceService.unloadLastCargo(flight, compartment);

        assertTrue(result, "Разгрузка последнего груза должна быть успешной");
        assertEquals(3500.0, compartment.getCurrentWeight(), "Вес отсека должен уменьшиться на вес последнего груза");
        assertEquals(500.0, flight.getTotalCargoWeight(), "Общий вес рейса должен уменьшиться");
        assertEquals(1, flight.getCargoList().size(), "В списке должен остаться 1 груз");
    }

    @Test
    void unloadLastCargo_fail_emptyCompartment() {
        flight.getCargoList().clear();
        flight.setTotalCargoWeight(0.0);
        compartment.setCurrentWeight(3000.0);

        boolean result = weightBalanceService.unloadLastCargo(flight, compartment);

        assertFalse(result, "Разгрузка из отсека без грузов должна быть отклонена");
        assertEquals(3000.0, compartment.getCurrentWeight(), "Вес отсека не должен измениться");
        assertEquals(0.0, flight.getTotalCargoWeight(), "Общий вес рейса не должен измениться");
    }

    @Test
    void validateAndAddCargo_multipleCargosInOneCompartment() {
        double initialWeight = compartment.getCurrentWeight();
        double totalAdded = 0.0;

        for (int i = 0; i < 3; i++) {
            Cargo cargo = new Cargo();
            cargo.setWeight(500.0);
            cargo.setCompartment(compartment);

            boolean result = weightBalanceService.validateAndAddCargo(flight, cargo);

            assertTrue(result, "Каждое добавление груза должно быть успешным");
            totalAdded += 500.0;
        }

        assertEquals(initialWeight + totalAdded, compartment.getCurrentWeight(),
                "Вес отсека должен увеличиться на сумму всех грузов");
        assertEquals(totalAdded, flight.getTotalCargoWeight(),
                "Общий вес рейса должен равняться сумме всех добавленных грузов");
        assertEquals(3, flight.getCargoList().size(),
                "Список грузов должен содержать 3 элемента");
    }
}