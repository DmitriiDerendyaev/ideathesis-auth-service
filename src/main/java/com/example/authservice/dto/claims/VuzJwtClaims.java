package com.example.authservice.dto.claims;

import com.nimbusds.jwt.JWTClaimsSet;
import io.jsonwebtoken.Claims;
import lombok.Builder;
import lombok.Data;

import java.util.Date;
import java.util.UUID;


public class VuzJwtClaims {
    private UUID guid;
    private String role;
    private String issuer;
    private String audience;
    private Date issuedAt;
    private Date expiration;

    public static VuzJwtClaims fromNimbusClaims(JWTClaimsSet claims) {
        VuzJwtClaims vuzJwtClaims = new VuzJwtClaims();
        vuzJwtClaims.guid = UUID.fromString((String) claims.getClaim("IndividualGuid"));
        vuzJwtClaims.role = (String) claims.getClaim("http://schemas.microsoft.com/ws/2008/06/identity/claims/role");
        vuzJwtClaims.issuer = claims.getIssuer();
        vuzJwtClaims.audience = claims.getAudience().get(0);
        vuzJwtClaims.issuedAt = claims.getIssueTime();
        vuzJwtClaims.expiration = claims.getExpirationTime();
        return vuzJwtClaims;
    }

    // Getters and Setters
    public UUID getGuid() { return guid; }
    public void setGuid(UUID guid) { this.guid = guid; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getIssuer() { return issuer; }
    public void setIssuer(String issuer) { this.issuer = issuer; }

    public String getAudience() { return audience; }
    public void setAudience(String audience) { this.audience = audience; }

    public Date getIssuedAt() { return issuedAt; }
    public void setIssuedAt(Date issuedAt) { this.issuedAt = issuedAt; }

    public Date getExpiration() { return expiration; }
    public void setExpiration(Date expiration) { this.expiration = expiration; }
}