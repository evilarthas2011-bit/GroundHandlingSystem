package com.kuleshov.groundhandling;

import com.kuleshov.groundhandling.domain.*;
import com.kuleshov.groundhandling.repository.ResourceRepository;
import com.kuleshov.groundhandling.service.ResourceAllocationService;
import com.kuleshov.groundhandling.service.WeightBalanceService;
import org.hibernate.Session;

import java.time.LocalDateTime;

public class ServiceTest {

    public static void main(String[] args) {
        System.out.println("Тестирование сервисов бизнес-логики\n");

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            session.beginTransaction();
            Aircraft aircraft = new Aircraft(AircraftType.BOEING_737);
            Compartment noseCompartment = new Compartment("Носовой отсек", 3000.0);
            Compartment aftCompartment = new Compartment("Хвостовой отсек", 4000.0);
            aircraft.addCompartment(noseCompartment);
            aircraft.addCompartment(aftCompartment);
            session.persist(aircraft);
            System.out.println("✓ Создано ВС: " + aircraft.getType().getDisplayName() +
                    " (макс. грузоподъемность: " + aircraft.getMaxPayload() + " кг)\n");

            Flight flight = new Flight("SU-1234", AircraftType.BOEING_737, LocalDateTime.now());
            flight.setAircraft(aircraft);
            session.persist(flight);
            System.out.println("✓ Создан рейс: " + flight.getFlightNumber() + "\n");

            Equipment tractor1 = new Equipment("Тягач T-100", 5000.0);
            Equipment tractor2 = new Equipment("Погрузчик P-200", 3000.0);
            Brigade brigade1 = new Brigade(6, "Погрузочная бригада №1");
            Brigade brigade2 = new Brigade(4, "Погрузочная бригада №2");

            session.persist(tractor1);
            session.persist(tractor2);
            session.persist(brigade1);
            session.persist(brigade2);
            System.out.println("Созданы ресурсы: 2 единицы техники, 2 бригады\n");

            session.getTransaction().commit();

            System.out.println("Тест 1: Контроль веса и центровки ===\n");
            WeightBalanceService weightService = new WeightBalanceService();

            Cargo cargo1 = new Cargo(1500.0, "Багаж пассажиров", noseCompartment, flight);
            boolean result1 = weightService.validateAndAddCargo(flight, cargo1);
            System.out.println("Результат добавления груза 1 (1500 кг): " + (result1 ? "УСПЕХ" : "ОТКАЗ") + "\n");

            Cargo cargo2 = new Cargo(2000.0, "Коммерческий груз", noseCompartment, flight);
            boolean result2 = weightService.validateAndAddCargo(flight, cargo2);
            System.out.println("Результат добавления груза 2 (2000 кг): " + (result2 ? "УСПЕХ" : "ОТКАЗ") + "\n");

            Cargo cargo3 = new Cargo(2000.0, "Превышение лимита", noseCompartment, flight);
            boolean result3 = weightService.validateAndAddCargo(flight, cargo3);
            System.out.println("Результат добавления груза 3 (превышение лимита): " + (result3 ? "УСПЕХ" : "ОТКАЗ") + "\n");

            System.out.println("Тест 2: Автоматическое распределение ресурсов\n");
            ResourceRepository resourceRepository = new ResourceRepository();
            ResourceAllocationService allocationService = new ResourceAllocationService(resourceRepository);
            allocationService.autoAllocate(flight);

            System.out.println("\nВсе тесты завершены!");
        } catch (Exception e) {
            System.err.println("Ошибка: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
