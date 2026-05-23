package com.primevideo.service;

import com.primevideo.dto.request.LoginRequest;
import com.primevideo.dto.request.RegisterRequest;
import com.primevideo.dto.response.AuthResponse;
import com.primevideo.dto.response.MfaSetupResponse;
import com.primevideo.dto.response.UserDTO;
import com.primevideo.entity.Device;
import com.primevideo.entity.User;
import com.primevideo.exception.BusinessException;
import com.primevideo.repository.DeviceRepository;
import com.primevideo.repository.UserRepository;
import com.primevideo.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import com.primevideo.config.CustomUserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

/**
 * Service gérant l'inscription et la connexion des utilisateurs.
 *
 * <p>C'est ici que se trouve la logique métier d'authentification :
 * vérification de l'unicité de l'email, hachage du mot de passe,
 * vérification des identifiants et génération du token JWT.</p>
 *
 * <p>Ce service est appelé par {@link com.primevideo.controller.AuthController}
 * aussi bien pour les pages Thymeleaf que pour les endpoints API REST.</p>
 *
 * <p>Annotations :</p>
 * <ul>
 *   <li>{@code @Service} — indique à Spring que c'est un composant de la couche métier</li>
 *   <li>{@code @Slf4j} — injecte un logger Lombok : {@code log.info("message")}</li>
 *   <li>{@code @Transactional} — garantit que si une étape échoue, toute l'opération est annulée</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final DeviceRepository deviceRepository;
    private final PasswordEncoder passwordEncoder;         // BCrypt (configuré dans SecurityConfig)
    private final AuthenticationManager authenticationManager; // Vérifie email + mot de passe
    private final CustomUserDetailsService userDetailsService;
    private final JwtUtil jwtUtil;                         // Génère et valide les tokens JWT
    private final MfaService mfaService;
    private final MailService mailService;
    private final NotificationService notificationService;

    /**
     * Inscrit un nouvel utilisateur.
     */
    @Transactional
    public UserDTO register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("Cet email est déjà utilisé");
        }
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new BusinessException("Les mots de passe ne correspondent pas");
        }
        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail().trim())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(User.Role.USER)
                .isActive(true)
                .build();
        User saved = userRepository.save(user);
        log.info("Nouvel utilisateur inscrit : {}", saved.getEmail());
        
        // Notification asynchrone
        notificationService.sendWelcomeEmail(saved);
        notificationService.createNotification(saved, "Bienvenue sur Prime Video !", "Nous sommes ravis de vous accueillir. Explorez nos milliers de contenus.", com.primevideo.entity.Notification.NotificationType.INFO, "/home");
        
        return toDTO(saved);
    }

    /**
     * Connecte un utilisateur et génère un token JWT.
     */
    @Transactional
    public AuthResponse login(LoginRequest request) {
        try {
            authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
        } catch (AuthenticationException e) {
            throw new BusinessException("Email ou mot de passe incorrect");
        }

        String cleanEmail = (request.getEmail() != null) ? request.getEmail().trim() : "";
        User user = userRepository.findByEmailIgnoreCase(cleanEmail)
                .orElseThrow(() -> new BusinessException("Utilisateur introuvable"));

        // 1. Vérification MFA
        if (user.getMfaEnabled()) {
            // Si le MFA est activé, on retourne un mfaToken temporaire
            log.info("MFA requis pour l'utilisateur : {}", user.getEmail());
            String mfaToken = jwtUtil.generateToken(userDetailsService.loadUserByUsername(user.getEmail())); // On pourrait utiliser un token plus court
            return AuthResponse.builder()
                    .requiresMFA(true)
                    .mfaToken(mfaToken)
                    .build();
        }

        // 2. Pas de MFA ou MFA déjà validé (via un autre endpoint) -> Générer les tokens finaux
        return completeLogin(user, request.getDeviceId());
    }

    /**
     * Renouvelle l'access token à partir d'un refresh token valide.
     */
    @Transactional
    public AuthResponse refreshAccessToken(String refreshToken) {
        User user = userRepository.findByRefreshToken(refreshToken)
                .orElseThrow(() -> new BusinessException("Refresh token invalide"));

        if (user.getRefreshTokenExpiresAt().isBefore(java.time.LocalDateTime.now())) {
            throw new BusinessException("Refresh token expiré, veuillez vous reconnecter");
        }

        // Rotation du refresh token : on en génère un nouveau
        return completeLogin(user, null); // On garde l'appareil actuel (ou null si inconnu ici)
    }

    /**
     * Finalise la connexion après validation du mot de passe (et du MFA si besoin).
     */
    @Transactional
    public AuthResponse completeLogin(User user, String deviceId) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        String accessToken = jwtUtil.generateToken(userDetails);
        
        // Génération du Refresh Token
        String refreshToken = jwtUtil.generateRefreshToken(); // On suppose que JwtUtil a cette méthode
        user.setRefreshToken(refreshToken);
        user.setRefreshTokenExpiresAt(java.time.LocalDateTime.now().plusDays(30));
        
        // Gestion de l'appareil
        if (deviceId != null) {
            Device device = deviceRepository.findByUserIdAndDeviceId(user.getId(), deviceId)
                    .orElse(Device.builder()
                            .user(user)
                            .deviceId(deviceId)
                            .name("Appareil inconnu") // On pourrait passer plus d'infos dans LoginRequest
                            .type(Device.DeviceType.OTHER)
                            .build());
            device.setLastSeenAt(java.time.LocalDateTime.now());
            deviceRepository.save(device);
        }

        userRepository.save(user);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(3600L)
                .user(toDTO(user))
                .build();
    }

    /**
     * Valide le second facteur MFA (TOTP).
     */
    @Transactional
    public AuthResponse verifyMfaChallenge(String mfaToken, int code, String deviceId) {
        // 1. Extraire l'email du mfaToken
        String email = jwtUtil.extractUsername(mfaToken);
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new BusinessException("Utilisateur non trouvé"));

        // 2. Vérifier le code TOTP
        if (!mfaService.verifyCode(user.getMfaSecret(), code)) {
            throw new BusinessException("Code MFA invalide");
        }

        // 3. Connexion réussie
        return completeLogin(user, deviceId);
    }

    /**
     * Déclenche la procédure de mot de passe oublié.
     */
    @Transactional
    public void forgotPassword(String email) {
        String cleanEmail = (email != null) ? email.trim() : "";
        User user = userRepository.findByEmailIgnoreCase(cleanEmail).orElse(null);
        if (user != null) {
            String token = java.util.UUID.randomUUID().toString().substring(0, 6).toUpperCase();
            user.setResetToken(token);
            user.setResetTokenExpiresAt(java.time.LocalDateTime.now().plusMinutes(15));
            userRepository.save(user);
            log.info("============== CODE DE REINITIALISATION POUR {} : {} ==============", email, token);
            mailService.sendPasswordResetEmail(email, token);
        }
        // Même si l'utilisateur n'existe pas, on ne dit rien pour éviter l'énumération
    }

    /**
     * Réinitialise le mot de passe via le token.
     */
    @Transactional
    public void resetPassword(String token, String newPassword) {
        String cleanToken = (token != null) ? token.trim().toUpperCase() : "";
        User user = userRepository.findByResetToken(cleanToken)
                .orElseThrow(() -> new BusinessException("Token invalide"));

        if (user.getResetTokenExpiresAt().isBefore(java.time.LocalDateTime.now())) {
            throw new BusinessException("Token expiré");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setResetToken(null);
        user.setResetTokenExpiresAt(null);
        user.setRefreshToken(null); // Invalide les sessions existantes
        userRepository.save(user);
        log.info("Mot de passe réinitialisé pour : {}", user.getEmail());
    }

    /**
     * Initialise la configuration MFA pour l'utilisateur connecté.
     */
    @Transactional
    public MfaSetupResponse initMfaSetup(String email) {
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new BusinessException("Utilisateur introuvable"));
        if (user.getMfaEnabled()) {
            throw new BusinessException("Le MFA est déjà activé");
        }

        String secret = mfaService.generateSecret();
        user.setMfaSecret(secret);
        userRepository.save(user);

        return MfaSetupResponse.builder()
                .secret(secret)
                .qrCodeUrl(mfaService.getQrCodeUrl(secret, user.getEmail()))
                .backupCodes(mfaService.generateBackupCodes())
                .build();
    }

    /**
     * Active définitivement le MFA après vérification du premier code.
     */
    @Transactional
    public void activateMfa(String email, int code, List<String> backupCodes) {
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new BusinessException("Utilisateur introuvable"));
        if (user.getMfaSecret() == null) {
            throw new BusinessException("MFA non initialisé");
        }

        if (!mfaService.verifyCode(user.getMfaSecret(), code)) {
            throw new BusinessException("Code invalide");
        }

        user.setMfaEnabled(true);
        user.setBackupCodes(backupCodes);
        userRepository.save(user);
        log.info("MFA activé pour l'utilisateur : {}", user.getEmail());
    }

    /**
     * Convertit une entité {@link User} en {@link UserDTO} sécurisé.
     *
     * <p>Cette conversion est OBLIGATOIRE : on n'expose jamais l'entité brute
     * car elle contient le hash du mot de passe et d'autres données sensibles.
     * Le DTO ne contient que ce que le client a le droit de voir.</p>
     */
    private UserDTO toDTO(User user) {
        return UserDTO.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .role(user.getRole().name())
                .avatarUrl(user.getAvatarUrl())
                .isActive(user.getIsActive())
                .createdAt(user.getCreatedAt())
                // NB : le mot de passe (même hashé) n'est JAMAIS inclus dans le DTO
                .build();
    }
}
