package com.kuleshov.groundhandling;

import com.kuleshov.groundhandling.domain.Aircraft;
import com.kuleshov.groundhandling.domain.Flight;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class CSVExporter {

    private static final String SEPARATOR = ";";

    private CSVExporter() {
    }

    public static void exportFlights(List<Flight> flights, String filePath) throws IOException {
        try (PrintWriter writer = new PrintWriter(filePath, StandardCharsets.UTF_8)) {
            writer.print('\ufeff');
            writer.println(String.join(SEPARATOR,
                    "Номер рейса", "Тип ВС", "Борт", "Статус",
                    "Дата вылета", "Груз (кг)", "Пассажиры (кг)", "Итого (кг)"));
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

            for (Flight flight : flights) {
                Aircraft aircraft = flight.getAircraft();
                double cargoWeight = flight.getTotalCargoWeight();
                double passengerWeight = flight.getPassengerWeight();


                writer.println(String.join(SEPARATOR,
                        flight.getFlightNumber(),
                        aircraft.getAircraftType().getDisplayName(),
                        aircraft.getTailNumber(),
                        flight.getStatus().name(),
                        flight.getDepartureDateTime().format(fmt),
                        format(cargoWeight),
                        format(passengerWeight),
                        format(cargoWeight + passengerWeight)));
            }
        }
    }

    private static String format(double value) {
        return String.format("%.1f", value).replace('.', ',');
    }
}
