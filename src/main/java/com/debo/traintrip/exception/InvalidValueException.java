package com.debo.traintrip.exception;

public class InvalidValueException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    private String problemType;

    public InvalidValueException(String message, String problemType) {
        super(message);
        this.problemType = problemType;
    }

    public InvalidValueException(String message) {
        super(message);
    }

    public InvalidValueException(String message, Throwable cause) {
        super(message, cause);
    }

    public String getProblemType() {
        return problemType;
    }
}
