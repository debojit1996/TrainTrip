package com.debo.traintrip.model;

import java.util.List;

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
public class BookingDetails {

    private String from;
    private String to;
    private String userEmail;
    private double pricePaid;
    private List<TrainBookingDetails> trainBookingDetails;
}
