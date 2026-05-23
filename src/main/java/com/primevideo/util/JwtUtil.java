package com.primevideo.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Utilitaire pour la génération et la validation des tokens JWT (JSON Web Token).
 *
 * <p>Un JWT est une chaîne encodée en Base64 composée de 3 parties séparées par des points :</p>
 * <pre>
 * eyJhbGciOiJIUzI1NiJ9    ←  Header (algorithme de signature : HS256)
 * .eyJ1c2VySWQiOjEyM30    ←  Payload (données : email, expiration)
 * .SflKxwRJSMeKKF2QT4fw   ←  Signature (vérifie que le token n'a pas été falsifié)
 * </pre>
 *
 * <p>La clé secrète ({@code jwt.secret} dans application.yml) sert à signer et vérifier
 * les tokens. Elle ne doit jamais être exposée publiquement.</p>
 *
 * <p>Cycle de vie d'un token :</p>
 * <ol>
 *   <li>Connexion réussie → {@link #generateToken} crée un token valide 1h</li>
 *   <li>Chaque requête → {@link JwtFilter} appelle {@link #isTokenValid} pour vérifier</li>
 *   <li>Après 1h → {@link #isTokenExpired} retourne true → token rejeté → reconnexion</li>
 * </ol>
 */
@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long expiration;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        return createToken(claims, userDetails.getUsername());
    }

    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey())
                .compact();
    }

    public String extractUsername(String token) {
        return extractEmail(token);
    }

    public String extractEmail(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    public String generateRefreshToken() {
        return java.util.UUID.randomUUID().toString();
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        try {
            String email = extractEmail(token);
            return email.equals(userDetails.getUsername()) && !isTokenExpired(token);
        } catch (JwtException e) {
            return false;
        }
    }

    private boolean isTokenExpired(String token) {
        Date expDate = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getExpiration();
        return expDate.before(new Date());
    }
}
