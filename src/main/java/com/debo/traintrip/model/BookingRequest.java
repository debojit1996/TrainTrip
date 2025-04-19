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
public class BookingRequest {

    private String from;
    private String to;
    private String loggedInUsername;
    private List<Passenger> passengers;
    private String trainNumber;
    private String dateOfJourney; // declared it as a string for simplicity now
    private int numberOfSeats;
}
