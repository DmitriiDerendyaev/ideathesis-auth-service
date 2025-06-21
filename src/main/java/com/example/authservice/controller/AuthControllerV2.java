package com.example.authservice.controller;


import com.example.authservice.dto.v2.GenerateCredentialsRequest;
import com.example.authservice.dto.v2.LoginRequest;
import com.example.authservice.dto.v2.LoginResponse;
import com.example.authservice.service.AuthServiceV2;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/v2")
@RequiredArgsConstructor
public class AuthControllerV2 {
    private final AuthServiceV2 authService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/generate-credentials")
    public ResponseEntity<String> generateCredentials(
            @Valid @RequestBody GenerateCredentialsRequest request,
            @RequestHeader("Authorization") String authorizationHeader) {
        String token = authorizationHeader.replace("Bearer ", "");
        return ResponseEntity.ok(authService.generateCredentials(request, token));
    }
}
