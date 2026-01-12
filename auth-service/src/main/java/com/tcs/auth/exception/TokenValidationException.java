package com.tcs.auth.exception;

import org.springframework.http.HttpStatus;

public class TokenValidationException extends ApiException {
    public TokenValidationException(String message) {
        super(message, HttpStatus.UNAUTHORIZED);
    }
}
