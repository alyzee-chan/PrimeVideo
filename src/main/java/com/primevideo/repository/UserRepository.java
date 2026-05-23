package com.primevideo.repository;

import com.primevideo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Accès à la table {@code users} en base de données.
 *
 * <p>Étend {@link JpaRepository} qui fournit toutes les opérations CRUD sans écrire du SQL :</p>
 * <ul>
 *   <li>{@code findAll()} → SELECT * FROM users</li>
 *   <li>{@code findById(1L)} → SELECT * FROM users WHERE id = 1</li>
 *   <li>{@code save(user)} → INSERT ou UPDATE selon si l'id existe déjà</li>
 *   <li>{@code deleteById(1L)} → DELETE FROM users WHERE id = 1</li>
 * </ul>
 *
 * <p>Les méthodes personnalisées ci-dessous sont générées automatiquement
 * par Spring Data JPA à partir de leur nom.</p>
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Recherche un utilisateur par son email.
     * Utilisé lors de la connexion et dans le {@link com.primevideo.config.SecurityConfig}.
     *
     * <p>Retourne un {@code Optional} car l'email peut ne pas exister.
     * Exemple : {@code userRepository.findByEmail("test@test.com").orElseThrow(...)}</p>
     *
     * <p>SQL généré : {@code SELECT * FROM users WHERE email = ?}</p>
     */
    Optional<User> findByEmail(String email);
    Optional<User> findByEmailIgnoreCase(String email);

    /**
     * Vérifie si un email est déjà enregistré — plus efficace que {@code findByEmail} car
     * retourne juste un booléen sans charger tout l'objet User.
     * Utilisé dans {@link com.primevideo.service.AuthService#register} pour éviter les doublons.
     *
     * <p>SQL généré : {@code SELECT COUNT(*) > 0 FROM users WHERE email = ?}</p>
     */
    boolean existsByEmail(String email);
    /**
     * Recherche un utilisateur par son refresh token.
     */
    Optional<User> findByRefreshToken(String refreshToken);

    /**
     * Recherche un utilisateur par son token de réinitialisation.
     */
    Optional<User> findByResetToken(String resetToken);
}
