package com.debo.traintrip.controller;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.debo.traintrip.model.BookingDetails;
import com.debo.traintrip.model.BookingRequest;
import com.debo.traintrip.model.BookingUpdateRequest;
import com.debo.traintrip.model.PassengerSeatDetails;
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

    // This can just be an internal API to get all booking IDs. In a real application, this should be accessible to developers/admins only
    @GetMapping("/all-booking-ids")
    public List<String> getAllBookingIds() {
        return bookingService.getAllBookingIds();
    }

    @GetMapping("/details")
    public BookingDetails getByBookingId(@RequestParam(defaultValue = "") String bookingId) {
        return bookingService.getByBookingId(bookingId);
    }

    @GetMapping("/passenger-details")
    public List<PassengerSeatDetails> getPassengerDetailsBySection(@RequestParam(defaultValue = "") String trainNumber,
                                                                   @RequestParam(defaultValue = "") String section) {
        return bookingService.getPassengerDetailsBySection(trainNumber, section);
    }

    @PutMapping("/{bookingId}/modify")
    public BookingDetails modifyPassengerSeat(@PathVariable String bookingId, @RequestBody BookingUpdateRequest bookingUpdateRequest) {
        return bookingService.modifyPassengerSeat(bookingId, bookingUpdateRequest);
    }

    @DeleteMapping("/cancel/{bookingId}")
    public void cancelBooking(@PathVariable String bookingId) {
        bookingService.cancelBooking(bookingId);
    }
}
