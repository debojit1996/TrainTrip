package com.debo.traintrip.service.impl;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.debo.traintrip.constants.BookingConstants;
import com.debo.traintrip.entity.TrainAndSeatDetails;
import com.debo.traintrip.exception.ReservationFailedException;
import com.debo.traintrip.model.BookingDetails;
import com.debo.traintrip.model.BookingRequest;
import com.debo.traintrip.model.TrainBookingDetails;
import com.debo.traintrip.repository.BookingRepository;
import com.debo.traintrip.service.BookingService;
import com.debo.traintrip.validator.RequestValidator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final RequestValidator requestValidator;
    private final BookingRepository bookingRepository;

    @Override
    public BookingDetails purchaseTicket(BookingRequest bookingRequest) {
        // Validate the booking request and then call save and return booking details
        requestValidator.validateBookingRequest(bookingRequest);
        // Fetch seat availability and price from the database(in real application).
        TrainAndSeatDetails trainAndSeatDetails = bookingRepository.getTrainAndSeatDetails(bookingRequest.getTrainNumber());
        int price = bookingRepository.getPriceForTrain(bookingRequest.getFrom(), bookingRequest.getTo());

        // first we will check required number of seats are available or not
        int seatsInSecA = trainAndSeatDetails.getAvailableSeatCount().get(BookingConstants.TrainSection.A);
        int seatsInSecB = trainAndSeatDetails.getAvailableSeatCount().get(BookingConstants.TrainSection.B);
        BookingDetails bookingDetails = BookingDetails.builder()
                .from(bookingRequest.getFrom())
                .to(bookingRequest.getTo())
                .userEmail(bookingRequest.getUser().getEmail())
                .build();
        if (bookingRequest.getNumberOfSeats() <= (seatsInSecA + seatsInSecB)) {
            // set the total price for the response
            bookingDetails.setPricePaid(price * bookingRequest.getNumberOfSeats());
            // set the TrainDetails in the bookingDetails object
            updateTrainDetails(bookingDetails, bookingRequest, seatsInSecA, seatsInSecB);
        } else {
            // Not making it complicated by implementing logic for RAC and other statuses
            throw  new ReservationFailedException("Number of seats available is less than " + bookingRequest.getNumberOfSeats());
        }
        // save the booking details in the database/mock database and return response
        bookingRepository.saveBooking(bookingDetails);
        return bookingDetails;
    }

    private void updateTrainDetails(BookingDetails bookingDetails, BookingRequest bookingRequest, int seatsInSecA, int seatsInSecB) {
        TrainBookingDetails trainBookingDetails = TrainBookingDetails.builder()
                .trainNumber(bookingRequest.getTrainNumber())
                .build();
        int updatedSeatsInSecA = seatsInSecA;
        int updatedSeatsInSecB = seatsInSecB;

        // First will try to allocate all seats in a single section
        if (bookingRequest.getNumberOfSeats() <= seatsInSecA) {
            // Allocate all seats in section A
            trainBookingDetails.setSectionWiseSeatCount(Map.of(BookingConstants.TrainSection.A,
                    bookingRequest.getNumberOfSeats()));
            bookingDetails.setTrainBookingDetails(List.of(trainBookingDetails));
            updatedSeatsInSecA -= bookingRequest.getNumberOfSeats();
        } else if (bookingRequest.getNumberOfSeats() <= seatsInSecB) {
            // Allocate all seats in section B
            trainBookingDetails.setSectionWiseSeatCount(Map.of(BookingConstants.TrainSection.B,
                    bookingRequest.getNumberOfSeats()));
            bookingDetails.setTrainBookingDetails(List.of(trainBookingDetails));
            updatedSeatsInSecB -= bookingRequest.getNumberOfSeats();
        } else {
            /* Allocate seats in both sections. Firstly, allocate all available seats from section A and then allocate remaining
               seats from section B. */
            int seatsInB = bookingRequest.getNumberOfSeats() - seatsInSecA;

            trainBookingDetails.setSectionWiseSeatCount(Map.of(BookingConstants.TrainSection.A, seatsInSecA,
                    BookingConstants.TrainSection.B, seatsInB));
            bookingDetails.setTrainBookingDetails(List.of(trainBookingDetails));

            updatedSeatsInSecA = 0;
            updatedSeatsInSecB -= seatsInB;
        }
        log.info("Updated seats in section A: {} and section B: {} for train number: {}",
                updatedSeatsInSecA, updatedSeatsInSecB, bookingRequest.getTrainNumber());
        // Update the available seat count in the database/mock database
        bookingRepository.updateTrainAndSeatDetails(bookingRequest.getTrainNumber(),
                Map.of(BookingConstants.TrainSection.A, updatedSeatsInSecA,
                        BookingConstants.TrainSection.B, updatedSeatsInSecB));
    }
}
