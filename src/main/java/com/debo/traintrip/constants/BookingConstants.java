package com.debo.traintrip.constants;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.debo.traintrip.entity.TrainAndSeatDetails;
import com.debo.traintrip.model.BookingDetails;

public class BookingConstants {
    // Enum for booking status
    public enum BookingStatus {
        AVAILABLE,
        CONFIRMED
    }

    // Enum TrainSection with two sections A and B
    public enum TrainSection {
        A,
        B
    }

    public static Map<String, List<String>> userIdToBookingIdsMap = new HashMap<>();
    public static Map<String, Integer> sourceDestToPriceMap = Map.of("London_France", 20);

    public static Map<String, TrainAndSeatDetails> trainIdToDetailsMap = new HashMap<>();
    public static Map<String, BookingDetails> bookingIdToDetailsMap = new HashMap<>();

    // Other constants
    public static final String trainNumber = "LON_FRA_13456";
    // Error Types
    public static final String RESOURCE_NOT_FOUND = "RESOURCE_NOT_FOUND";
    public static final String REQUIRED_FIELDS_MISSING = "REQUIRED_FIELDS_MISSING";
    public static final String INVALID_FIELD_VALUE = "INVALID_VALUE";

}
