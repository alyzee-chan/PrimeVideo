package com.primevideo.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
/**
 * DTO pour la requête d'inscription.
 *
 * <p>Contient les données du formulaire d'inscription avec des contraintes de validation
 * strictes sur chaque champ. La vérification que les deux mots de passe correspondent
 * est une validation métier effectuée dans {@link com.primevideo.service.AuthService#register},
 * car elle nécessite de comparer deux champs — ce qu'une simple annotation ne peut pas faire.</p>
 */
public class RegisterRequest {

    @NotBlank(message = "Le nom complet est obligatoire")
    @Size(min = 2, max = 100, message = "Le nom doit contenir entre 2 et 100 caractères")
    private String fullName;

    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "Format d'email invalide")
    private String email;

    @NotBlank(message = "Le mot de passe est obligatoire")
    @Size(min = 8, message = "Le mot de passe doit contenir au moins 8 caractères")
    @Pattern(regexp = ".*[A-Z].*", message = "Le mot de passe doit contenir au moins une majuscule")
    @Pattern(regexp = ".*[0-9].*", message = "Le mot de passe doit contenir au moins un chiffre")
    private String password;

    @NotBlank(message = "La confirmation du mot de passe est obligatoire")
    private String confirmPassword;
}
