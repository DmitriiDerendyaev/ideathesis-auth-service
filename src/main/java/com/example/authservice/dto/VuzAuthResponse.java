package com.example.authservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VuzAuthResponse {
    private String token;
    private String guid;
    private String jwt;
    private String jwt_refresh;
}