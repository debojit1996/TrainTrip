package com.debo.traintrip.repository.impl;

import static com.debo.traintrip.constants.BookingConstants.RESOURCE_NOT_FOUND;
import static com.debo.traintrip.constants.BookingConstants.bookingIdToDetailsMap;
import static com.debo.traintrip.constants.BookingConstants.trainIdToDetailsMap;
import static com.debo.traintrip.constants.BookingConstants.userIdToBookingIdsMap;
import static com.debo.traintrip.constants.BookingConstants.sourceDestToPriceMap;

import java.util.ArrayList;

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



    @Override
    public void saveBooking(BookingDetails bookingDetails) {
        // In actual implementation, we would have saved this detail in the database
        bookingIdToDetailsMap.put(bookingDetails.getBookingId(), bookingDetails);
        // Also, update user-email to List of bookingId map. In Real world, this might be a separate table storing this info.
        userIdToBookingIdsMap.computeIfAbsent(bookingDetails.getUserEmail(), k -> new ArrayList<>()).add(bookingDetails.getBookingId());
    }

    @Override
    public TrainAndSeatDetails getTrainAndSeatDetails(String trainNumber) {
        // Fetch seat availability and price from the database(in real application).
        TrainAndSeatDetails trainAndSeatDetails = trainIdToDetailsMap.getOrDefault(trainNumber,
                new TrainAndSeatDetails());
        if (Objects.isNull(trainAndSeatDetails.getTrainNumber())) {
            throw new ResourceNotFoundException("Train with number " + trainNumber + " doesn't exist", RESOURCE_NOT_FOUND);
        }
        return trainAndSeatDetails;
    }

    @Override
    public int getPriceForTrain(String from, String to) {
        return sourceDestToPriceMap.get(from.concat("_").concat(to));
    }

    @Override
    public void updateTrainAndSeatDetails(String trainNumber, Map<BookingConstants.TrainSection, Integer> sectionWiseSeatCount) {
        trainIdToDetailsMap.put(trainNumber, TrainAndSeatDetails.builder()
                .trainNumber(trainNumber)
                .availableSeatCount(sectionWiseSeatCount)
                .sectionSeatDetailsMap(trainIdToDetailsMap.get(trainNumber).getSectionSeatDetailsMap())
                .build());
        log.info("Updated trainId to details map: {}", trainIdToDetailsMap);
    }

}
