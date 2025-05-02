package com.example.authservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BotLoginResponse {
    private String accessToken;
    private String refreshToken;
    private UserDTO user;
    private String message;
}