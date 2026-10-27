package com.kuleshov.groundhandling.service;

import com.kuleshov.groundhandling.domain.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class WeightBalanceServiceTest {

    private WeightBalanceService weightBalanceService;
    private Flight flight;
    private Compartment noseCompartment;
    private Compartment aftCompartment;

    @BeforeEach
    void setUp() {
        weightBalanceService = new WeightBalanceService();

        Aircraft aircraft = new Aircraft(AircraftType.BOEING_737, "RA-TEST");
        noseCompartment = new Compartment("Носовой отсек", 3000.0);
        aftCompartment = new Compartment("Хвостовой отсек", 4000.0);
        aircraft.addCompartment(noseCompartment);
        aircraft.addCompartment(aftCompartment);

        flight = new Flight("TEST-001", AircraftType.BOEING_737, LocalDateTime.now());
        flight.setAircraft(aircraft);
        flight.setTotalCargoWeight(0.0);
    }

    @Test
    void testAddCargo_Success() {
        Cargo cargo = new Cargo(1000.0, "Тестовый груз", noseCompartment, flight);
        boolean result = weightBalanceService.validateAndAddCargo(flight, cargo);

        assertTrue(result, "Груз должен быть успешно добавлен");
        assertEquals(1000.0, noseCompartment.getCurrentWeight(), "Вес отсека должен обновиться");
        assertEquals(1000.0, flight.getTotalCargoWeight(), "Общий вес рейса должен обновиться");
        assertEquals(1, flight.getCargoList().size(), "Список грузов должен содержать одну запись");
    }

    @Test
    void testAddCargo_ExceedsCompartmentLimit() {
        Cargo cargo = new Cargo(3500.0, "Перегруз отсека", noseCompartment, flight);
        boolean result = weightBalanceService.validateAndAddCargo(flight, cargo);

        assertFalse(result, "Груз не должен быть добавлен при превышении лимита отсека");
        assertEquals(0.0, noseCompartment.getCurrentWeight(), "Вес отсека не должен измениться");
        assertEquals(0.0, flight.getTotalCargoWeight(), "Общий вес рейса не должен измениться");
    }

    @Test
    void testAddCargo_ExceedsAircraftPayload() {
        // Симулируем ситуацию, когда самолет уже почти полностью загружен (19 000 кг из 20 000 кг)
        flight.setTotalCargoWeight(19000.0);

        // Пытаемся добавить груз 2000 кг. Он влезает в отсек (2000 < 3000),
        // но общий вес станет 21 000 кг, что больше макс. грузоподъемности Boeing 737 (20 000 кг).
        Cargo cargo = new Cargo(2000.0, "Перегруз самолета", noseCompartment, flight);
        boolean result = weightBalanceService.validateAndAddCargo(flight, cargo);

        assertFalse(result, "Груз не должен быть добавлен при превышении общей грузоподъемности самолета");
        assertEquals(19000.0, flight.getTotalCargoWeight(), "Общий вес должен остаться прежним");
        assertEquals(0.0, noseCompartment.getCurrentWeight(), "Вес отсека не должен измениться");
    }

    @Test
    void testUnloadCargo_Success() {
        Cargo cargo = new Cargo(1000.0, "Тестовый груз", noseCompartment, flight);
        weightBalanceService.validateAndAddCargo(flight, cargo);

        boolean result = weightBalanceService.unloadLastCargo(flight, noseCompartment);

        assertTrue(result, "Разгрузка должна быть успешной");
        assertEquals(0.0, noseCompartment.getCurrentWeight(), "Вес отсека должен вернуться к нулю");
        assertEquals(0.0, flight.getTotalCargoWeight(), "Общий вес должен вернуться к нулю");
        assertTrue(flight.getCargoList().isEmpty(), "Список грузов должен быть пуст");
    }

    @Test
    void testUnloadCargo_EmptyCompartment() {
        boolean result = weightBalanceService.unloadLastCargo(flight, noseCompartment);

        assertFalse(result, "Разгрузка пустого отсека должна вернуть false");
    }

    @Test
    void testMultipleCargoInSameCompartment() {
        Cargo cargo1 = new Cargo(1000.0, "Груз 1", noseCompartment, flight);
        Cargo cargo2 = new Cargo(1500.0, "Груз 2", noseCompartment, flight);

        weightBalanceService.validateAndAddCargo(flight, cargo1);
        weightBalanceService.validateAndAddCargo(flight, cargo2);

        assertEquals(2500.0, noseCompartment.getCurrentWeight(), "Вес должен суммироваться");
        assertEquals(2500.0, flight.getTotalCargoWeight(), "Общий вес должен суммироваться");
        assertEquals(2, flight.getCargoList().size(), "Должно быть две записи о грузе");
    }
}