package com.debo.traintrip.repository.impl;

import static com.debo.traintrip.constants.BookingConstants.RESOURCE_NOT_FOUND;
import static com.debo.traintrip.constants.BookingConstants.bookingIdToDetailsMap;
import static com.debo.traintrip.constants.BookingConstants.trainIdToDetailsMap;
import static com.debo.traintrip.constants.BookingConstants.userIdToBookingIdsMap;
import static com.debo.traintrip.constants.BookingConstants.sourceDestToPriceMap;

import java.util.ArrayList;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.stereotype.Repository;

import com.debo.traintrip.constants.BookingConstants;
import com.debo.traintrip.exception.ResourceNotFoundException;
import com.debo.traintrip.model.BookingDetails;
import com.debo.traintrip.entity.TrainAndSeatDetails;
import com.debo.traintrip.model.PassengerSeatDetails;
import com.debo.traintrip.model.SeatDetails;
import com.debo.traintrip.model.TrainBookingDetails;
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
        return trainIdToDetailsMap.get(trainNumber);
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

    @Override
    public void deleteBooking(String bookingId) {
        BookingDetails bookingDetails = bookingIdToDetailsMap.get(bookingId);
        /* For simplicity, I'm removing the entry from the map but in actual application, we might just soft delete the entry(by updating
        certain columns like deleted_date, deleted_by) to keep it for audit purposes. */
        bookingIdToDetailsMap.remove(bookingId);
        userIdToBookingIdsMap.get(bookingDetails.getUserEmail()).remove(bookingId);
        log.info("Deleted booking with id: {}", bookingId);
    }

    @Override
    public void updateBookingStatus(String trainNumber, Map<BookingConstants.TrainSection, List<Integer>> secToBookedSeatNumbers,
                                    BookingConstants.BookingStatus currentBookingStatus,
                                    BookingConstants.BookingStatus toBeBookingStatus) {
        Map<BookingConstants.TrainSection, List<SeatDetails>> sectionSeatDetailsMap = trainIdToDetailsMap.get(trainNumber)
                .getSectionSeatDetailsMap();
        for (Map.Entry<BookingConstants.TrainSection, List<Integer>> entry : secToBookedSeatNumbers.entrySet()) {
            BookingConstants.TrainSection trainSection = entry.getKey();
            List<Integer> bookedSeatNumbers = entry.getValue();
            List<SeatDetails> seatDetailsList = sectionSeatDetailsMap.get(trainSection);
            for (int seatNumber : bookedSeatNumbers) {
                seatDetailsList.get(seatNumber - 1).setBookingStatus(toBeBookingStatus);
            }
        }
        log.info("Updated booking status for train number {}: {}", trainNumber, trainIdToDetailsMap);
    }

    @Override
    public void updateSeatCountDetails(String trainNumber, int seatsBookedInSecA, int seatsBookedInSecB) {
        TrainAndSeatDetails trainAndSeatDetails = trainIdToDetailsMap.get(trainNumber);
        Map<BookingConstants.TrainSection, Integer> availableSeatCount = trainAndSeatDetails.getAvailableSeatCount();
        availableSeatCount.put(BookingConstants.TrainSection.A, availableSeatCount.get(BookingConstants.TrainSection.A) + seatsBookedInSecA);
        availableSeatCount.put(BookingConstants.TrainSection.B, availableSeatCount.get(BookingConstants.TrainSection.B) + seatsBookedInSecB);
        trainAndSeatDetails.setAvailableSeatCount(availableSeatCount);
        trainIdToDetailsMap.put(trainNumber, trainAndSeatDetails);
        log.info("Updated seat count for train number {}: {}", trainNumber, trainIdToDetailsMap);
    }

    @Override
    public List<String> getAllBookingIds() {
        return new ArrayList<>(bookingIdToDetailsMap.keySet().stream().toList());
    }

    @Override
    public BookingDetails getBookingDetailsById(String bookingId) {
        return bookingIdToDetailsMap.get(bookingId);
    }

    @Override
    public List<PassengerSeatDetails> fetchPassengerDetailsBySection(String trainNumber, BookingConstants.TrainSection trainSection) {
        return bookingIdToDetailsMap.values().stream()
                .filter(bookingDetails -> bookingDetails.getTrainNumber().equalsIgnoreCase(trainNumber))
                .flatMap(bookingDetails -> bookingDetails.getTrainBookingDetails().getSectionWisePassengerSeatDetails()
                        .get(trainSection).stream())
                .toList();
    }

}
