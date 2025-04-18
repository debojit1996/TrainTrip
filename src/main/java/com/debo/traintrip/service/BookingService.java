package com.debo.traintrip.service;

import org.springframework.stereotype.Service;

import com.debo.traintrip.model.BookingDetails;
import com.debo.traintrip.model.BookingRequest;


public interface BookingService {
    BookingDetails purchaseTicket(BookingRequest bookingDetails);
}
