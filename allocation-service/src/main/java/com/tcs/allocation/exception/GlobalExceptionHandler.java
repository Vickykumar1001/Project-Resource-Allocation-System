package com.tcs.allocation.exception;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.tcs.allocation.dto.ErrorResponse;

import jakarta.servlet.http.HttpServletRequest;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(NotFoundException ex, HttpServletRequest req) {
        ErrorResponse err = buildError(HttpStatus.NOT_FOUND, ex.getMessage(), req.getRequestURI(), null);
        return new ResponseEntity<>(err, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(ExternalServiceException.class)
    public ResponseEntity<ErrorResponse> handleExternal(ExternalServiceException ex, HttpServletRequest req) {
        ErrorResponse err = buildError(HttpStatus.BAD_GATEWAY, ex.getMessage(), req.getRequestURI(), null);
        return new ResponseEntity<>(err, HttpStatus.BAD_GATEWAY);
    }

    @ExceptionHandler(AllocationException.class)
    public ResponseEntity<ErrorResponse> handleAllocation(AllocationException ex, HttpServletRequest req) {
        ErrorResponse err = buildError(HttpStatus.BAD_REQUEST, ex.getMessage(), req.getRequestURI(), null);
        return new ResponseEntity<>(err, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest req) {
        List<String> errors = new ArrayList<>();
        ex.getBindingResult().getFieldErrors().forEach(fe -> errors.add(fe.getField() + ": " + fe.getDefaultMessage()));
        ErrorResponse err = buildError(HttpStatus.BAD_REQUEST, "Validation failed", req.getRequestURI(), errors);
        return new ResponseEntity<>(err, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAny(Exception ex, HttpServletRequest req) {
        ErrorResponse err = buildError(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage() == null ? "Unexpected error" : ex.getMessage(), req.getRequestURI(), null);
        return new ResponseEntity<>(err, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private ErrorResponse buildError(HttpStatus status, String message, String path, List<String> errors) {
        ErrorResponse er = new ErrorResponse();
        er.setTimestamp(Instant.now().toEpochMilli());
        er.setStatus(status.value());
        er.setError(status.getReasonPhrase());
        er.setMessage(message);
        er.setPath(path);
        er.setErrors(errors == null ? Collections.emptyList() : errors);
        return er;
    }
}
