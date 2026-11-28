package com.kuleshov.groundhandling.service;

import com.kuleshov.groundhandling.HibernateUtil;
import com.kuleshov.groundhandling.domain.*;
import com.kuleshov.groundhandling.repository.ResourceRepositoryImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class ResourceManagementServiceTest extends IntegrationTestBase {

    private ResourceManagementService service;
    private LocalDateTime tomorrow;

    @BeforeEach
    void setUp() {
        service = new ResourceManagementServiceImpl(new ResourceRepositoryImpl());
        tomorrow = LocalDateTime.now().plusDays(1);
    }


    private Long createEquipment(String modelName) {
        return HibernateUtil.inTransaction(session -> {
            Equipment equipment = new Equipment();
            equipment.setName(modelName);
            session.persist(equipment);
            return equipment.getId();
        });
    }

    private Long createBrigade(String name, int size) {
        return HibernateUtil.inTransaction(session -> {
            Brigade brigade = new Brigade();
            brigade.setName(name);
            brigade.setSize(size);
            session.persist(brigade);
            return brigade.getId();
        });
    }

    private Flight createDepartedFlight(String number) {
        return HibernateUtil.inTransaction(session -> {
            Aircraft aircraft = new Aircraft();
            aircraft.setTailNumber("RA-" + number);
            aircraft.setAircraftType(AircraftType.BOEING_737);
            session.persist(aircraft);

            Flight flight = new Flight();
            flight.setFlightNumber(number);
            flight.setAircraft(aircraft);
            flight.setDepartureDateTime(tomorrow);
            flight.setStatus(FlightStatus.DEPARTED);
            session.persist(flight);
            return flight;
        });
    }

    @Test
    @DisplayName("addEquipment: корректная техника сохранена в базе")
    void addEquipment_valid_success() {
        OperationResult result = service.addEquipment("Тягач T-100");
        assertTrue(result.isSuccess(), result.getMessage());

        Equipment equipment = HibernateUtil.inTransaction(session ->
                session.createQuery("FROM Equipment e WHERE e.name = :n", Equipment.class)
                        .setParameter("n", "Тягач T-100")
                        .uniqueResult());
        assertNotNull(equipment, "техника должна сохраниться в базе");
    }

    @Test
    @DisplayName("addEquipment: пустая модель — ошибка")
    void addEquipment_empty_error() {
        assertFalse(service.addEquipment("").isSuccess());
        assertFalse(service.addEquipment("   ").isSuccess());
        assertFalse(service.addEquipment(null).isSuccess());

        long count = HibernateUtil.inTransaction(session ->
                session.createQuery("SELECT COUNT(e) FROM Equipment e", Long.class)
                        .uniqueResult());
        assertEquals(0L, count, "ни один из невалидных вызовов не должен был добавить технику");
    }
    @Test
    @DisplayName("addBrigade: нулевой и отрицательный размер — ошибка")
    void addBrigade_invalidSize_error() {
        assertFalse(service.addBrigade("Погрузочная", 0).isSuccess());
        assertFalse(service.addBrigade("Погрузочная", -3).isSuccess());
    }


    @Test
    @DisplayName("freeResource: занятый ресурс освобождён, ссылка на рейс снята")
    void freeResource_busy_success() {
        Flight departed = createDepartedFlight("SU-7777");
        Long resourceId = createEquipment("Тягач T-500");

        HibernateUtil.inTransaction(session -> {
            Equipment equipment = session.get(Equipment.class, resourceId);
            equipment.setAssignedFlight(departed);
            session.merge(equipment);
            return null;
        });

        OperationResult result = service.freeResource(resourceId);
        assertTrue(result.isSuccess(), "занятый ресурс должен освобождаться: " + result.getMessage());

        Equipment released = HibernateUtil.inTransaction(session -> session.get(Equipment.class, resourceId));
        assertNull(released.getAssignedFlight(), "ссылка на рейс должна быть снята");
    }

    @Test
    @DisplayName("freeResource: несуществующий ресурс — ошибка")
    void freeResource_notFound_error() {
        assertFalse(service.freeResource(999999L).isSuccess());
    }

    @Test
    @DisplayName("deleteResource: свободный ресурс удалён из базы")
    void deleteResource_free_success() {
        Long id = createBrigade("Уборочная", 4);

        OperationResult result = service.deleteResource(id);
        assertTrue(result.isSuccess(), result.getMessage());

        Brigade deleted = HibernateUtil.inTransaction(session -> session.get(Brigade.class, id));
        assertNull(deleted, "бригада должна быть удалена");
    }

    @Test
    @DisplayName("deleteResource: занятый ресурс удалить нельзя — ошибка")
    void deleteResource_busy_error() {
        Flight departed = createDepartedFlight("SU-8888");

        Long id = HibernateUtil.inTransaction(session -> {
            Brigade brigade = new Brigade();
            brigade.setName("Погрузочная");
            brigade.setSize(6);
            brigade.setAssignedFlight(departed);
            session.persist(brigade);
            return brigade.getId();
        });

        OperationResult result = service.deleteResource(id);
        assertFalse(result.isSuccess(), "занятый ресурс не должен удаляться");

        Brigade stillThere = HibernateUtil.inTransaction(session -> session.get(Brigade.class, id));
        assertNotNull(stillThere, "занятая бригада должна остаться в базе");
    }
}
