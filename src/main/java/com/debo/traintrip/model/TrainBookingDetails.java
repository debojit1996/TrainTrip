package com.debo.traintrip.model;

import java.util.List;
import java.util.Map;

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
public class TrainBookingDetails {
    private Map<BookingConstants.TrainSection, Integer> sectionWiseSeatCount;
    private Map<BookingConstants.TrainSection, List<PassengerSeatDetails>> sectionWisePassengerSeatDetails;
}
