package com.debo.traintrip.exception;

public class ReservationFailedException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    private String problemType;

    public ReservationFailedException(String message) {
        super(message);
    }

    public ReservationFailedException(String message, String problemType) {
        super(message);
        this.problemType = problemType;
    }

    public ReservationFailedException(String message, Throwable cause) {
        super(message, cause);
    }

    public String getProblemType() {
        return problemType;
    }
}
