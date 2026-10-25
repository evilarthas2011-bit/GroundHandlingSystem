package com.kuleshov.groundhandling;

import com.kuleshov.groundhandling.domain.*;
import org.hibernate.Session;

import java.time.LocalDateTime;

public class DatabaseTest {

    public static void main(String[] args) {
        System.out.println(" Тестирование моделей данных \n");

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            session.beginTransaction();

            Aircraft aircraft = new Aircraft("Boeing 737-800", 20000.0);

            Compartment noseCompartment = new Compartment("Носовой отсек", 3000.0);
            Compartment aftCompartment = new Compartment("Хвостовой отсек", 4000.0);

            aircraft.addCompartment(noseCompartment);
            aircraft.addCompartment(aftCompartment);

            session.persist(aircraft);
            System.out.println("✓ Создано ВС: " + aircraft);


            Flight flight = new Flight("SU-1234", "Boeing 737-800", LocalDateTime.now());
            flight.setAircraft(aircraft);
            session.persist(flight);
            System.out.println("✓ Создан рейс: " + flight);

            // 4. Создаем груз
            Cargo cargo1 = new Cargo(1500.0, "Багаж пассажиров", noseCompartment, flight);
            Cargo cargo2 = new Cargo(2000.0, "Коммерческий груз", aftCompartment, flight);

            flight.addCargo(cargo1);
            flight.addCargo(cargo2);

            session.persist(cargo1);
            session.persist(cargo2);
            System.out.println("✓ Создан груз: " + cargo1);
            System.out.println("✓ Создан груз: " + cargo2);

            // 5. Создаем ресурсы (технику и бригады)
            Equipment tractor = new Equipment("Тягач T-100", 5000.0);
            Brigade loadingBrigade = new Brigade(6, "Погрузочная бригада №1");

            session.persist(tractor);
            session.persist(loadingBrigade);
            System.out.println("✓ Создана техника: " + tractor);
            System.out.println("✓ Создана бригада: " + loadingBrigade);

            // 6. Назначаем ресурсы на рейс
            tractor.assignToFlight(flight);
            loadingBrigade.assignToFlight(flight);
            System.out.println("✓ Ресурсы назначены на рейс");

            // 7. Проверяем добавление веса в отсек
            boolean weightAdded = noseCompartment.addWeight(500.0);
            System.out.println("✓ Вес добавлен в отсек: " + weightAdded);
            System.out.println("  Текущий вес отсека: " + noseCompartment.getCurrentWeight() + " кг");

            session.getTransaction().commit();
            System.out.println("\nВсе операции успешно выполнены! ");

        } catch (Exception e) {
            System.err.println("Ошибка: " + e.getMessage());
            e.printStackTrace();
        }
    }
}