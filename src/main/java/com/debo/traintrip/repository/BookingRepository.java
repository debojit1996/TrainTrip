package com.debo.traintrip.repository;

import java.util.List;
import java.util.Map;


import com.debo.traintrip.constants.BookingConstants;
import com.debo.traintrip.entity.TrainAndSeatDetails;
import com.debo.traintrip.model.BookingDetails;
import com.debo.traintrip.model.PassengerSeatDetails;
import com.debo.traintrip.model.TrainBookingDetails;


public interface BookingRepository {
    void saveBooking(BookingDetails bookingDetails);

    TrainAndSeatDetails getTrainAndSeatDetails(String trainNumber);

    int getPriceForTrain(String from, String to);

    void updateTrainAndSeatDetails(String trainNumber, Map<BookingConstants.TrainSection, Integer> sectionWiseSeatCountMap);

    void deleteBooking(String bookingId);

    void updateBookingStatus(String trainNumber, Map<BookingConstants.TrainSection, List<Integer>> secToBookedSeatNumbers, BookingConstants.BookingStatus currentBookingStatus,
                             BookingConstants.BookingStatus toBeBookingStatus);

    void updateSeatCountDetails(String trainNumber, int seatsBookedInSecA, int seatsBookedInSecB);

    List<String> getAllBookingIds();

    BookingDetails getBookingDetailsById(String bookingId);

    List<PassengerSeatDetails> fetchPassengerDetailsBySection(String trainNumber, BookingConstants.TrainSection trainSection);
}
