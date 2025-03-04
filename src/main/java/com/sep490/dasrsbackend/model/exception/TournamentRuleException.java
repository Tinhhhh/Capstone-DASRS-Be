package com.sep490.dasrsbackend.model.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.util.Map;

@Getter
public class TournamentRuleException extends RuntimeException {
    private HttpStatus httpStatus;
    private Map<String, String> data;

    public TournamentRuleException(String message) {
        super(message);
    }

    public TournamentRuleException(HttpStatus httpStatus, String message, Map<String, String> data) {
        super(message);
        this.httpStatus = httpStatus;
        this.data = data;
    }
}
