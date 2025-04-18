package com.debo.traintrip.exception;

public class ReservationFailedException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public ReservationFailedException(String message) {
        super(message);
    }

    public ReservationFailedException(String message, Throwable cause) {
        super(message, cause);
    }
}
