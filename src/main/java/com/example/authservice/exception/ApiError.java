package com.example.authservice.exception;

import java.util.Map;

public record ApiError(int status, String message, Map<String, String> stackTrace) {
}