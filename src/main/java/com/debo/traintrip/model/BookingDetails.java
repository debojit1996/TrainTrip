package com.debo.traintrip.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

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
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class BookingDetails {
    private String bookingId;
    private String trainNumber;
    private String from;
    private String to;
    private String userEmail;
    private int numberOfSeatsBooked;
    private int pricePaid;
    private TrainBookingDetails trainBookingDetails;
    private String dateOfJourney;
}
