package com.example.authservice.security;

import com.example.authservice.exception.InvalidTokenException;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;


@Slf4j
@Component
@RequiredArgsConstructor
public class JwtTokenProviderV2 {
    @Value("${jwt.secret-key}")
    private String secretKey;

    @Value("${jwt.token-expiration-millis}")
    private long tokenExpirationMillis;

    @Value("${jwt.refresh-token-expiration-millis}")
    private long refreshTokenExpirationMillis;

    public String generateAccessToken(UUID userId, String username) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + tokenExpirationMillis);

        return Jwts.builder()
                .subject(username)
                .claim("userId", userId.toString())
                .claim("type", "ACCESS")
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8)), Jwts.SIG.HS512)
                .compact();
    }

    public String generateRefreshToken(UUID userId, String username) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + refreshTokenExpirationMillis);

        return Jwts.builder()
                .subject(username)
                .claim("userId", userId.toString())
                .claim("type", "REFRESH")
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8)), Jwts.SIG.HS512)
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8)))
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (MalformedJwtException ex) {
            throw new InvalidTokenException("Invalid JWT token: " + ex.getMessage());
        } catch (ExpiredJwtException ex) {
            throw new InvalidTokenException("Expired JWT token: " + ex.getMessage());
        } catch (UnsupportedJwtException ex) {
            throw new InvalidTokenException("Unsupported JWT token: " + ex.getMessage());
        } catch (IllegalArgumentException ex) {
            throw new InvalidTokenException("JWT claims string is empty: " + ex.getMessage());
        } catch (JwtException ex) {
            throw new InvalidTokenException("JWT signature does not match: " + ex.getMessage());
        }
    }

    public UUID getUserIdFromToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8)))
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return UUID.fromString(claims.get("userId", String.class));
        } catch (JwtException ex) {
            throw new InvalidTokenException("Cannot extract userId from token: " + ex.getMessage());
        }
    }

    public String getUsernameFromToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8)))
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .getSubject();
        } catch (JwtException ex) {
            throw new InvalidTokenException("Cannot extract username from token: " + ex.getMessage());
        }
    }

    public String getTokenTypeFromToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8)))
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .get("type", String.class);
        } catch (JwtException ex) {
            throw new InvalidTokenException("Cannot extract token type from token: " + ex.getMessage());
        }
    }

    public Date getExpirationDateFromToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8)))
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .getExpiration();
        } catch (JwtException ex) {
            throw new InvalidTokenException("Cannot extract expiration date from token: " + ex.getMessage());
        }
    }
}