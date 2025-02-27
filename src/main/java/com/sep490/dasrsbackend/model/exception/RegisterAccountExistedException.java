package com.sep490.dasrsbackend.model.exception;

public class RegisterAccountExistedException extends RuntimeException {
    public RegisterAccountExistedException(String message) {
        super(message);
    }
}
