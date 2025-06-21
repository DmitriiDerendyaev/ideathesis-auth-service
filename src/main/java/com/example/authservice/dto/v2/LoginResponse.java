package com.example.authservice.dto.v2;

import com.example.authservice.dto.UserDTO;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoginResponse {
    private String accessToken;
    private String refreshToken;
    private UserDTO user;
    private String message;
}
