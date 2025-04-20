package com.debo.traintrip.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.debo.traintrip.model.BookingDetails;
import com.debo.traintrip.model.BookingRequest;
import com.debo.traintrip.model.BookingUpdateRequest;
import com.debo.traintrip.model.PassengerSeatDetails;


public interface BookingService {
    BookingDetails purchaseTicket(BookingRequest bookingDetails);

    BookingDetails getByBookingId(String bookingId);

    List<String> getAllBookingIds();

    List<PassengerSeatDetails> getPassengerDetailsBySection(String trainNumber, String section);

    void cancelBooking(String bookingId);

    BookingDetails modifyPassengerSeat(String bookingId, BookingUpdateRequest bookingUpdateRequest);
}
