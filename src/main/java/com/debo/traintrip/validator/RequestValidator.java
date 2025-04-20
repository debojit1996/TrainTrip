package com.debo.traintrip.validator;

import static com.debo.traintrip.constants.BookingConstants.INVALID_FIELD_VALUE;
import static com.debo.traintrip.constants.BookingConstants.REQUIRED_FIELDS_MISSING;
import static com.debo.traintrip.constants.BookingConstants.TrainSection.A;
import static com.debo.traintrip.constants.BookingConstants.TrainSection.B;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.debo.traintrip.constants.BookingConstants;
import com.debo.traintrip.exception.ReservationFailedException;
import com.debo.traintrip.model.BookingDetails;
import com.debo.traintrip.model.BookingRequest;
import com.debo.traintrip.model.BookingUpdateRequest;
import com.debo.traintrip.model.Passenger;
import com.debo.traintrip.model.PassengerSeatDetails;

/* For simplicity, I'm adding only null checks, but surely we can add more checks like:
   1. Email format validation
   2. All required fields should be non-empty
   3. Character length validation for string fields
   4. Date field related validations and more. */
@Component
public class RequestValidator {
    public void validateBookingRequest(BookingRequest bookingRequest) {

        if (Objects.isNull(bookingRequest)) {
            throw new ReservationFailedException("Booking request cannot be null", REQUIRED_FIELDS_MISSING);
        }
        // performs user related validations
        if (Objects.isNull(bookingRequest.getLoggedInUsername()) || bookingRequest.getLoggedInUsername().isEmpty()) {
            throw new ReservationFailedException("LoggedInUser details cannot be null or empty", REQUIRED_FIELDS_MISSING);
        }

        if (bookingRequest.getNumberOfSeats() <= 0) {
            throw new ReservationFailedException("Number of seats should be greater than zero", INVALID_FIELD_VALUE);
        }

        if (Objects.isNull(bookingRequest.getPassengers()) || bookingRequest.getPassengers().isEmpty()) {
            throw new ReservationFailedException("Passenger details cannot be null or empty", REQUIRED_FIELDS_MISSING);
        }

        if (bookingRequest.getPassengers().size() != bookingRequest.getNumberOfSeats()) {
            throw new ReservationFailedException("Number of passengers and number of seats should be equal", INVALID_FIELD_VALUE);
        }

        bookingRequest.getPassengers().forEach(this::validateUserDetails);

        if (Objects.isNull(bookingRequest.getTrainNumber())) {
            throw new ReservationFailedException("Train details cannot be null", REQUIRED_FIELDS_MISSING);
        }

        if (Objects.isNull(bookingRequest.getDateOfJourney()))  {
            throw new ReservationFailedException("Journey date cannot be null", REQUIRED_FIELDS_MISSING);
        }
    }

    private void validateUserDetails(Passenger passenger) {
        if (Objects.isNull(passenger)) {
            throw new ReservationFailedException("Passenger details cannot be null", REQUIRED_FIELDS_MISSING);
        }

        if (Objects.isNull(passenger.getFirstName()) || passenger.getFirstName().isEmpty()) {
            throw new ReservationFailedException("Passenger name cannot be null or empty", REQUIRED_FIELDS_MISSING);
        }

        if (Objects.isNull(passenger.getEmail()) || passenger.getEmail().isEmpty()) {
            throw new ReservationFailedException("Passenger email cannot be null or empty", REQUIRED_FIELDS_MISSING);
        }

        if (Objects.isNull(passenger.getGender()) || passenger.getGender().isEmpty()) {
            throw new ReservationFailedException("Passenger gender cannot be null or empty", REQUIRED_FIELDS_MISSING);
        }

        if (passenger.getAge() <= 0) {
            throw new ReservationFailedException("Passenger age should be greater than zero", INVALID_FIELD_VALUE);
        }
    }

    public void validateBookingUpdateRequest(BookingUpdateRequest bookingUpdateRequest, BookingDetails bookingDetails) {
        if (Objects.isNull(bookingUpdateRequest)) {
            throw new ReservationFailedException("Booking update request cannot be null", REQUIRED_FIELDS_MISSING);
        }

        if (bookingUpdateRequest.getSeatNumber() <= 0) {
            throw new ReservationFailedException("Seat number should be greater than zero", INVALID_FIELD_VALUE);
        }

        if (Objects.isNull(bookingUpdateRequest.getSection()) || bookingUpdateRequest.getSection().isEmpty()) {
            throw new ReservationFailedException("Train Section cannot be null or empty", REQUIRED_FIELDS_MISSING);
        }

        if (!A.name().equalsIgnoreCase(bookingUpdateRequest.getSection()) && !B.name().equalsIgnoreCase(bookingUpdateRequest.getSection())) {
            throw new ReservationFailedException("Train Section is invalid. Please provide one among A or B", INVALID_FIELD_VALUE);
        }

        // Validate for the given bookingId and section, the provided seat number is CONFIRMED
        Set<Integer> bookedSeatNumbersForGivenSection = bookingDetails.getTrainBookingDetails().getSectionWisePassengerSeatDetails()
                .get(BookingConstants.TrainSection.valueOf(bookingUpdateRequest.getSection())).stream()
                .map(PassengerSeatDetails::getSeatNumber)
                .collect(Collectors.toSet());

        if (!bookedSeatNumbersForGivenSection.contains(bookingUpdateRequest.getSeatNumber())) {
            throw new ReservationFailedException("For the given bookingId and section, the given seat number is not already booked",
                    INVALID_FIELD_VALUE);
        }
    }
}
