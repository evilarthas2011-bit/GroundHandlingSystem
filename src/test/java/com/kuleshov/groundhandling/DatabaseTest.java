package com.kuleshov.groundhandling;

import com.kuleshov.groundhandling.domain.*;
import org.hibernate.Session;

import java.time.LocalDateTime;

public class DatabaseTest {

    public static void main(String[] args) {
        System.out.println(" Тестирование моделей данных\n");

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            session.beginTransaction();

            Aircraft aircraft = new Aircraft(AircraftType.BOEING_737, "RA-73701");

            Compartment noseCompartment = new Compartment("Носовой отсек", 3000.0);
            Compartment aftCompartment = new Compartment("Хвостовой отсек", 4000.0);

            aircraft.addCompartment(noseCompartment);
            aircraft.addCompartment(aftCompartment);

            session.persist(aircraft);
            System.out.println("✅ Создано ВС: " + aircraft.getType().getDisplayName() + " (борт: " + aircraft.getTailNumber() + ")");

            Flight flight = new Flight("SU-1234", AircraftType.BOEING_737, LocalDateTime.now());
            flight.setAircraft(aircraft);
            flight.setTotalCargoWeight(0.0);
            session.persist(flight);
            System.out.println("✅ Создан рейс: " + flight.getFlightNumber());

            Cargo cargo1 = new Cargo(1500.0, "Багаж пассажиров", noseCompartment, flight);
            Cargo cargo2 = new Cargo(2000.0, "Коммерческий груз", aftCompartment, flight);

            flight.getCargoList().add(cargo1);
            flight.getCargoList().add(cargo2);

            session.persist(cargo1);
            session.persist(cargo2);
            System.out.println("✅ Создан груз: " + cargo1.getDescription() + " (" + cargo1.getWeight() + " кг)");
            System.out.println("✅ Создан груз: " + cargo2.getDescription() + " (" + cargo2.getWeight() + " кг)");

            Equipment tractor = new Equipment("Тягач T-100", 5000.0);
            Brigade loadingBrigade = new Brigade(6, "Погрузочная бригада №1");

            session.persist(tractor);
            session.persist(loadingBrigade);
            System.out.println("✅ Создана техника: " + tractor.getModelName());
            System.out.println("✅ Создана бригада: " + loadingBrigade.getSpecialization() + " (" + loadingBrigade.getSize() + " чел.)");

            tractor.setAssignedFlight(flight);
            loadingBrigade.setAssignedFlight(flight);
            System.out.println("✅ Ресурсы назначены на рейс");

            noseCompartment.setCurrentWeight(500.0);
            flight.setTotalCargoWeight(500.0);
            System.out.println("✅ Вес добавлен в отсек: 500 кг");
            System.out.println("  Текущий вес отсека: " + noseCompartment.getCurrentWeight() + " кг");

            session.getTransaction().commit();
            System.out.println("\n✨ Все операции успешно выполнены!");

        } catch (Exception e) {
            System.err.println("❌ Ошибка: " + e.getMessage());
            e.printStackTrace();
        }
    }
}