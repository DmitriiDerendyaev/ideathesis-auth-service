package com.example.authservice.service;

import com.example.authservice.dto.*;
import com.example.authservice.dto.claims.VuzJwtClaims;
import com.example.authservice.exception.InvalidCredentialsException;
import com.example.authservice.exception.InvalidTokenException;
import com.example.authservice.model.User;
import com.example.authservice.repository.UserRepository;
import com.example.authservice.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import javax.naming.ServiceUnavailableException;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;

import com.nimbusds.jwt.SignedJWT;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.crypto.RSASSAVerifier;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AuthService {
    private final WebClient webClient;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

    @Value("${vuz.auth-url}")
    private String vuzAuthUrl;

    @Value("${vuz.jwt-public-key}")
    private String vuzPublicKey;

    public BotLoginResponse authenticateWithVuz(VuzAuthRequest request) {
        // 1. Аутентификация в системе вуза
        VuzAuthResponse vuzResponse = authenticateAtVuz(request);

        // 2. Верификация JWT от вуза
        VuzJwtClaims vuzClaims = verifyVuzJwt(vuzResponse.getJwt());

        // 3. Поиск пользователя в нашей системе по guid
        UUID userGuid = vuzClaims.getGuid();
        Optional<User> userOpt = userRepository.findByGuid(userGuid);

        if (userOpt.isEmpty()) {
            return BotLoginResponse.builder()
                    .message("Данные синхронизируются, попробуйте позже")
                    .build();
        }

        User user = userOpt.get();

        // 4. Генерация наших токенов
        String accessToken = jwtTokenProvider.generateAccessToken(user.getGuid(), user.getFullName());
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getGuid(), user.getFullName());

        return BotLoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .user(convertToDto(user))
                .message("Успешная авторизация")
                .build();
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

    private VuzAuthResponse authenticateAtVuz(VuzAuthRequest request) {
        return webClient.post()
                .uri(vuzAuthUrl)
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .bodyValue(createFormData(request))
                .retrieve()
                .onStatus(
                        status -> status == HttpStatus.UNAUTHORIZED,
                        response -> Mono.error(new InvalidCredentialsException("Неверные учетные данные вуза"))
                )
                .onStatus(
                        status -> status == HttpStatus.INTERNAL_SERVER_ERROR,
                        response -> Mono.error(new ServiceUnavailableException("Сервис вуза недоступен"))
                )
                .bodyToMono(VuzAuthResponse.class)
                .block();
    }

    private MultiValueMap<String, String> createFormData(VuzAuthRequest request) {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("ulogin", request.getUlogin());
        formData.add("upassword", request.getUpassword());
        return formData;
    }

    private VuzJwtClaims verifyVuzJwt(String jwt) {
        try {
            // Парсим JWT
            SignedJWT signedJWT = SignedJWT.parse(jwt);

            // Получаем публичный ключ
            PublicKey publicKey = parsePublicKey(vuzPublicKey);
            RSAPublicKey rsaPublicKey = (RSAPublicKey) publicKey;

            // Создаем наш кастомный верификатор с отключенной проверкой длины ключа
            RSASSAVerifier verifier = new RSASSAVerifier(rsaPublicKey) {
                protected void validateKeyLength() {
                    // Просто игнорируем проверку длины ключа
                }
            };

            // Проверяем подпись
            boolean isValid = signedJWT.verify(verifier);

            if (!isValid) {
                throw new InvalidTokenException("Невалидный токен вуза", null);
            }

            return VuzJwtClaims.fromNimbusClaims(signedJWT.getJWTClaimsSet());

        } catch (JOSEException | java.text.ParseException e) {
            throw new InvalidTokenException("Ошибка при верификации токена вуза", e);
        } catch (Exception e) {
            throw new InvalidTokenException("Ошибка парсинга публичного ключа", e);
        }
    }

    private PublicKey parsePublicKey(String publicKey) throws Exception {
        publicKey = publicKey.replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s", "");

        byte[] keyBytes = Base64.getDecoder().decode(publicKey);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePublic(spec);
    }
}