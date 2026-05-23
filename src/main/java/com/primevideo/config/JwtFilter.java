package com.primevideo.config;

import com.primevideo.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filtre HTTP qui intercepte chaque requête pour vérifier la présence d'un token JWT.
 *
 * <p>Ce filtre s'exécute UNE SEULE FOIS par requête (grâce à {@link OncePerRequestFilter})
 * et AVANT tous les autres filtres Spring Security.</p>
 *
 * <p>Fonctionnement :</p>
 * <pre>
 * Requête HTTP entrante
 *       ↓
 * Y a-t-il un header "Authorization: Bearer xxx" ?
 *       ├── Non → laisser passer (Spring Security gérera l'accès selon les règles de SecurityConfig)
 *       └── Oui → extraire et valider le token JWT
 *                     ├── Token invalide/expiré → laisser passer SANS authentifier (→ 401 si route protégée)
 *                     └── Token valide → authentifier l'utilisateur dans le SecurityContext
 *                                            ↓
 *                                       Requête traitée par le Controller
 * </pre>
 *
 * <p>Ce filtre est utilisé principalement pour les appels API REST (ex : depuis Postman
 * ou une future appli mobile). Pour les pages Thymeleaf, l'authentification se fait
 * via le formulaire de login et les sessions Spring Security.</p>
 */
@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService;

    /**
     * Méthode principale exécutée pour chaque requête HTTP.
     *
     * @param request     la requête HTTP entrante
     * @param response    la réponse HTTP sortante
     * @param filterChain la chaîne de filtres — appeler {@code doFilter} pour passer au suivant
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // Étape 1 : lire le header "Authorization"
        // Format attendu : "Bearer eyJhbGci..."
        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            // Pas de token JWT → on passe au filtre suivant sans authentifier
            // Spring Security appliquera alors ses propres règles (session, accès public/protégé)
            filterChain.doFilter(request, response);
            return;
        }

        // Étape 2 : extraire le token (enlever le préfixe "Bearer ")
        String token = authHeader.substring(7);

        try {
            // Étape 3 : extraire l'email depuis le payload du token
            String email = jwtUtil.extractEmail(token);

            // Étape 4 : si l'email est présent ET l'utilisateur n'est pas déjà authentifié
            // (évite de recharger inutilement si le filtre est appelé plusieurs fois)
            if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                // Étape 5 : charger les détails de l'utilisateur depuis la base
                UserDetails userDetails = userDetailsService.loadUserByUsername(email);

                // Étape 6 : valider le token (signature correcte + non expiré)
                if (jwtUtil.isTokenValid(token, userDetails)) {
                    // Étape 7 : créer un objet d'authentification et le placer dans le contexte
                    // À partir de ce moment, l'utilisateur est considéré comme connecté pour cette requête
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,                           // Pas de credentials (déjà validés)
                                    userDetails.getAuthorities());  // Rôles : ROLE_USER ou ROLE_ADMIN
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        } catch (Exception e) {
            // Token malformé, signature invalide, expiré, etc.
            // On ne bloque pas la requête — Spring Security refusera l'accès si la route est protégée
            logger.warn("JWT invalide : " + e.getMessage());
        }

        // Passer au filtre suivant dans la chaîne
        filterChain.doFilter(request, response);
    }
}
