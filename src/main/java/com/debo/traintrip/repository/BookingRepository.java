package com.debo.traintrip.repository;

import java.util.Map;


import com.debo.traintrip.constants.BookingConstants;
import com.debo.traintrip.entity.TrainAndSeatDetails;
import com.debo.traintrip.model.BookingDetails;


public interface BookingRepository {
    void saveBooking(BookingDetails bookingDetails);

    TrainAndSeatDetails getTrainAndSeatDetails(String trainNumber);

    int getPriceForTrain(String from, String to);

    void updateTrainAndSeatDetails(String trainNumber, Map<BookingConstants.TrainSection, Integer> a);
}
