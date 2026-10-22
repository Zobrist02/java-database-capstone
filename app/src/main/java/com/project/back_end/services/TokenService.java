package com.project.back_end.services;

import com.project.back_end.repo.AdminRepository;
import com.project.back_end.repo.DoctorRepository;
import com.project.back_end.repo.PatientRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class TokenService {

    private final AdminRepository adminRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final String jwtSecret;

    public TokenService(
            AdminRepository adminRepository,
            DoctorRepository doctorRepository,
            PatientRepository patientRepository,
            @Value("${jwt.secret}") String jwtSecret
    ) {
        this.adminRepository = adminRepository;
        this.doctorRepository = doctorRepository;
        this.patientRepository = patientRepository;
        this.jwtSecret = jwtSecret;
    }

    public String generateToken(String identifier) {
        return generateToken(identifier, null);
    }

    public String generateToken(String identifier, String role) {
        Date now = new Date();
        var builder = Jwts.builder()
                .subject(identifier)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + 7L * 24 * 60 * 60 * 1000))
                .signWith(getSigningKey());

        if (role != null) {
            builder.claim("role", role);
        }

        return builder.compact();
    }

    public String extractIdentifier(String token) {
        return parseClaims(token).getSubject();
    }

    public boolean validateToken(String token, String user) {
        try {
            Claims claims = parseClaims(token);
            String identifier = claims.getSubject();
            String tokenRole = claims.get("role", String.class);

            if (identifier == null || !user.equalsIgnoreCase(tokenRole)) {
                return false;
            }

            return switch (user.toLowerCase()) {
                case "admin" -> adminRepository.findByUsername(identifier) != null;
                case "doctor" -> doctorRepository.findByEmail(identifier) != null;
                case "patient" -> patientRepository.findByEmail(identifier) != null;
                default -> false;
            };
        } catch (Exception exception) {
            return false;
        }
    }

    public SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}