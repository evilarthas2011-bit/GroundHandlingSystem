package com.kuleshov.groundhandling.service;

import com.kuleshov.groundhandling.HibernateUtil;
import com.kuleshov.groundhandling.domain.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class WeightBalanceServiceTest extends IntegrationTestBase {

    private WeightBalanceService service;
    private Long flightId;
    private Long fwdId;
    private Long aftId;

    @BeforeEach
    void setUp() {
        service = new WeightBalanceService();
        long seed = System.nanoTime();

        HibernateUtil.inTransaction(session -> {
            Aircraft aircraft = Aircraft.createFor(AircraftType.BOEING_737, "RA-WB-" + seed);
            session.persist(aircraft);

            Flight flight = new Flight();
            flight.setFlightNumber("WB-" + seed);
            flight.setAircraft(aircraft);
            flight.setDepartureDateTime(LocalDateTime.now().plusDays(1));
            flight.setStatus(FlightStatus.SCHEDULED);
            session.persist(flight);

            flightId = flight.getId();
            fwdId = aircraft.getCompartments().get(0).getId();
            aftId = aircraft.getCompartments().get(1).getId();
            return null;
        });
    }


    private boolean attemptLoad(Long compartmentId, double weight, String description) {
        return HibernateUtil.inTransaction(session -> {
            Flight flight = session.get(Flight.class, flightId);
            flight.getCargoList().size();                       // инициализируем ленивые коллекции
            flight.getAircraft().getCompartments().size();
            Compartment compartment = session.get(Compartment.class, compartmentId);

            Cargo cargo = new Cargo();
            cargo.setDescription(description);
            cargo.setWeight(weight);
            cargo.setCompartment(compartment);

            boolean accepted = service.validateAndAddCargo(flight, cargo);
            if (accepted) {
                session.persist(cargo);
            }
            return accepted;
        });
    }

    private double persistedWeight(Long compartmentId) {
        return HibernateUtil.inTransaction(session -> {
            Compartment compartment = session.get(Compartment.class, compartmentId);
            compartment.getCargoList().size();
            return compartment.getCurrentWeight();
        });
    }

    @Test
    @DisplayName("Погрузка ровно до лимита отсека (3400 кг в FWD) допустима")
    void loadExactlyToLimit_ok() {
        assertTrue(attemptLoad(fwdId, 3400.0, "До лимита"));
        assertEquals(3400.0, persistedWeight(fwdId), 0.001, "вес отсека должен сохраниться");
    }

    @Test
    @DisplayName("Погрузка сверх лимита отсека отклоняется")
    void loadOverLimit_rejected() {
        assertFalse(attemptLoad(aftId, 2800.1, "Перегруз AFT"));
        assertEquals(0.0, persistedWeight(aftId), 0.001, "отклонённый груз не должен сохраниться");
    }

    @Test
    @DisplayName("Сумма партий учитывается: вторая партия упирается в остаток лимита")
    void cumulativeLoad_respectsLimit() {
        assertTrue(attemptLoad(aftId, 2000.0, "Партия 1"));
        assertFalse(attemptLoad(aftId, 1000.0, "Партия 2"),
                "2000 + 1000 > лимита 2800 — вторая партия должна отклоняться");
        assertTrue(attemptLoad(aftId, 800.0, "Партия 3"),
                "2000 + 800 ровно лимит — принимается");
        assertEquals(2800.0, persistedWeight(aftId), 0.001);
    }

    @Test
    @DisplayName("unloadLastCargo снимает последнюю партию, вес отсека уменьшается")
    void unloadLastCargo_removesLatest() {
        assertTrue(attemptLoad(aftId, 1000.0, "Партия 1"));
        assertTrue(attemptLoad(aftId, 500.0, "Партия 2"));

        boolean removed = HibernateUtil.inTransaction(session -> {
            Flight flight = session.get(Flight.class, flightId);
            flight.getCargoList().size();
            flight.getAircraft().getCompartments().size();
            Compartment compartment = session.get(Compartment.class, aftId);
            return service.unloadLastCargo(flight, compartment);
        });
        assertTrue(removed, "последняя партия должна сниматься");
        assertEquals(1000.0, persistedWeight(aftId), 0.001,
                "после снятия последней партии должен остаться только вес первой");
    }

    @Test
    @DisplayName("unloadLastCargo на пустом отсеке возвращает false")
    void unloadLastCargo_emptyCompartment_false() {
        boolean removed = HibernateUtil.inTransaction(session -> {
            Flight flight = session.get(Flight.class, flightId);
            flight.getCargoList().size();
            flight.getAircraft().getCompartments().size();
            Compartment compartment = session.get(Compartment.class, aftId);
            return service.unloadLastCargo(flight, compartment);
        });
        assertFalse(removed);
    }
    @Test
    @DisplayName("Пассажиры учитываются в грузоподъёмности: груз при почти полном весе пассажиров отклоняется")
    void passengersConsumePayload_rejected() {

        HibernateUtil.inTransaction(session -> {
            Flight flight = session.get(Flight.class, flightId);
            flight.setPassengerWeight(6000.0);
            return null;
        });

        assertFalse(attemptLoad(fwdId, 500.0, "Груз при тяжёлых пассажирах"),
                "0 + 6000 + 500 > 6200 — груз должен отклоняться");
        assertEquals(0.0, persistedWeight(fwdId), 0.001,
                "отклонённый груз не должен сохраниться");
    }


}
