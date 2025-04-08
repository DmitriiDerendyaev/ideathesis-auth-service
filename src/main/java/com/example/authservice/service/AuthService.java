package com.example.authservice.service;

import com.example.authservice.dto.LoginRequest;
import com.example.authservice.dto.LoginResponse;
import com.example.authservice.dto.RegisterRequest;
import com.example.authservice.dto.UserDTO;
import com.example.authservice.exception.InvalidCredentialsException;
import com.example.authservice.exception.UserAlreadyExistsException;
import com.example.authservice.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Base64;

@Service
public class AuthService {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Value("${user-service.base-url}")
    private String userServiceBaseUrl;

    public UserDTO register(RegisterRequest request) {
        // Проверяем, существует ли уже пользователь с таким username или email
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Basic " + Base64.getEncoder().encodeToString("admin:admin".getBytes()));

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            restTemplate.exchange(userServiceBaseUrl + "/search?username=" + request.getUsername(), HttpMethod.GET, entity, UserDTO.class);
            throw new UserAlreadyExistsException("User with this username already exists");
        } catch (Exception e) {
            // Если пользователь не найден, продолжаем регистрацию
        }

        try {
            restTemplate.exchange(userServiceBaseUrl + "/search?email=" + request.getEmail(), HttpMethod.GET, entity, UserDTO.class);
            throw new UserAlreadyExistsException("User with this email already exists");
        } catch (Exception e) {
            // Если пользователь не найден, продолжаем регистрацию
        }

        UserDTO userDTO = UserDTO.builder()
                .username(request.getUsername())
                .password(request.getPassword())
                .email(request.getEmail())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .build();

        HttpEntity<UserDTO> userEntity = new HttpEntity<>(userDTO, headers);

        ResponseEntity<UserDTO> response = restTemplate.exchange(userServiceBaseUrl, HttpMethod.POST, userEntity, UserDTO.class);
        return response.getBody();
    }

    public LoginResponse login(LoginRequest request) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Basic " + Base64.getEncoder().encodeToString("admin:admin".getBytes()));

        HttpEntity<LoginRequest> entity = new HttpEntity<>(request, headers);

        try {
            ResponseEntity<UserDTO> userResponse = restTemplate.exchange(userServiceBaseUrl + "/search?username=" + request.getUsername(), HttpMethod.GET, entity, UserDTO.class);
            UserDTO userDTO = userResponse.getBody();

            if (!userDTO.getPassword().equals(request.getPassword())) {
                throw new InvalidCredentialsException("Invalid username or password");
            }

            String accessToken = jwtTokenProvider.generateAccessToken(userDTO.getId(), userDTO.getUsername());
            String refreshToken = jwtTokenProvider.generateRefreshToken(userDTO.getId(), userDTO.getUsername());

            return LoginResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .build();
        } catch (Exception e) {
            throw new InvalidCredentialsException("Invalid username or password");
        }
    }

    public String generateAccessToken(Long userId, String username) {
        return jwtTokenProvider.generateAccessToken(userId, username);
    }

    public String generateRefreshToken(Long userId, String username) {
        return jwtTokenProvider.generateRefreshToken(userId, username);
    }

    public boolean validateToken(String token) {
        return jwtTokenProvider.validateToken(token);
    }

    public Long getUserIdFromToken(String token) {
        return jwtTokenProvider.getUserIdFromToken(token);
    }
}