package com.kuleshov.groundhandling.service;

import com.kuleshov.groundhandling.HibernateUtil;
import com.kuleshov.groundhandling.domain.*;
import com.kuleshov.groundhandling.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.*;

class FlightManagementServiceTest extends IntegrationTestBase {

    private FlightManagementService service;
    private LocalDateTime tomorrow;

    @BeforeEach
    void setUp() {
        service = new FlightManagementServiceImpl(
                new FlightRepositoryImpl(),
                new ResourceRepositoryImpl(),
                new AircraftRepositoryImpl(),
                new WeightBalanceService()
        );
        tomorrow = LocalDateTime.now().plusDays(1).truncatedTo(ChronoUnit.SECONDS);
    }


    private OperationResult createFlight(String number, AircraftType type,
                                         String tail, LocalDateTime when) {
        return service.createFlight(number, when, type, tail);
    }

    private Aircraft createAircraft(String tail, AircraftType type) {
        return HibernateUtil.inTransaction(session -> {
            Aircraft aircraft = new Aircraft();
            aircraft.setTailNumber(tail);
            aircraft.setAircraftType(type);
            session.persist(aircraft);
            return aircraft;
        });
    }

    private Flight findFlight(String number) {
        return HibernateUtil.inTransaction(session ->
                session.createQuery("FROM Flight f WHERE f.flightNumber = :n", Flight.class)
                        .setParameter("n", number)
                        .uniqueResult());
    }
    private Long createFlightEntity(String number, String tail, AircraftType type) {
        return HibernateUtil.inTransaction(session -> {
            Aircraft aircraft = Aircraft.createFor(type, tail);
            session.persist(aircraft);
            Flight flight = new Flight(number, LocalDateTime.now().plusDays(1), aircraft);
            flight.setStatus(FlightStatus.SCHEDULED);
            session.persist(flight);
            return flight.getId();
        });
    }

    @Test
    @DisplayName("createFlight: пустой номер рейса — ошибка")
    void createFlight_emptyNumber_error() {
        OperationResult result = createFlight("", AircraftType.BOEING_737, "RA-00001", tomorrow);
        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().toLowerCase().contains("номер"));
    }

    @Test
    @DisplayName("createFlight: дата вылета в прошлом — ошибка")
    void createFlight_pastDate_error() {
        OperationResult result = createFlight("SU-9001", AircraftType.BOEING_737,
                "RA-00001", LocalDateTime.now().minusHours(1));
        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().toLowerCase().contains("прошл")
                || result.getMessage().toLowerCase().contains("дата"));
    }

    @Test
    @DisplayName("createFlight: несуществующий борт — ошибка")
    void createFlight_unknownTailNumber_error() {
        OperationResult result = createFlight("SU-9002", AircraftType.BOEING_737, "RA-NOPE", tomorrow);
        assertFalse(result.isSuccess());
    }

    @Test
    @DisplayName("createFlight: тип рейса не совпадает с типом борта — ошибка")
    void createFlight_typeMismatch_error() {
        createAircraft("RA-77777", AircraftType.AIRBUS_A320); // борт — A320

        OperationResult result = createFlight(
                "SU-9003", AircraftType.BOEING_737, "RA-77777", tomorrow); // рейс — Boeing
        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().toLowerCase().contains("тип"));
    }

    @Test
    @DisplayName("createFlight: дубликат номера рейса — ошибка")
    void createFlight_duplicateNumber_error() {
        createAircraft("RA-11111", AircraftType.BOEING_737);

        OperationResult first = createFlight("SU-1111", AircraftType.BOEING_737, "RA-11111", tomorrow);
        assertTrue(first.isSuccess(), "первое создание должно пройти: " + first.getMessage());

        OperationResult second = createFlight("SU-1111", AircraftType.BOEING_737, "RA-11111", tomorrow);
        assertFalse(second.isSuccess());
        assertTrue(second.getMessage().toLowerCase().contains("существ")
                || second.getMessage().toLowerCase().contains("дубл"));
    }


    @Test
    @DisplayName("createFlight: валидный рейс создан со статусом SCHEDULED")
    void createFlight_valid_success() {
        createAircraft("RA-22222", AircraftType.BOEING_737);

        OperationResult result = createFlight("SU-2222", AircraftType.BOEING_737, "RA-22222", tomorrow);
        assertTrue(result.isSuccess(), "ожидали успех, получили: " + result.getMessage());

        Flight flight = findFlight("SU-2222");
        assertNotNull(flight, "рейс должен быть в базе");
        assertEquals("RA-22222", flight.getAircraft().getTailNumber());
        assertEquals(FlightStatus.SCHEDULED, flight.getStatus());
        assertEquals(tomorrow, flight.getDepartureDateTime());
    }


    @Test
    @DisplayName("Рейс в статусе DEPARTED закрыт для погрузки и назначения ресурсов")
    void departedFlight_closedForOperations() {
        createAircraft("RA-66666", AircraftType.BOEING_737);
        OperationResult created = createFlight("SU-6666", AircraftType.BOEING_737, "RA-66666", tomorrow);
        assertTrue(created.isSuccess());

        Flight flight = HibernateUtil.inTransaction(session -> {
            Flight f = session.createQuery(
                            "FROM Flight f WHERE f.flightNumber = :n", Flight.class)
                    .setParameter("n", "SU-6666")
                    .uniqueResult();
            f.setStatus(FlightStatus.DEPARTED);
            session.merge(f);
            return f;
        });

        assertTrue(flight.isClosedForOperations(),
                "гейт должен блокировать операции над улетевшим рейсом");
    } @Test
    @DisplayName("updatePassengerWeight: вес в пределах остатка грузоподъёмности сохраняется")
    void passengerWeight_withinLimit_success() {
        Long flightId = createFlightEntity("SU-7001", "RA-70001", AircraftType.BOEING_737);

        OperationResult result = service.updatePassengerWeight(flightId, 6000.0);
        assertTrue(result.isSuccess(), "6000 из 6200 должно приниматься: " + result.getMessage());

        Double persisted = HibernateUtil.inTransaction(session ->
                session.get(Flight.class, flightId).getPassengerWeight());
        assertEquals(6000.0, persisted, 0.001, "вес пассажиров должен сохраниться в базе");
    }

    @Test
    @DisplayName("updatePassengerWeight: вес сверх грузоподъёмности борта отклоняется")
    void passengerWeight_overLimit_rejected() {
        Long flightId = createFlightEntity("SU-7002", "RA-70002", AircraftType.BOEING_737);

        OperationResult result = service.updatePassengerWeight(flightId, 7000.0);
        assertFalse(result.isSuccess(), "7000 больше payload 6200 — должно отклоняться");

        Double persisted = HibernateUtil.inTransaction(session ->
                session.get(Flight.class, flightId).getPassengerWeight());
        assertEquals(0.0, persisted, 0.001, "отклонённый вес не должен сохраниться");
    }

    @Test
    @DisplayName("updatePassengerWeight: учитывает уже загруженный груз")
    void passengerWeight_considersCargo_rejected() {
        Long flightId = createFlightEntity("SU-7003", "RA-70003", AircraftType.BOEING_737);

        Long compartmentId = HibernateUtil.inTransaction(session ->
                session.get(Flight.class, flightId).getAircraft().getCompartments().get(0).getId());

        // грузим 1000 кг: остаток payload = 6200 - 1000 = 5200
        OperationResult cargoResult = service.addCargoToFlight(
                flightId, compartmentId, 1000.0, "Груз для проверки пассажиров");
        assertTrue(cargoResult.isSuccess(), "груз 1000 кг должен приниматься: " + cargoResult.getMessage());

        OperationResult over = service.updatePassengerWeight(flightId, 5300.0);
        assertFalse(over.isSuccess(), "1000 + 5300 > 6200 — вес пассажиров должен отклоняться");

        OperationResult exact = service.updatePassengerWeight(flightId, 5200.0);
        assertTrue(exact.isSuccess(), "1000 + 5200 ровно 6200 — должно приниматься: " + exact.getMessage());
    }

    @Test
    @DisplayName("updatePassengerWeight: отрицательный вес отклоняется")
    void passengerWeight_negative_rejected() {
        Long flightId = createFlightEntity("SU-7004", "RA-70004", AircraftType.BOEING_737);

        OperationResult result = service.updatePassengerWeight(flightId, -100.0);
        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().toLowerCase().contains("отрицательн"));
    }

    @Test
    @DisplayName("updatePassengerWeight: рейс в статусе DEPARTED закрыт для изменения")
    void passengerWeight_departedFlight_rejected() {
        Long flightId = createFlightEntity("SU-7005", "RA-70005", AircraftType.BOEING_737);

        HibernateUtil.inTransaction(session -> {
            Flight f = session.get(Flight.class, flightId);
            f.setStatus(FlightStatus.DEPARTED);
            return null;
        });

        OperationResult result = service.updatePassengerWeight(flightId, 1000.0);
        assertFalse(result.isSuccess(), "улетевший рейс закрыт для операций");
        assertTrue(result.getMessage().toLowerCase().contains("закрыт"));
    }
}
