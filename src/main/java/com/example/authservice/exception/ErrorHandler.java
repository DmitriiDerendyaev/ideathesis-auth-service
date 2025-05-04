package com.example.authservice.exception;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatusCode;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import org.springframework.web.ErrorResponse;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.LinkedHashMap;

import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class ErrorHandler extends ResponseEntityExceptionHandler {

    // InvalidCredentialsException — 401
    @ExceptionHandler(InvalidCredentialsException.class)
    public ApiError handleInvalidCredentials(InvalidCredentialsException ex, HttpServletRequest request) {
        return buildApiError(ex, 401, "Invalid Credentials");
    }

    // InvalidTokenException — 401
    @ExceptionHandler(InvalidTokenException.class)
    public ApiError handleInvalidToken(InvalidTokenException ex, HttpServletRequest request) {
        return buildApiError(ex, 401, "Invalid Token");
    }

    // ServiceUnavailableException — 503
    @ExceptionHandler(ServiceUnavailableException.class)
    public ApiError handleServiceUnavailable(ServiceUnavailableException ex, HttpServletRequest request) {
        return buildApiError(ex, 503, "Service Unavailable");
    }

    // MethodArgumentNotValidException — 400
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {

        String detail = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .collect(Collectors.joining(", "));

        ApiError error = buildApiError(ex, status.value(), detail);
        return new ResponseEntity<>(error, status);
    }

    // Обработка всех прочих RuntimeException
    @ExceptionHandler(RuntimeException.class)
    public ApiError handleAllOtherExceptions(RuntimeException ex, HttpServletRequest request) {
        return buildApiError(ex, 500, "Internal Server Error");
    }

    // Построение объекта ApiError
    private ApiError buildApiError(Throwable ex, int status, String message) {
        Map<String, String> trace = new LinkedHashMap<>();
        trace.put("exception", ex.getClass().getName());
        trace.put("message", ex.getMessage());

        StackTraceElement[] stackTrace = ex.getStackTrace();
        for (int i = 0; i < Math.min(stackTrace.length, 5); i++) {
            trace.put("stack[" + i + "]", stackTrace[i].toString());
        }

        return new ApiError(status, message, trace);
    }
}