package com.debo.traintrip.advice;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.debo.traintrip.exception.InvalidValueException;
import com.debo.traintrip.exception.ReservationFailedException;
import com.debo.traintrip.exception.ResourceNotFoundException;
import com.debo.traintrip.model.Problem;

import lombok.extern.slf4j.Slf4j;

@ControllerAdvice
@Slf4j
public class BookingControllerAdvice {

    @ExceptionHandler({ReservationFailedException.class})
    public ResponseEntity<Problem> handleFailedReservationException(ReservationFailedException exception) {
        log.error("ReservationFailedException occurred: ", exception);

        Problem problem = this.constructProblem(exception.getMessage(), exception.getProblemType());
        return this.getProblemResponseEntity(HttpStatus.BAD_REQUEST, problem);
    }

    @ExceptionHandler({ResourceNotFoundException.class})
    public ResponseEntity<Problem> handleResourceNotFoundException(ResourceNotFoundException exception) {
        log.error("ResourceNotFoundException occurred: ", exception);

        Problem problem = this.constructProblem(exception.getMessage(), exception.getProblemType());
        return this.getProblemResponseEntity(HttpStatus.NOT_FOUND, problem);
    }

    @ExceptionHandler({InvalidValueException.class})
    public ResponseEntity<Problem> handleInvalidValueException(InvalidValueException exception) {
        log.error("InvalidValueException occurred: ", exception);

        Problem problem = this.constructProblem(exception.getMessage(), exception.getProblemType());
        return this.getProblemResponseEntity(HttpStatus.BAD_REQUEST, problem);
    }

    private Problem constructProblem(String message, String problemType) {
        Problem problemObj = Problem.builder()
                .problemId(UUID.randomUUID())
                .problemDescription(message)
                .build();
        if (problemType != null) {
            problemObj.setProblemType(problemType);
        }
        return problemObj;
    }

    private ResponseEntity<Problem> getProblemResponseEntity(HttpStatus status, Problem problem) {
        ResponseEntity<Problem> response = new ResponseEntity<>(problem, status);
        log.debug("<-- Error-response: {}", response);
        return response;
    }
}
