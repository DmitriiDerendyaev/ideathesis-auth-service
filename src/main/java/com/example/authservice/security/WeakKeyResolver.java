package com.example.authservice.security;

import io.jsonwebtoken.SigningKeyResolver;
import io.jsonwebtoken.JwsHeader;
import io.jsonwebtoken.Claims;

import java.security.Key;
import java.security.PublicKey;

public class WeakKeyResolver implements SigningKeyResolver {
    private final PublicKey vuzPublicKey;

    public WeakKeyResolver(PublicKey vuzPublicKey) {
        this.vuzPublicKey = vuzPublicKey;
    }

    @Override
    public Key resolveSigningKey(JwsHeader jwsHeader, Claims claims) {
        return vuzPublicKey;
    }

    @Override
    public Key resolveSigningKey(JwsHeader jwsHeader, byte[] bytes) {
        return null;
    }

}