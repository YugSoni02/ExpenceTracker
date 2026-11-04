package com.example.expensetracker.exception;

import java.time.Instant;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice(basePackages = "com.example.expensetracker.controller.api")
public class GlobalExceptionHandler {

	@ExceptionHandler(ResourceNotFoundException.class)
	public ResponseEntity<ApiError> handleNotFound(ResourceNotFoundException exception,
			HttpServletRequest request) {
		return error(HttpStatus.NOT_FOUND, exception.getMessage(), request);
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException exception,
			HttpServletRequest request) {
		String message = exception.getBindingResult().getFieldErrors().stream()
				.map(error -> error.getField() + ": " + error.getDefaultMessage())
				.collect(Collectors.joining("; "));
		return error(HttpStatus.BAD_REQUEST, message, request);
	}

	@ExceptionHandler({ HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class })
	public ResponseEntity<ApiError> handleMalformedRequest(Exception exception, HttpServletRequest request) {
		return error(HttpStatus.BAD_REQUEST, "The request contains an invalid value.", request);
	}

	private ResponseEntity<ApiError> error(HttpStatus status, String message, HttpServletRequest request) {
		String path = ServletUriComponentsBuilder.fromRequest(request).build().getPath();
		return ResponseEntity.status(status)
				.body(new ApiError(Instant.now(), status.value(), status.getReasonPhrase(), message, path));
	}

	public record ApiError(Instant timestamp, int status, String error, String message, String path) {
	}
}
