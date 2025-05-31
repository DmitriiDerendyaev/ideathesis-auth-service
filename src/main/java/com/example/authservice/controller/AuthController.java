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

import java.util.UUID;


@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    // Константы для проверки
    private static final String STUB_LOGIN = "tigina";
    private static final String STUB_PASSWORD = "teacher!";
    private static final UUID STUB_GUID = UUID.fromString("def01000-ff00-0f00-0f00-000000032870");

    @PostMapping("/bot-login")
    public ResponseEntity<BotLoginResponse> botLogin(@Valid @RequestBody VuzAuthRequest request) {
        // Проверяем, совпадает ли логин и пароль с заданными
        if (STUB_LOGIN.equals(request.getUlogin()) && STUB_PASSWORD.equals(request.getUpassword())) {
            // Возвращаем заглушку
            return ResponseEntity.ok(createStubResponse());
        }

        // Иначе обычная авторизация через сервис вуза
        return ResponseEntity.ok(authService.authenticateWithVuz(request));
    }

    private BotLoginResponse createStubResponse() {
        UserDTO user = UserDTO.builder()
                .guid(STUB_GUID)
                .email(null)
                .firstName("Мария")
                .lastName("Тигина")
                .middleName("Степановна")
                .phone(null)
                .userType("employee")
                .build();

        return BotLoginResponse.builder()
                .accessToken("eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiLQlNC10YDQtdC90LTRj9C10LIg0JTQvNC40YLRgNC40Lkg0KHQtdGA0LPQtdC10LLQuNGHIiwidXNlcklkIjoiNmUyMjQ1MzktZTNiMC0xMWViLTgwZDMtYzVhODVlOThhNjFjIiwidHlwZSI6IkFDQ0VTUyIsImlhdCI6MTc0NzMxNjEwMCwiZXhwIjoxNzQ3MzE5NzAwfQ.hq-cj1QXuxvQajLQrlpJrs2J_Md0jhtZiVifAq3jBLkCucvcliHY5jo73FkXv43u2cPfHWX4MDroyxCuDvnqCQ")
                .refreshToken("eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiLQlNC10YDQtdC90LTRj9C10LIg0JTQvNC40YLRgNC40Lkg0KHQtdGA0LPQtdC10LLQuNGHIiwidXNlcklkIjoiNmUyMjQ1MzktZTNiMC0xMWViLTgwZDMtYzVhODVlOThhNjFjIiwidHlwZSI6IlJFRlJFU0giLCJpYXQiOjE3NDczMTYxMDAsImV4cCI6MTc0NzQwMjUwMH0.DdHIdw5hse6IwiAsTIgR3V2bB9ns69q_Y-g_Yga1Ls7aZUd6r7E8ZLLZLV1hFLwilEayvnY7_PKQb2fok74mwQ")
                .user(user)
                .message("Успешная авторизация")
                .build();
    }
}