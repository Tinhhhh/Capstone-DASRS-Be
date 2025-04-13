package com.sep490.dasrsbackend.model.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidTeamDataException extends RuntimeException {
    public InvalidTeamDataException(String message) {
        super(message);
    }
}
