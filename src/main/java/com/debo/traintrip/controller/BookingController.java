package com.debo.traintrip.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.debo.traintrip.model.BookingDetails;
import com.debo.traintrip.model.BookingRequest;
import com.debo.traintrip.service.BookingService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/bookings")
public class BookingController {

    private final BookingService bookingService;

    @PostMapping("/purchase")
    public BookingDetails purchaseTicket(@RequestBody BookingRequest bookingRequest) {
        return bookingService.purchaseTicket(bookingRequest);
    }
}
