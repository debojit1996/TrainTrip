package com.debo.traintrip.model;

import com.debo.traintrip.constants.BookingConstants;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PassengerSeatDetails {
    private String name;
    private int age;
    private String gender;
    private int seatNumber;
    private BookingConstants.BookingStatus bookingStatus;
}
