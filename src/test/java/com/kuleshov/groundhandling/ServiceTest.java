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

            Aircraft aircraft737 = new Aircraft(AircraftType.BOEING_737);
            Compartment nose737 = new Compartment("Носовой отсек", 3000.0);
            Compartment aft737 = new Compartment("Хвостовой отсек", 4000.0);
            aircraft737.addCompartment(nose737);
            aircraft737.addCompartment(aft737);
            session.persist(aircraft737);

            Aircraft aircraft320 = new Aircraft(AircraftType.AIRBUS_A320);
            Compartment nose320 = new Compartment("Носовой отсек", 2500.0);
            aircraft320.addCompartment(nose320);
            session.persist(aircraft320);

            Aircraft aircraft777 = new Aircraft(AircraftType.BOEING_777);
            Compartment nose777 = new Compartment("Носовой отсек", 10000.0);
            aircraft777.addCompartment(nose777);
            session.persist(aircraft777);

            Flight flight1 = new Flight("SU-1234", AircraftType.BOEING_737, LocalDateTime.now());
            flight1.setAircraft(aircraft737);
            flight1.setStatus(FlightStatus.SCHEDULED);
            session.persist(flight1);

            Flight flight2 = new Flight("SU-1235", AircraftType.AIRBUS_A320, LocalDateTime.now().minusHours(2));
            flight2.setAircraft(aircraft320);
            flight2.setStatus(FlightStatus.LOADING);
            session.persist(flight2);

            Flight flight3 = new Flight("SU-1236", AircraftType.BOEING_777, LocalDateTime.now().minusHours(5));
            flight3.setAircraft(aircraft777);
            flight3.setStatus(FlightStatus.DELAYED);
            session.persist(flight3);

            Flight flight4 = new Flight("SU-1237", AircraftType.BOEING_737, LocalDateTime.now().minusHours(8));
            flight4.setAircraft(aircraft737);
            flight4.setStatus(FlightStatus.DEPARTED);
            session.persist(flight4);

            Equipment tractor1 = new Equipment("Тягач T-100", 5000.0);
            Equipment tractor2 = new Equipment("Погрузчик P-200", 3000.0);
            Brigade brigade1 = new Brigade(6, "Погрузочная бригада №1");
            Brigade brigade2 = new Brigade(4, "Погрузочная бригада №2");

            session.persist(tractor1);
            session.persist(tractor2);
            session.persist(brigade1);
            session.persist(brigade2);

            session.getTransaction().commit();

            System.out.println("Тест 1: Контроль веса и центровки\n");
            WeightBalanceService weightService = new WeightBalanceService();

            Cargo cargo1 = new Cargo(1500.0, "Багаж пассажиров", nose737, flight1);
            boolean result1 = weightService.validateAndAddCargo(flight1, cargo1);
            System.out.println("Результат добавления груза 1 (1500 кг): " + (result1 ? "УСПЕХ" : "ОТКАЗ") + "\n");

            Cargo cargo2 = new Cargo(2000.0, "Коммерческий груз", nose737, flight1);
            boolean result2 = weightService.validateAndAddCargo(flight1, cargo2);
            System.out.println("Результат добавления груза 2 (2000 кг): " + (result2 ? "УСПЕХ" : "ОТКАЗ") + "\n");

            System.out.println("Тест 2: Автоматическое распределение ресурсов\n");
            ResourceRepository resourceRepository = new ResourceRepository();
            ResourceAllocationService allocationService = new ResourceAllocationService(resourceRepository);
            allocationService.autoAllocate(flight1);

            System.out.println("\nВсе тесты завершены!");
        } catch (Exception e) {
            System.err.println("Ошибка: " + e.getMessage());
            e.printStackTrace();
        }
    }
}