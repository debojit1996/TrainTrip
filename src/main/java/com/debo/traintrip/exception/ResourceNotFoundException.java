package com.debo.traintrip.exception;

import lombok.Getter;

@Getter
public class ResourceNotFoundException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    private String problemType;

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String message, String problemType) {
        super(message);
        this.problemType = problemType;
    }

    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

}
