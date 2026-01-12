package com.tcs.auth.exception;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.tcs.auth.dto.ErrorResponse;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;

@ControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(ApiException.class)
	public ResponseEntity<ErrorResponse> handleApiException(ApiException ex, HttpServletRequest request) {
		HttpStatus status = ex.getStatus();
		ErrorResponse body = ErrorResponse.builder().timestamp(System.currentTimeMillis()).status(status.value())
				.error(status.getReasonPhrase()).message(ex.getMessage()).path(request.getRequestURI()).build();
		return ResponseEntity.status(status).body(body);
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex,
			HttpServletRequest request) {

		List<String> errors = ex.getBindingResult().getFieldErrors().stream()
				.map(fe -> fe.getField() + ": " + fe.getDefaultMessage()).collect(Collectors.toList());

		ErrorResponse body = ErrorResponse.builder().timestamp(System.currentTimeMillis())
				.status(HttpStatus.BAD_REQUEST.value()).error(HttpStatus.BAD_REQUEST.getReasonPhrase())
				.message("Validation failed").path(request.getRequestURI()).errors(errors).build();

		return ResponseEntity.badRequest().body(body);
	}

	// @for param validation
	@ExceptionHandler(ConstraintViolationException.class)
	public ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException ex,
			HttpServletRequest request) {

		List<String> errors = ex.getConstraintViolations().stream()
				.map(cv -> cv.getPropertyPath() + ": " + cv.getMessage()).collect(Collectors.toList());

		ErrorResponse body = ErrorResponse.builder().timestamp(System.currentTimeMillis())
				.status(HttpStatus.BAD_REQUEST.value()).error(HttpStatus.BAD_REQUEST.getReasonPhrase())
				.message("Validation failed").path(request.getRequestURI()).errors(errors).build();

		return ResponseEntity.badRequest().body(body);
	}

	@ExceptionHandler(AccessDeniedException.class)
	public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
		ErrorResponse body = ErrorResponse.builder().timestamp(System.currentTimeMillis())
				.status(HttpStatus.FORBIDDEN.value()).error(HttpStatus.FORBIDDEN.getReasonPhrase())
				.message("Access denied").path(request.getRequestURI()).build();

		return ResponseEntity.status(HttpStatus.FORBIDDEN).body(body);
	}

	// for authentication problems
	@ExceptionHandler(AuthenticationException.class)
	public ResponseEntity<ErrorResponse> handleAuthException(AuthenticationException ex, HttpServletRequest request) {
		ErrorResponse body = ErrorResponse.builder().timestamp(System.currentTimeMillis())
				.status(HttpStatus.UNAUTHORIZED.value()).error(HttpStatus.UNAUTHORIZED.getReasonPhrase())
				.message(ex.getMessage()).path(request.getRequestURI()).build();

		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
	}

	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<ErrorResponse> handleIllegalArg(IllegalArgumentException ex, HttpServletRequest request) {
		ErrorResponse body = ErrorResponse.builder().timestamp(System.currentTimeMillis())
				.status(HttpStatus.BAD_REQUEST.value()).error(HttpStatus.BAD_REQUEST.getReasonPhrase())
				.message(ex.getMessage()).path(request.getRequestURI()).build();

		return ResponseEntity.badRequest().body(body);
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponse> handleAll(Exception ex, HttpServletRequest request) {

		ErrorResponse body = ErrorResponse.builder().timestamp(System.currentTimeMillis())
				.status(HttpStatus.INTERNAL_SERVER_ERROR.value())
				.error(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase()).message("An unexpected error occurred")
				.path(request.getRequestURI()).build();

		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
	}
}
