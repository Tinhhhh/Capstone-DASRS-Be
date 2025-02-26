package com.sep490.dasrsbackend.model.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class DasrsException extends RuntimeException {
    private HttpStatus httpStatus;

    public DasrsException(String message) {
        super(message);
    }

    public DasrsException(HttpStatus httpStatus, String message) {
        super(message);
        this.httpStatus = httpStatus;
    }

}
