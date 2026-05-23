package com.primevideo.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
/**
 * Réponse retournée après une connexion réussie via l'API REST.
 *
 * <p>Contient le token JWT à conserver côté client pour les requêtes suivantes.
 * Le token doit être envoyé dans le header : {@code Authorization: Bearer <accessToken>}</p>
 *
 * <p>Exemple de réponse JSON :</p>
 * <pre>{@code
 * {
 *   "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
 *   "tokenType": "Bearer",
 *   "expiresIn": 3600,
 *   "user": { "id": 1, "email": "user@test.com", "role": "USER" }
 * }
 * }</pre>
 */
public class AuthResponse {
    private String accessToken;
    private String refreshToken;
    @Builder.Default
    private String tokenType = "Bearer";
    private Long expiresIn;
    private UserDTO user;

    // MFA fields
    private boolean requiresMFA;
    private String mfaToken;
}
