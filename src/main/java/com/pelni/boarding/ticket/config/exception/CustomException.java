package com.pelni.boarding.ticket.config.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.io.Serial;

public class CustomException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    @Getter
    private final HttpStatus httpStatus;
    private final String message;


    public CustomException(String message, HttpStatus httpStatus) {
        this.message = message;
        this.httpStatus = httpStatus;
    }

    @Override
    public String getMessage() {
        return message;
    }

}
