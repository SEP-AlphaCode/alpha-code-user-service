package com.alpha_code.alpha_code_user_service.util;

import com.alpha_code.alpha_code_user_service.entity.Account;
import com.alpha_code.alpha_code_user_service.service.RoleService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

@Component
public class JwtUtil {

    private final RoleService roleService;
    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration-ms}")
    private int jwtExpirationMs;

    @Value("${jwt.refresh-expiration-ms}")
    private int refreshTokenExpirationMs;

    @Value("${jwt.reset-password-expiration-ms}")
    private int resetPasswordTokenExpirationMs;

    public JwtUtil(RoleService roleService) {
        this.roleService = roleService;
    }

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateAccessToken(Account account) {
        var role = roleService.getById(account.getRoleId());
        return Jwts.builder()
                .claims(Map.of(
                        "id", account.getId(),
                        "fullName", account.getFullName(),
                        "username", account.getUsername(),
                        "email", account.getEmail(),
                        "roleId", account.getRoleId(),
                        "roleName", role.getName()
                ))
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .signWith(getSigningKey(), Jwts.SIG.HS512)
                .compact();
    }

    public Integer getRefreshTokenExpirationMs() {
        return refreshTokenExpirationMs;
    }

    public String generateRefreshToken(Account account) {
        return Jwts.builder()
                .claims(Map.of(
                        "id", account.getId().toString(),
                        "username", account.getUsername(),
                        "email", account.getEmail(),
                        "roleId", account.getRoleId(),
                        "roleName", roleService.getById(account.getRoleId()).getName()
                ))
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + refreshTokenExpirationMs))
                .signWith(getSigningKey(), Jwts.SIG.HS512)
                .compact();
    }

    public Claims getAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String getUsernameFromJwt(String token) {
        return getAllClaims(token).getSubject();
    }

    public UUID getUserIdFromToken(String token) {
        String idStr = getAllClaims(token).get("id", String.class);
        return UUID.fromString(idStr);
    }

    public UUID getRoleIdFromToken(String token) {
        String idStr = getAllClaims(token).get("roleId", String.class);
        return UUID.fromString(idStr);
    }

    public boolean validateJwtToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public String generateResetPasswordToken(Account account) {
        return Jwts.builder()
                .claims(Map.of(
                        "id", account.getId(),
                        "email", account.getEmail()
                ))
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + resetPasswordTokenExpirationMs))
                .signWith(getSigningKey(), Jwts.SIG.HS512)
                .compact();
    }

    public String extractEmail(String token) {
        return getAllClaims(token).get("email", String.class);
    }
}
