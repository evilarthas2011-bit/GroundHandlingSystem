package com.kuleshov.groundhandling;

import com.kuleshov.groundhandling.domain.Aircraft;
import com.kuleshov.groundhandling.domain.AircraftType;
import com.kuleshov.groundhandling.domain.Brigade;
import com.kuleshov.groundhandling.domain.Equipment;
import com.kuleshov.groundhandling.domain.Flight;
import com.kuleshov.groundhandling.domain.FlightStatus;
import org.hibernate.Session;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public final class DemoDataInitializer {

    private DemoDataInitializer() {
    }

    private record FlightSpec(String number, AircraftType type, String tail,
                              int minutesOffset, FlightStatus status, double paxWeight) {
    }


    private static final FlightSpec[] FLIGHT_SPECS = {
            new FlightSpec("SU-1101", AircraftType.BOEING_737, "RA-73101", -90,  FlightStatus.ARRIVED,   5_400),
            new FlightSpec("SU-1102", AircraftType.AIRBUS_A320, "RA-74001", -30,  FlightStatus.LOADING,   5_000),
            new FlightSpec("SU-1103", AircraftType.BOEING_737, "RA-73102", -15,  FlightStatus.LOADING,   5_200),
            new FlightSpec("SU-1104", AircraftType.AIRBUS_A320, "RA-74002",  30,  FlightStatus.BOARDING,  4_800),
            new FlightSpec("SU-1105", AircraftType.BOEING_737, "RA-73103",  60,  FlightStatus.SCHEDULED, 0),
            new FlightSpec("SU-1106", AircraftType.AIRBUS_A320, "RA-74003", 120,  FlightStatus.SCHEDULED, 0),
            new FlightSpec("SU-1107", AircraftType.BOEING_737, "RA-73104", -40,  FlightStatus.DELAYED,   3_800),
            new FlightSpec("SU-1108", AircraftType.AIRBUS_A320, "RA-74004", 180,  FlightStatus.READY,     4_600),
            new FlightSpec("SU-1109", AircraftType.BOEING_737, "RA-73105", -180, FlightStatus.DEPARTED,  5_900),
            new FlightSpec("SU-1110", AircraftType.AIRBUS_A320, "RA-74005", 240,  FlightStatus.SCHEDULED, 0)
    };

    public static void initialize() {
        HibernateUtil.inTransaction(session -> {
            Long flights = session.createQuery(
                    "SELECT COUNT(f) FROM Flight f", Long.class).uniqueResult();
            if (flights != null && flights > 0) {
                return null; // база уже заполнена
            }

            List<Flight> created = createFlights(session);
            createEquipment(session, created);
            createBrigades(session, created);
            return null;
        });
    }

    private static List<Flight> createFlights(Session session) {
        List<Flight> result = new ArrayList<>();
        for (FlightSpec spec : FLIGHT_SPECS) {
            Aircraft aircraft = Aircraft.createFor(spec.type(), spec.tail());
            session.persist(aircraft);

            Flight flight = new Flight(spec.number(),
                    LocalDateTime.now().plusMinutes(spec.minutesOffset()), aircraft);
            flight.setStatus(spec.status());
            if (spec.paxWeight() > 0) {
                flight.setPassengerWeight(spec.paxWeight());
            }
            session.persist(flight);
            result.add(flight);
        }
        return result;
    }

    private static Equipment equipment(String name, String model, double capacity) {
        Equipment equipment = new Equipment();
        equipment.setName(name);
        equipment.setModelName(model);
        equipment.setCapacity(capacity);
        return equipment;
    }

    private static void createEquipment(Session session, List<Flight> flights) {
        Equipment t1 = equipment("Тягач №1", "TLD TPX-100", 40.0);
        Equipment t2 = equipment("Тягач №2", "Goldhofer AST-1", 35.0);
        Equipment l1 = equipment("Багажная лента №1", "TLD TXL-828", 4.5);
        Equipment l2 = equipment("Багажная лента №2", "AviTeam BL-120", 3.0);
        Equipment s1 = equipment("Трап №1", "SKYMETAL TM-210", 0.0);
        Equipment s2 = equipment("Ступеньки №1", "SNK-2", 0.0);
        Equipment d1 = equipment("Деайсер №1", "Artsway DA-500", 12.0);
        Equipment g1 = equipment("Заправщик №1", "GPU-90", 0.0);
        Equipment c1 = equipment("Грузовая тележка №1", "TCV-40", 2.5);
        Equipment c2 = equipment("Багажная тележка №1", "AVT-20", 1.5);


        t1.setAssignedFlight(flights.get(1));
        l1.setAssignedFlight(flights.get(1));
        l2.setAssignedFlight(flights.get(2));

        for (Equipment e : new Equipment[]{t1, t2, l1, l2, s1, s2, d1, g1, c1, c2}) {
            session.persist(e);
        }
    }

    private static Brigade brigade(String name, String specialization, int size) {
        Brigade brigade = new Brigade();
        brigade.setName(name);
        brigade.setSpecialization(specialization);
        brigade.setSize(size);
        return brigade;
    }

    private static void createBrigades(Session session, List<Flight> flights) {
        Brigade b1 = brigade("Бригада №1", "Грузовая", 5);
        Brigade b2 = brigade("Бригада №2", "Грузовая", 4);
        Brigade b3 = brigade("Бригада №3", "Грузовая", 6);
        Brigade b4 = brigade("Бригада №4", "Багажная", 3);
        Brigade b5 = brigade("Бригада №5", "Багажная", 4);
        Brigade b6 = brigade("Бригада №6", "Багажная", 3);
        Brigade b7 = brigade("Бригада №7", "Заправка", 4);
        Brigade b8 = brigade("Бригада №8", "Уборка ВС", 5);
        Brigade b9 = brigade("Бригада №9", "Багажная", 3);
        Brigade b10 = brigade("Бригада №10", "Грузовая", 4);

        b1.setAssignedFlight(flights.get(1));
        b2.setAssignedFlight(flights.get(2));
        b4.setAssignedFlight(flights.get(1));

        for (Brigade b : new Brigade[]{b1, b2, b3, b4, b5, b6, b7, b8, b9, b10}) {
            session.persist(b);
        }
    }
}
