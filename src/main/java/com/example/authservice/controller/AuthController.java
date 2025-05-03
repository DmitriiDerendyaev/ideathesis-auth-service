// src/main/java/com/example/authservice/controller/AuthController.java

package com.example.authservice.controller;

import com.example.authservice.dto.*;
import com.example.authservice.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/bot-login")
    public ResponseEntity<BotLoginResponse> botLogin(@Valid @RequestBody VuzAuthRequest request) {
        return ResponseEntity.ok(authService.authenticateWithVuz(request));
    }
}