package com.debo.traintrip.service.impl;

import static com.debo.traintrip.constants.BookingConstants.BookingStatus.AVAILABLE;
import static com.debo.traintrip.constants.BookingConstants.BookingStatus.CONFIRMED;
import static com.debo.traintrip.constants.BookingConstants.INVALID_FIELD_VALUE;
import static com.debo.traintrip.constants.BookingConstants.REQUIRED_FIELDS_MISSING;
import static com.debo.traintrip.constants.BookingConstants.RESOURCE_NOT_FOUND;
import static com.debo.traintrip.constants.BookingConstants.TRAIN_RESERVATION_FULL;
import static com.debo.traintrip.constants.BookingConstants.TrainSection.A;
import static com.debo.traintrip.constants.BookingConstants.TrainSection.B;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.debo.traintrip.constants.BookingConstants;
import com.debo.traintrip.entity.TrainAndSeatDetails;
import com.debo.traintrip.exception.InvalidValueException;
import com.debo.traintrip.exception.ReservationFailedException;
import com.debo.traintrip.exception.ResourceNotFoundException;
import com.debo.traintrip.model.BookingDetails;
import com.debo.traintrip.model.BookingRequest;
import com.debo.traintrip.model.BookingUpdateRequest;
import com.debo.traintrip.model.Passenger;
import com.debo.traintrip.model.PassengerSeatDetails;
import com.debo.traintrip.model.SeatDetails;
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
        if (Objects.isNull(trainAndSeatDetails)) {
            throw new ReservationFailedException("Train number " + bookingRequest.getTrainNumber() + " not found", RESOURCE_NOT_FOUND);
        }
        int price = bookingRepository.getPriceForTrain(bookingRequest.getFrom(), bookingRequest.getTo());

        // first we will check required number of seats are available or not
        int seatsInSecA = trainAndSeatDetails.getAvailableSeatCount().get(A);
        int seatsInSecB = trainAndSeatDetails.getAvailableSeatCount().get(B);
        BookingDetails bookingDetails = BookingDetails.builder()
                .trainNumber(bookingRequest.getTrainNumber())
                .from(bookingRequest.getFrom())
                .to(bookingRequest.getTo())
                .userEmail(bookingRequest.getLoggedInUsername())
                .dateOfJourney(bookingRequest.getDateOfJourney())
                .build();
        if (bookingRequest.getNumberOfSeats() <= (seatsInSecA + seatsInSecB)) {
            // set the total price for the response
            bookingDetails.setPricePaid(price * bookingRequest.getNumberOfSeats());
            bookingDetails.setNumberOfSeatsBooked(bookingRequest.getNumberOfSeats());
            // generate booking Id
            generateBookingId(bookingDetails, bookingRequest.getTrainNumber());
            // set the TrainDetails in the bookingDetails object
            updateTrainDetails(bookingDetails, bookingRequest, seatsInSecA, seatsInSecB, trainAndSeatDetails);
        } else {
            // Not making it complicated by implementing logic for RAC and other statuses
            throw new ReservationFailedException("Number of seats available is less than " + bookingRequest.getNumberOfSeats(),
                    TRAIN_RESERVATION_FULL);
        }
        // save the booking details in the database/mock database and return response
        bookingRepository.saveBooking(bookingDetails);
        return bookingDetails;
    }

    @Override
    public BookingDetails getByBookingId(String bookingId) {
        if (!StringUtils.hasText(bookingId)) {
            throw new InvalidValueException("Booking ID cannot be null or empty" , REQUIRED_FIELDS_MISSING);
        }
        // Fetch booking details from the database/mock database
        BookingDetails bookingDetails = bookingRepository.getBookingDetailsById(bookingId);
        if (Objects.isNull(bookingDetails)) {
            throw new ResourceNotFoundException("Booking ID " + bookingId + " not found", RESOURCE_NOT_FOUND);
        }
        return bookingDetails;
    }

    @Override
    public List<String> getAllBookingIds() {
        return bookingRepository.getAllBookingIds();
    }

    @Override
    public List<PassengerSeatDetails> getPassengerDetailsBySection(String trainNumber, String section) {
        if (!StringUtils.hasText(section) || !StringUtils.hasText(trainNumber)) {
            throw new InvalidValueException("Section and TrainNumber cannot be null or empty" , REQUIRED_FIELDS_MISSING);
        }

        TrainAndSeatDetails trainAndSeatDetails = bookingRepository.getTrainAndSeatDetails(trainNumber);
        if (Objects.isNull(trainAndSeatDetails)) {
            throw new ResourceNotFoundException("Train number " + trainNumber + " not found", RESOURCE_NOT_FOUND);
        }

        if (A.name().equalsIgnoreCase(section) || B.name().equalsIgnoreCase(section)) {
            BookingConstants.TrainSection trainSection = BookingConstants.TrainSection.valueOf(section.toUpperCase());
            log.debug("Fetching passenger details for section: {}", trainSection);
            return bookingRepository.fetchPassengerDetailsBySection(trainNumber, trainSection);
        }

        throw new InvalidValueException("Train Section " + section + " not found. Please insert section A or B", INVALID_FIELD_VALUE);
    }

    @Override
    public void cancelBooking(String bookingId) {
        if (!StringUtils.hasText(bookingId)) {
            throw new InvalidValueException("Booking ID cannot be null or empty" , REQUIRED_FIELDS_MISSING);
        }

        // Fetch booking details from the database/mock database
        BookingDetails bookingDetails = bookingRepository.getBookingDetailsById(bookingId);

        if (Objects.nonNull(bookingDetails)) {
            bookingRepository.deleteBooking(bookingId);
            // update the train and seat details in the database/mock database
            updateSeatAvailability(bookingDetails);
            log.info("Booking ID {} cancelled successfully", bookingId);
            return;
        }

        throw new ResourceNotFoundException("Booking ID " + bookingId + " not found", RESOURCE_NOT_FOUND);

    }

    @Override
    public BookingDetails modifyPassengerSeat(String bookingId, BookingUpdateRequest bookingUpdateRequest) {
        if (!StringUtils.hasText(bookingId)) {
            throw new InvalidValueException("Booking ID cannot be null or empty" , REQUIRED_FIELDS_MISSING);
        }

        // Fetch booking details from the database/mock database
        BookingDetails bookingDetails = bookingRepository.getBookingDetailsById(bookingId);

        if (Objects.isNull(bookingDetails)) {
            throw new ResourceNotFoundException("Booking ID " + bookingId + " not found", RESOURCE_NOT_FOUND);
        }
        // Validate the booking update request
        requestValidator.validateBookingUpdateRequest(bookingUpdateRequest, bookingDetails);

        updatePassengerSeat(bookingDetails, bookingUpdateRequest);

        return bookingDetails;
    }

    private void updatePassengerSeat(BookingDetails bookingDetails, BookingUpdateRequest bookingUpdateRequest) {
        // Fetch the train and seat details from the database/mock database
        TrainAndSeatDetails trainAndSeatDetails = bookingRepository.getTrainAndSeatDetails(bookingDetails.getTrainNumber());
        int seatsAvailableInSecA = trainAndSeatDetails.getAvailableSeatCount().get(A);
        int seatsAvailableInSecB = trainAndSeatDetails.getAvailableSeatCount().get(B);

        if (seatsAvailableInSecA + seatsAvailableInSecB == 0) {
            throw new ReservationFailedException("No seats available for modification as all seats are reserved", TRAIN_RESERVATION_FULL);
        }

        BookingConstants.TrainSection trainSection = BookingConstants.TrainSection.valueOf(bookingUpdateRequest.getSection().toUpperCase());
        List<PassengerSeatDetails> passengerSeatDetailsList = bookingDetails.getTrainBookingDetails()
                .getSectionWisePassengerSeatDetails().get(trainSection);
        /* If in current section, there are seats available, then we assign the seat in the same section. Otherwise,
           we assign the seat in the other section. */
        BookingConstants.TrainSection updatedSection = getUpdatedTrainSection(trainSection, seatsAvailableInSecA, seatsAvailableInSecB);

        for (PassengerSeatDetails passengerSeatDetails : passengerSeatDetailsList) {
            if (passengerSeatDetails.getSeatNumber() == bookingUpdateRequest.getSeatNumber()) {
                assignNewSeat(trainAndSeatDetails, bookingDetails, passengerSeatDetails, trainSection, updatedSection, bookingUpdateRequest.getSeatNumber());
                break;
            }
        }
        log.info("Updated passenger seat details for booking ID: {}", bookingDetails.getBookingId());
        log.info("Updated train and seat details: {}", trainAndSeatDetails);
    }

    private void assignNewSeat(TrainAndSeatDetails trainAndSeatDetails, BookingDetails bookingDetails,
                               PassengerSeatDetails passengerSeatDetails, BookingConstants.TrainSection currentSection,
                               BookingConstants.TrainSection updatedSection, int currentSeatNumber) {
        List<SeatDetails> seatDetails = trainAndSeatDetails.getSectionSeatDetailsMap().get(updatedSection);
        if (currentSection.name().equalsIgnoreCase(updatedSection.name())) {
            log.info("No change in section. Only Seat number will change");
            // find another seat in the same section and assign that seat number
            updateSeatStatus(passengerSeatDetails, seatDetails);
            // mark current seat as available
            seatDetails.get(currentSeatNumber - 1).setBookingStatus(AVAILABLE);
        } else {
            log.info("Changing section from {} to {}", currentSection, updatedSection);
            // find another seat in the other section and assign that seat number
            updateSeatStatus(passengerSeatDetails, seatDetails);
            // Copy this passengerDetails to the new section
            List<PassengerSeatDetails> updatedPassengerSeatDetailsList = bookingDetails.getTrainBookingDetails()
                    .getSectionWisePassengerSeatDetails().get(updatedSection);
            updatedPassengerSeatDetailsList.add(passengerSeatDetails);
            // Remove this passengerDetails from the current section
            List<PassengerSeatDetails> currentPassengerSeatDetailsList = bookingDetails.getTrainBookingDetails()
                    .getSectionWisePassengerSeatDetails().get(currentSection);
            currentPassengerSeatDetailsList.remove(passengerSeatDetails);
            // update the section wise passenger seat details in BookingDetails
            bookingDetails.getTrainBookingDetails().setSectionWisePassengerSeatDetails(
                    new HashMap<>(Map.of(currentSection, currentPassengerSeatDetailsList,
                            updatedSection, updatedPassengerSeatDetailsList)));

            // update the section wise seat count in booking details
            int updatedNewSectionCount = Objects.nonNull(bookingDetails.getTrainBookingDetails().getSectionWiseSeatCount()
                    .get(updatedSection)) ? bookingDetails.getTrainBookingDetails().getSectionWiseSeatCount().get(updatedSection) + 1 : 1;

            bookingDetails.getTrainBookingDetails().getSectionWiseSeatCount().put(currentSection,
                    bookingDetails.getTrainBookingDetails().getSectionWiseSeatCount().get(currentSection) - 1);
            bookingDetails.getTrainBookingDetails().getSectionWiseSeatCount().put(updatedSection, updatedNewSectionCount);

            // mark old seat as available
            trainAndSeatDetails.getSectionSeatDetailsMap().get(currentSection).get(currentSeatNumber - 1).setBookingStatus(AVAILABLE);
            /* Update Available seat count in the database/mock database. We increase the count of the current section by one and
               reduce the count of the updated section by one. */
            if (currentSection.name().equalsIgnoreCase(A.name())) {
                bookingRepository.updateSeatCountDetails(bookingDetails.getTrainNumber(), 1, -1);
            } else {
                bookingRepository.updateSeatCountDetails(bookingDetails.getTrainNumber(), -1, +1);
            }
        }
    }

    private void updateSeatStatus(PassengerSeatDetails passengerSeatDetails, List<SeatDetails> seatDetails) {
        for (SeatDetails seatDetail : seatDetails) {
            if (seatDetail.getBookingStatus() == AVAILABLE) {
                passengerSeatDetails.setSeatNumber(seatDetail.getSeatNumber());
                seatDetail.setBookingStatus(CONFIRMED);
                break;
            }
        }
    }

    private BookingConstants.TrainSection getUpdatedTrainSection(BookingConstants.TrainSection trainSection, int seatsAvailableInSecA,
                                                                 int seatsAvailableInSecB) {
        if (trainSection == A && seatsAvailableInSecA > 0) {
            return A;
        } else if (trainSection == B && seatsAvailableInSecB > 0) {
            return B;
        } else if (trainSection == A && seatsAvailableInSecB > 0) {
            return B;
        } else {
            return A;
        }
    }

    private void updateSeatAvailability(BookingDetails bookingDetails) {
        Map<BookingConstants.TrainSection, List<Integer>> secToBookedSeatNumbers = new HashMap<>();
        TrainBookingDetails trainBookingDetails = bookingDetails.getTrainBookingDetails();
        // Get the number of seats booked in each section
        int seatsBookedInSecA = Objects.nonNull(trainBookingDetails.getSectionWiseSeatCount().get(A)) ?
                trainBookingDetails.getSectionWiseSeatCount().get(A) : 0;
        int seatsBookedInSecB = Objects.nonNull(trainBookingDetails.getSectionWiseSeatCount().get(B)) ?
                trainBookingDetails.getSectionWiseSeatCount().get(B) : 0;

        Map<BookingConstants.TrainSection, List<PassengerSeatDetails>> sectionWisePassengerSeatDetails =
                trainBookingDetails.getSectionWisePassengerSeatDetails();
        for (Map.Entry<BookingConstants.TrainSection, List<PassengerSeatDetails>> entry : sectionWisePassengerSeatDetails.entrySet()) {
            List<PassengerSeatDetails> passengerSeatDetailsList = entry.getValue();
            for (PassengerSeatDetails passengerSeatDetails : passengerSeatDetailsList) {
                if (passengerSeatDetails.getBookingStatus() == CONFIRMED) {
                    secToBookedSeatNumbers.computeIfAbsent(entry.getKey(), k -> new ArrayList<>())
                            .add(passengerSeatDetails.getSeatNumber());
                }
            }
        }
        // Update the booking status of the seats to AVAILABLE
        bookingRepository.updateBookingStatus(bookingDetails.getTrainNumber(), secToBookedSeatNumbers, CONFIRMED, AVAILABLE);
        // Update the section wise seat count details
        bookingRepository.updateSeatCountDetails(bookingDetails.getTrainNumber(), seatsBookedInSecA, seatsBookedInSecB);

    }

    private void generateBookingId(BookingDetails bookingDetails, String trainNumber) {
        // In a real application, this would be generated by the database or a UUID generator.
        String bookingId = trainNumber + "-" + System.currentTimeMillis() + "-" + (int)(Math.random() * 1000);
        bookingDetails.setBookingId(bookingId);
        log.info("Generated booking ID: {} for train number: {}", bookingDetails.getBookingId(), trainNumber);
    }

    private void updateTrainDetails(BookingDetails bookingDetails, BookingRequest bookingRequest, int seatsInSecA, int seatsInSecB,
                                    TrainAndSeatDetails trainAndSeatDetails) {
        TrainBookingDetails trainBookingDetails = TrainBookingDetails.builder()
                .build();
        int updatedSeatsInSecA = seatsInSecA;
        int updatedSeatsInSecB = seatsInSecB;

        // First will try to allocate all seats in a single section
        if (bookingRequest.getNumberOfSeats() <= seatsInSecA) {
            // Allocate all seats in section A
            trainBookingDetails.setSectionWiseSeatCount(new HashMap<>(Map.of(A,
                    bookingRequest.getNumberOfSeats())));
            updatedSeatsInSecA -= bookingRequest.getNumberOfSeats();
            assignSeats(bookingRequest, trainBookingDetails, trainAndSeatDetails, bookingRequest.getNumberOfSeats(), 0);
        } else if (bookingRequest.getNumberOfSeats() <= seatsInSecB) {
            // Allocate all seats in section B
            trainBookingDetails.setSectionWiseSeatCount(new HashMap<>(Map.of(B,
                    bookingRequest.getNumberOfSeats())));
            updatedSeatsInSecB -= bookingRequest.getNumberOfSeats();
            assignSeats(bookingRequest, trainBookingDetails, trainAndSeatDetails, 0, bookingRequest.getNumberOfSeats());
        } else {
            /* Allocate seats in both sections. Firstly, allocate all available seats from section A and then allocate remaining
               seats from section B. */
            int seatsInB = bookingRequest.getNumberOfSeats() - seatsInSecA;

            trainBookingDetails.setSectionWiseSeatCount(new HashMap<>(Map.of(A, seatsInSecA,
                    B, seatsInB)));

            updatedSeatsInSecA = 0;
            updatedSeatsInSecB -= seatsInB;
            assignSeats(bookingRequest, trainBookingDetails, trainAndSeatDetails, seatsInSecA, seatsInB);
        }
        bookingDetails.setTrainBookingDetails(trainBookingDetails);
        log.info("Updated seats in section A: {} and section B: {} for train number: {}",
                updatedSeatsInSecA, updatedSeatsInSecB, bookingRequest.getTrainNumber());
        // Update the available seat count in the database/mock database
        bookingRepository.updateTrainAndSeatDetails(bookingRequest.getTrainNumber(),
                new HashMap<>(Map.of(A, updatedSeatsInSecA, B, updatedSeatsInSecB)));
    }

    private void assignSeats(BookingRequest bookingRequest, TrainBookingDetails trainBookingDetails,
                             TrainAndSeatDetails trainAndSeatDetails, int seatsInSecA, int seatsInSecB) {
        // In a real application, this would involve more complex logic to assign specific seats.
        List<PassengerSeatDetails> secASeatDetailsList = new ArrayList<>();
        List<PassengerSeatDetails> secBSeatDetailsList = new ArrayList<>();
        Map<BookingConstants.TrainSection, List<SeatDetails>> sectionToSeatDetailsMap =
                trainAndSeatDetails.getSectionSeatDetailsMap();
        for (Passenger passenger : bookingRequest.getPassengers()) {
            PassengerSeatDetails passengerSeatDetails = PassengerSeatDetails.builder()
                    .name(passenger.getFirstName() + " " + passenger.getLastName())
                    .age(passenger.getAge())
                    .bookingStatus(CONFIRMED)
                    .gender(passenger.getGender())
                    .build();

            if (seatsInSecA > 0) {
                checkAndAssignSeat(sectionToSeatDetailsMap.get(A), passengerSeatDetails);
                secASeatDetailsList.add(passengerSeatDetails);
                seatsInSecA--;
            } else {
                checkAndAssignSeat(sectionToSeatDetailsMap.get(B), passengerSeatDetails);
                secBSeatDetailsList.add(passengerSeatDetails);
                seatsInSecB--;
            }
        }

        trainBookingDetails.setSectionWisePassengerSeatDetails(new HashMap<>(Map.of(
                A, secASeatDetailsList,
                B, secBSeatDetailsList)));

    }

    private void checkAndAssignSeat(List<SeatDetails> seats, PassengerSeatDetails passengerSeatDetails) {
        for (SeatDetails seat : seats) {
            if (seat.getBookingStatus() == AVAILABLE) {
                seat.setBookingStatus(CONFIRMED); // this will be updated in the database in a real app
                passengerSeatDetails.setSeatNumber(seat.getSeatNumber());
                break;
            }
        }
    }
}
