package com.debo.traintrip.entity;

import java.util.List;
import java.util.Map;

import com.debo.traintrip.constants.BookingConstants;
import com.debo.traintrip.model.SeatDetails;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

// This detail will actually be stored in the database in a real application.
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class TrainAndSeatDetails {
    private String trainNumber;
    private Map<BookingConstants.TrainSection, Integer> availableSeatCount;
    private Map<BookingConstants.TrainSection, List<SeatDetails>> sectionSeatDetailsMap;
}
