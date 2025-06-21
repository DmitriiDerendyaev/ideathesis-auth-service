package com.example.authservice.service;

import com.example.authservice.dto.UserDTO;
import com.example.authservice.dto.v2.GenerateCredentialsRequest;
import com.example.authservice.dto.v2.LoginRequest;
import com.example.authservice.dto.v2.LoginResponse;
import com.example.authservice.exception.*;
import com.example.authservice.model.Credentials;
import com.example.authservice.model.User;
import com.example.authservice.repository.CredentialsRepository;
import com.example.authservice.repository.UserRepository;
import com.example.authservice.security.JwtTokenProviderV2;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthServiceV2 {
    private final UserRepository userRepository;
    private final CredentialsRepository credentialsRepository;
    private final JwtTokenProviderV2 jwtTokenProvider;

    public LoginResponse login(LoginRequest request) {
        try {
            Optional<Credentials> credentialsOpt = credentialsRepository.findByUsername(request.getUsername());
            if (credentialsOpt.isEmpty()) {
                throw new InvalidCredentialsException("Пользователь не найден");
            }
            Credentials credentials = credentialsOpt.get();
            if (!BCrypt.checkpw(request.getPassword(), credentials.getPassword())) {
                throw new InvalidCredentialsException("Неверный пароль");
            }
            Optional<User> userOpt = userRepository.findByGuid(credentials.getUserGuid());
            if (userOpt.isEmpty()) {
                throw new UserNotFoundException("Пользователь не найден");
            }

            User user = userOpt.get();
            if(user.getUserType().equals("employee")) {
                String[] fullName = user.getFullName().split(" ");
                user.setLastName(fullName[0]);
                user.setFirstName(fullName[1]);
                user.setMiddleName(fullName[2]);
            }
            String accessToken = jwtTokenProvider.generateAccessToken(user.getGuid(), credentials.getUsername());
            String refreshToken = jwtTokenProvider.generateRefreshToken(user.getGuid(), credentials.getUsername());
            return LoginResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .user(convertToDto(user))
                    .message("Успешная авторизация")
                    .build();
        } catch (DataAccessException ex) {
            throw new ServiceUnavailableException("Ошибка доступа к базе данных: " + ex.getMessage());
        }
    }

    public String generateCredentials(GenerateCredentialsRequest request, String token) {
        try {
            // Проверка токена
            if (!jwtTokenProvider.validateToken(token)) {
                throw new InvalidTokenException("Невалидный токен");
            }
            UUID currentUserGuid = jwtTokenProvider.getUserIdFromToken(token);

            // Проверка текущего пользователя
            Optional<User> currentUserOpt = userRepository.findByGuid(currentUserGuid);
            if (currentUserOpt.isEmpty()) {
                throw new UserNotFoundException("Текущий пользователь не найден");
            }
            User currentUser = currentUserOpt.get();

            // Проверка целевого пользователя
            UUID targetGuid;
            try {
                targetGuid = UUID.fromString(request.getTargetGuid());
            } catch (IllegalArgumentException ex) {
                throw new InvalidCredentialsException("Неверный формат targetGuid");
            }
            if (targetGuid.equals(currentUserGuid)) {
                throw new ForbiddenException("Нельзя генерировать учетные данные для себя");
            }
            Optional<User> targetUserOpt = userRepository.findByGuid(targetGuid);
            if (targetUserOpt.isEmpty()) {
                throw new UserNotFoundException("Целевой пользователь не найден");
            }
            User targetUser = targetUserOpt.get();

            // Проверка прав доступа
            boolean hasPermission = (currentUser.getUserType().equals("superuser") && targetUser.getUserType().equals("employee")) ||
                    (currentUser.getUserType().equals("employee") && targetUser.getUserType().equals("student"));
            if (!hasPermission) {
                throw new ForbiddenException("Нет прав для генерации учетных данных");
            }

            // Хеширование пароля и создание/обновление учетных данных
            String hashedPassword = BCrypt.hashpw(request.getPassword(), BCrypt.gensalt());
            Optional<Credentials> existingCredentialsOpt = credentialsRepository.findByUserGuid(targetGuid);
            Credentials credentials;
            if (existingCredentialsOpt.isPresent()) {
                credentials = existingCredentialsOpt.get();
                credentials.setUsername(request.getUsername());
                credentials.setPassword(hashedPassword);
            } else {
                credentials = Credentials.builder()
                        .id(UUID.randomUUID())
                        .username(request.getUsername())
                        .password(hashedPassword)
                        .userGuid(targetGuid)
                        .build();
            }
            try {
                credentialsRepository.save(credentials);
                return "Учетные данные созданы";
            } catch (DataIntegrityViolationException e) {
                throw new DuplicateUsernameException("Логин уже занят");
            }
        } catch (DataAccessException ex) {
            throw new ServiceUnavailableException("Ошибка доступа к базе данных: " + ex.getMessage());
        }
    }

    private UserDTO convertToDto(User user) {
        return UserDTO.builder()
                .guid(user.getGuid())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .middleName(user.getMiddleName())
                .phone(user.getPhone())
                .userType(user.getUserType())
                .build();
    }
}