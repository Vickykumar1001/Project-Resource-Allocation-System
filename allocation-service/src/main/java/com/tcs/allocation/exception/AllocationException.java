package com.tcs.allocation.exception;

public class AllocationException extends RuntimeException {
    public AllocationException(String message) { super(message); }
    public AllocationException(String message, Throwable t) { super(message,t); }
}
