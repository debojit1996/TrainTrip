package com.debo.traintrip.repository.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.stereotype.Repository;

import com.debo.traintrip.constants.BookingConstants;
import com.debo.traintrip.exception.ResourceNotFoundException;
import com.debo.traintrip.model.BookingDetails;
import com.debo.traintrip.entity.TrainAndSeatDetails;
import com.debo.traintrip.repository.BookingRepository;

import lombok.extern.slf4j.Slf4j;

// This is a mock implementation. In a real application, this would interact with a database.
@Repository
@Slf4j
public class BookingRepositoryImpl implements BookingRepository {

    private final Map<String, List<BookingDetails>> userIdToBookingDetailsMap = new HashMap<>();
    private final Map<String, Integer> sourceDestToPriceMap = Map.of("London_France", 20);

    private Map<String, TrainAndSeatDetails> trainIdToDeatilsMap = new HashMap<>(Map.of("LON_FRA_13456",
            new TrainAndSeatDetails("LON_FRA_13456",
                    new HashMap<>(Map.of(BookingConstants.TrainSection.A, 30,
                            BookingConstants.TrainSection.B, 30)))));

    @Override
    public void saveBooking(BookingDetails bookingDetails) {
        // In actual implementation, we would have saved this detail in the database
        userIdToBookingDetailsMap.computeIfAbsent(bookingDetails.getUserEmail(), k -> new ArrayList<>()).add(bookingDetails);
    }

    @Override
    public TrainAndSeatDetails getTrainAndSeatDetails(String trainNumber) {
        // Fetch seat availability and price from the database(in real application).
        TrainAndSeatDetails trainAndSeatDetails = trainIdToDeatilsMap.getOrDefault(trainNumber,
                new TrainAndSeatDetails());
        if (Objects.isNull(trainAndSeatDetails.getTrainNumber())) {
            throw new ResourceNotFoundException("Train with number " + trainNumber + " doesn't exist");
        }
        return trainAndSeatDetails;
    }

    @Override
    public int getPriceForTrain(String from, String to) {
        return sourceDestToPriceMap.get(from.concat("_").concat(to));
    }

    @Override
    public void updateTrainAndSeatDetails(String trainNumber, Map<BookingConstants.TrainSection, Integer> sectionWiseSeatCount) {
        trainIdToDeatilsMap.put(trainNumber, TrainAndSeatDetails.builder()
                .trainNumber(trainNumber)
                .availableSeatCount(sectionWiseSeatCount)
                .build());
        log.info("Updated trainId to details map: {}", trainIdToDeatilsMap);
    }

}
