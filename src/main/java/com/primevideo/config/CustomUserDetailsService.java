package com.primevideo.config;

import com.primevideo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Implémentation de {@link UserDetailsService} extraite dans sa propre classe
 * pour éviter la dépendance circulaire entre {@link JwtFilter} et {@link SecurityConfig}.
 *
 * <p>Problème sans cette classe :</p>
 * <pre>
 * JwtFilter        → a besoin de UserDetailsService
 * SecurityConfig   → a besoin de JwtFilter ET définit UserDetailsService
 *      ↑_______________________________________________↓   ← CYCLE !
 * </pre>
 *
 * <p>Solution : {@code UserDetailsService} est maintenant un composant Spring
 * indépendant ({@code @Service}). Il est créé avant tout le reste,
 * et {@link JwtFilter} comme {@link SecurityConfig} peuvent l'injecter sans cycle.</p>
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * Charge un utilisateur depuis la base de données à partir de son email.
     *
     * <p>Appelé automatiquement par Spring Security lors de chaque connexion
     * et lors de la vérification du token JWT dans {@link JwtFilter}.</p>
     *
     * @param email l'email de l'utilisateur (utilisé comme "username" dans Spring Security)
     * @return un objet {@link UserDetails} avec email, mot de passe hashé et rôle
     * @throws UsernameNotFoundException si aucun utilisateur ne correspond à cet email
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        String cleanEmail = (email != null) ? email.trim() : "";
        return userRepository.findByEmailIgnoreCase(cleanEmail)
                .map(user -> org.springframework.security.core.userdetails.User
                        .withUsername(user.getEmail())
                        .password(user.getPassword())
                        .roles(user.getRole().name())
                        .build())
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé : " + email));
    }
}
