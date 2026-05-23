package com.primevideo.dto.response;

import lombok.*;
import java.time.LocalDateTime;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
/**
 * DTO représentant un utilisateur — version sécurisée sans mot de passe.
 *
 * <p>On n'expose JAMAIS l'entité {@link com.primevideo.entity.User} directement
 * car elle contient le hash du mot de passe. Ce DTO ne contient que les informations
 * que le client a le droit de voir.</p>
 */
public class UserDTO {
    private Long id;
    private String fullName;
    private String email;
    private String role;
    private String avatarUrl;
    private Boolean isActive;
    private LocalDateTime createdAt;
}
