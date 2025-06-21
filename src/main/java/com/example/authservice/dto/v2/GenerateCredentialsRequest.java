package com.example.authservice.dto.v2;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class GenerateCredentialsRequest {
    @NotBlank
    private String targetGuid;
    @NotBlank
    private String username;
    @NotBlank
    private String password;
}
