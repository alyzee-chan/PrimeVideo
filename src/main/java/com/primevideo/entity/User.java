package com.primevideo.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entité représentant un compte utilisateur sur la plateforme.
 *
 * <p>Correspond à la table {@code users} en base de données.
 * Un utilisateur peut posséder plusieurs {@link Profile} (comme sur Netflix),
 * plusieurs {@link Subscription} et un historique de {@link Payment}.</p>
 *
 * <p>Annotations Lombok utilisées :</p>
 * <ul>
 *   <li>{@code @Getter / @Setter} — génère automatiquement tous les getters/setters</li>
 *   <li>{@code @NoArgsConstructor} — constructeur vide requis par JPA</li>
 *   <li>{@code @AllArgsConstructor} — constructeur avec tous les champs</li>
 *   <li>{@code @Builder} — permet d'écrire {@code User.builder().email("...").build()}</li>
 * </ul>
 */
@Entity
@Table(name = "users", uniqueConstraints = {
    @UniqueConstraint(columnNames = "email")   // L'email est unique en base
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class User {

    /** Identifiant auto-incrémenté par MySQL. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Nom complet affiché sur le compte (ex : "Jean Dupont"). */
    @NotBlank
    @Size(min = 2, max = 100)
    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;

    /** Email de connexion — unique en base, vérifié par @Email. */
    @NotBlank
    @Email
    @Column(nullable = false, unique = true, length = 150)
    private String email;

    /**
     * Mot de passe hashé avec BCrypt.
     * JAMAIS stocké en clair. Le hash est généré dans {@link com.primevideo.service.AuthService}.
     * Exemple de hash : {@code $2a$10$...}
     */
    @NotBlank
    @Column(nullable = false)
    private String password;

    /**
     * Rôle de l'utilisateur.
     * <ul>
     *   <li>{@code USER} — accès standard à la plateforme</li>
     *   <li>{@code ADMIN} — accès à /admin/** et aux fonctions de gestion</li>
     * </ul>
     * Stocké en base sous forme de texte (ex : "USER") grâce à {@code EnumType.STRING}.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private Role role = Role.USER;

    /** Si false, le compte est désactivé et la connexion est refusée. */
    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    public Boolean getIsActive() {
        return isActive != null && isActive;
    }

    /** Indique si l'utilisateur a confirmé son email (fonctionnalité optionnelle). */
    @Column(name = "email_verified")
    @Builder.Default
    private Boolean emailVerified = false;

    /** URL de la photo de profil (stockée sur un CDN ou en local). */
    @Column(name = "avatar_url")
    private String avatarUrl;

    /** Secret TOTP pour le MFA (Base32). */
    @Column(name = "mfa_secret")
    private String mfaSecret;

    /** Indique si le MFA est activé sur ce compte. */
    @Column(name = "mfa_enabled")
    @Builder.Default
    private Boolean mfaEnabled = false;

    /** Codes de secours à usage unique pour le MFA. */
    @ElementCollection
    @CollectionTable(name = "user_backup_codes", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "code")
    @Builder.Default
    private List<String> backupCodes = new ArrayList<>();

    /** Refresh token pour renouveler l'access token. */
    @Column(name = "refresh_token")
    private String refreshToken;

    /** Date d'expiration du refresh token. */
    @Column(name = "refresh_token_expires_at")
    private LocalDateTime refreshTokenExpiresAt;

    /** 
     * Indique si l'utilisateur a un abonnement Prime actif.
     * Pour les administrateurs, cette méthode retourne toujours true.
     */
    @Column(name = "is_prime")
    @Builder.Default
    private Boolean isPrime = false;

    public Boolean getIsPrime() {
        if (this.role == Role.ADMIN) return true;
        return isPrime != null && isPrime;
    }

    /** Points de fidélité cumulés. */
    @Column(name = "loyalty_points")
    @Builder.Default
    private Integer loyaltyPoints = 0;

    public Integer getLoyaltyPoints() {
        return loyaltyPoints != null ? loyaltyPoints : 0;
    }

    /** Numéro de téléphone pour les alertes SMS/Sécurité. */
    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    /** Token de réinitialisation de mot de passe. */
    @Column(name = "reset_token")
    private String resetToken;

    /** Date d'expiration du token de réinitialisation. */
    @Column(name = "reset_token_expires_at")
    private LocalDateTime resetTokenExpiresAt;

    /** Date de création du compte — remplie automatiquement par Hibernate, jamais modifiable. */
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    /** Date de dernière modification — mise à jour automatiquement par Hibernate. */
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ── Relations ────────────────────────────────────────────────────────────

    /**
     * Liste des profils de cet utilisateur (multi-profils, comme Netflix).
     * {@code cascade = ALL} : si on supprime l'utilisateur, ses profils sont supprimés aussi.
     * {@code orphanRemoval} : si on retire un profil de la liste, il est supprimé en base.
     */
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Profile> profiles = new ArrayList<>();

    /** Liste des appareils connectés. */
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Device> devices = new ArrayList<>();

    /** Historique des abonnements (actifs, expirés, annulés). */
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    @Builder.Default
    private List<Subscription> subscriptions = new ArrayList<>();

    /** Historique de tous les paiements effectués par cet utilisateur. */
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    @Builder.Default
    private List<Payment> payments = new ArrayList<>();

    /** Rôles disponibles sur la plateforme. */
    public enum Role {
        USER,   // Utilisateur standard
        ADMIN   // Administrateur (accès complet)
    }
}
