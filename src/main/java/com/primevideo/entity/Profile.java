package com.primevideo.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Profil de visionnage appartenant à un {@link User}.
 *
 * <p>Correspond à la table {@code profiles}. Un compte peut avoir plusieurs profils
 * (comme Netflix) — profil adulte, profil enfant ({@code isKids = true}), etc.
 * Le contrôle parental est géré via {@code maxAgeRating} et {@code parentalPin}.</p>
 */
@Entity
@Table(name = "profiles")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Profile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @NotBlank
    @Size(min = 1, max = 50)
    @Column(nullable = false, length = 50)
    private String name;

    @Column(name = "avatar_url")
    private String avatarUrl;

    @Column(name = "is_kids")
    @Builder.Default
    private Boolean isKids = false;

    @Column(name = "parental_pin", length = 4)
    private String parentalPin;   // PIN 4 chiffres pour contrôle parental

    @Enumerated(EnumType.STRING)
    @Column(name = "age_rating")
    @Builder.Default
    private AgeRating ageRating = AgeRating.R_18;

    @Column(name = "language", length = 10)
    @Builder.Default
    private String language = "fr";

    @Column(name = "pin", length = 4)
    private String pin; // PIN 4 chiffres pour accéder à ce profil

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public enum AgeRating {
        ALL, U, PG, PG_13, R_16, R_18, NONE
    }

    // Relations
    @OneToMany(mappedBy = "profile", cascade = CascadeType.ALL)
    @Builder.Default
    private List<WatchlistItem> watchlist = new ArrayList<>();

    @OneToMany(mappedBy = "profile", cascade = CascadeType.ALL)
    @Builder.Default
    private List<ViewingHistory> history = new ArrayList<>();

    @OneToMany(mappedBy = "profile", cascade = CascadeType.ALL)
    @Builder.Default
    private List<Rating> ratings = new ArrayList<>();
}
