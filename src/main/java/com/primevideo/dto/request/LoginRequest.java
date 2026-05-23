package com.primevideo.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
/**
 * DTO pour la requête de connexion.
 *
 * <p>Reçu depuis le formulaire de login Thymeleaf (via {@code @ModelAttribute})
 * ou depuis l'API REST (via {@code @RequestBody} en JSON).
 * Les contraintes {@code @NotBlank} et {@code @Email} sont vérifiées
 * automatiquement par {@code @Valid} dans {@link com.primevideo.controller.AuthController}.</p>
 */
public class LoginRequest {

    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "Format d'email invalide")
    private String email;

    @NotBlank(message = "Le mot de passe est obligatoire")
    private String password;

    private String deviceId;
}
