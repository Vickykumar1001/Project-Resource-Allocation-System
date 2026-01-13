package com.tcs.allocation.exception;

public class ExternalServiceException extends RuntimeException {
    public ExternalServiceException(String message) { super(message); }
    public ExternalServiceException(String message, Throwable t) { super(message, t); }
}
