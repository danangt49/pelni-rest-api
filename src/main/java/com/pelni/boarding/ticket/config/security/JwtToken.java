package com.pelni.boarding.ticket.config.security;

import com.pelni.boarding.ticket.config.exception.CustomException;
import com.pelni.boarding.ticket.dto.AdminDto;
import com.pelni.boarding.ticket.dto.AnotherDataDto;
import io.jsonwebtoken.*;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;


@Slf4j
@Component
public class JwtToken {
    @Value("${app.secret.key}")
    private String secretKey;

    @PostConstruct
    protected void init() {
        secretKey = Base64.getEncoder().encodeToString(secretKey.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(AdminDto adminDto) {
        try {
            Map<String, Object> claims = new HashMap<>();
            claims.put("username", adminDto.getUsername());
            claims.put("name", adminDto.getName());
            claims.put("role", adminDto.getRole());
            claims.put("type", adminDto.getType());
            claims.put("printer", adminDto.getPrinter());
            claims.put("anotherData", adminDto.getAnotherData());

            return Jwts.builder()
                    .setClaims(claims)
                    .signWith(SignatureAlgorithm.HS512, secretKey)
                    .compact();
        } catch (Exception e) {
            throw new CustomException("Token generation failed: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public Claims parseToken(String token) {
        try {
            return Jwts.parser()
                    .setSigningKey(secretKey)
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            throw new CustomException("Token validation failed", HttpStatus.UNAUTHORIZED);
        }
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .setSigningKey(secretKey)
                    .parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
