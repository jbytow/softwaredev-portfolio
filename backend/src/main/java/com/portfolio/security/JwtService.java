package com.portfolio.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
@Slf4j
public class JwtService {

    // Committed as the fallback in application.yml, so anyone can sign tokens with it
    static final String DEV_DEFAULT_SECRET = "dev_jwt_secret_key_minimum_32_characters_long_here";
    // HS256 needs at least 256 bits of key material
    private static final int MIN_SECRET_BYTES = 32;

    private final Environment environment;

    @Value("${app.jwt.secret}")
    private String secretKey;

    @Value("${app.jwt.expiration}")
    private long jwtExpiration;

    private SecretKey signInKey;

    @PostConstruct
    void init() {
        byte[] keyBytes = secretKey == null ? new byte[0] : secretKey.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < MIN_SECRET_BYTES) {
            throw new IllegalStateException("app.jwt.secret (JWT_SECRET) must be at least "
                    + MIN_SECRET_BYTES + " bytes, got " + keyBytes.length);
        }
        if (environment.acceptsProfiles(Profiles.of("prod")) && DEV_DEFAULT_SECRET.equals(secretKey)) {
            throw new IllegalStateException(
                    "app.jwt.secret is the committed development default in the prod profile; set JWT_SECRET");
        }
        signInKey = Keys.hmacShaKeyFor(keyBytes);
    }

    public Duration getExpiration() {
        return Duration.ofMillis(jwtExpiration);
    }

    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public String generateToken(String email, String name, boolean isAdmin) {
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("name", name);
        extraClaims.put("isAdmin", isAdmin);
        return generateToken(extraClaims, email);
    }

    public String generateToken(Map<String, Object> extraClaims, String email) {
        return Jwts.builder()
                .claims(extraClaims)
                .subject(email)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(signInKey)
                .compact();
    }

    public boolean isTokenValid(String token) {
        try {
            return !isTokenExpired(token);
        } catch (SignatureException e) {
            // INFO, not WARN: anyone can send forged tokens, so this must not flood the log
            log.info("JWT rejected: {}", e.getClass().getSimpleName());
            return false;
        } catch (JwtException | IllegalArgumentException e) {
            log.debug("JWT rejected: {}", e.getClass().getSimpleName());
            return false;
        }
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(signInKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean extractIsAdmin(String token) {
        Claims claims = extractAllClaims(token);
        return Boolean.TRUE.equals(claims.get("isAdmin", Boolean.class));
    }

    public String extractName(String token) {
        Claims claims = extractAllClaims(token);
        return claims.get("name", String.class);
    }
}
