package com.tcs.project.exception;

public class BadRequestException extends RuntimeException {
    public BadRequestException(String message) { super(message); }
}
