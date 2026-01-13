package com.tcs.auth.exception;

import org.springframework.http.HttpStatus;

public class UserDeletedException extends ApiException {
    public UserDeletedException(String message) {
        super(message, HttpStatus.FORBIDDEN);
    }
}
