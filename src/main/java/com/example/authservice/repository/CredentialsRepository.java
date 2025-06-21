package com.example.authservice.repository;

import com.example.authservice.model.Credentials;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CredentialsRepository extends JpaRepository<Credentials, UUID> {
    Optional<Credentials> findByUsername(String username);
    Optional<Credentials> findByUserGuid(UUID userGuid);
}
