package com.debo.traintrip.validator;

import java.util.Objects;

import org.springframework.stereotype.Component;

import com.debo.traintrip.exception.ReservationFailedException;
import com.debo.traintrip.model.BookingRequest;
import com.debo.traintrip.model.User;

/* For simplicity, I'm adding only null checks, but surely we can add more checks like:
   1. Email format validation
   2. All required fields should be non-empty
   3. Character length validation for string fields
   4. Date field related validations and more. */

@Component
public class RequestValidator {
    public void validateBookingRequest(BookingRequest bookingRequest) {

        if (Objects.isNull(bookingRequest)) {
            throw new ReservationFailedException("Booking request cannot be null");
        }
        // performs user related validations
        validateUserDetails(bookingRequest.getUser());

        if (Objects.isNull(bookingRequest.getTrainNumber())) {
            throw new ReservationFailedException("Train details cannot be null");
        }

        if (Objects.isNull(bookingRequest.getDateOfJourney()))  {
            throw new ReservationFailedException("Journey date cannot be null");
        }

        if (bookingRequest.getNumberOfSeats() <= 0) {
            throw new ReservationFailedException("Number of seats should be greater than zero");
        }
    }

    private void validateUserDetails(User user) {
        if (Objects.isNull(user)) {
            throw new ReservationFailedException("User details cannot be null");
        }

        if (Objects.isNull(user.getFirstName()) || user.getFirstName().isEmpty()) {
            throw new ReservationFailedException("User name cannot be null or empty");
        }

        if (Objects.isNull(user.getEmail()) || user.getEmail().isEmpty()) {
            throw new ReservationFailedException("User email cannot be null or empty");
        }
    }
}
