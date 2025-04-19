package com.debo.traintrip;

import static com.debo.traintrip.constants.BookingConstants.BookingStatus.AVAILABLE;
import static com.debo.traintrip.constants.BookingConstants.trainIdToDetailsMap;
import static com.debo.traintrip.constants.BookingConstants.trainNumber;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.debo.traintrip.constants.BookingConstants;
import com.debo.traintrip.entity.TrainAndSeatDetails;
import com.debo.traintrip.model.SeatDetails;

@SpringBootApplication
public class TrainTripApplication implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(TrainTripApplication.class, args);
    }

    @Override
    public void run(String... args) {
        TrainAndSeatDetails trainAndSeatDetails = TrainAndSeatDetails.builder()
                .trainNumber(trainNumber)
                .availableSeatCount(new HashMap<>(Map.of(
                        BookingConstants.TrainSection.A, 30,
                        BookingConstants.TrainSection.B, 30)))
                .sectionSeatDetailsMap(constructSeatDetailsMap())
                .build();
        // This is a mock implementation. In a real application, this would interact with a database.
        trainIdToDetailsMap.put(trainNumber, trainAndSeatDetails);
    }

    private Map<BookingConstants.TrainSection, List<SeatDetails>> constructSeatDetailsMap() {
        Map<BookingConstants.TrainSection, List<SeatDetails>> sectionSeatDetailsMap = new HashMap<>();
        populateSeatDetails(BookingConstants.TrainSection.A, sectionSeatDetailsMap);
        populateSeatDetails(BookingConstants.TrainSection.B, sectionSeatDetailsMap);
        return sectionSeatDetailsMap;

    }

    private void populateSeatDetails(BookingConstants.TrainSection trainSection, Map<BookingConstants.TrainSection,
            List<SeatDetails>> sectionSeatDetailsMap) {
        // Assuming each section has 30 seats numbered from 1 to 30
        for (int i = 1; i <= 30; i++) {
            sectionSeatDetailsMap.computeIfAbsent(trainSection, k -> new ArrayList<>()).add(SeatDetails.builder()
                    .seatNumber(i)
                    .bookingStatus(AVAILABLE)
                    .build());
        }
    }
}
