package com.primevideo.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entité centrale du catalogue — représente tout type de contenu diffusable.
 *
 * <p>Correspond à la table {@code contents} en base de données.
 * Un contenu peut être un film, une série, un animé, un K-Drama, etc.
 * Pour les séries, les épisodes sont accessibles via {@link Season} → {@link Episode}.</p>
 *
 * <p>Exemples de création :</p>
 * <pre>{@code
 * Content film = Content.builder()
 *     .title("Inception")
 *     .type(ContentType.FILM)
 *     .genre(Genre.SCIENCE_FICTION)
 *     .year(2010)
 *     .durationSeconds(8880)   // 2h 28min
 *     .isPremium(false)
 *     .build();
 * }</pre>
 */
@Entity
@Table(name = "contents")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Content {

    /** Identifiant auto-incrémenté par MySQL. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Titre du contenu. Obligatoire, 255 caractères max. */
    @NotBlank
    @Size(max = 255)
    @Column(nullable = false)
    private String title;

    /** Synopsis / description longue. Stocké en TEXT MySQL (pas de limite de taille). */
    @Column(columnDefinition = "TEXT")
    private String description;

    /**
     * Type de contenu — détermine l'affichage et le filtrage dans le catalogue.
     * <ul>
     *   <li>FILM — film avec {@code durationSeconds} renseigné</li>
     *   <li>SERIE — série avec {@link Season} et {@link Episode}</li>
     *   <li>ANIME — animé japonais (filtré dans la section Animés)</li>
     *   <li>KDRAMA — drama coréen (filtré dans la section K-Dramas)</li>
     *   <li>DOCUMENTAIRE, WEBTOON, LIVE — autres catégories</li>
     * </ul>
     */
    @Enumerated(EnumType.STRING)   // Stocké en texte ("FILM") et non en entier (0)
    @Column(nullable = false)
    private ContentType type;

    /** Genre cinématographique (ACTION, COMEDIE, THRILLER...). Optionnel. */
    @Enumerated(EnumType.STRING)
    private Genre genre;

    /** Année de sortie. Doit être entre 1888 (premier film de l'histoire) et 2100. */
    @Min(1888) @Max(2100)
    private Integer year;

    /**
     * Durée totale en secondes — utilisé pour les films et les épisodes.
     * Converti en "2h 18min" par {@link com.primevideo.service.CatalogService#formatDuration}.
     * Exemple : 8325 secondes = 2h 18min 45s.
     * Pour les séries, ce champ est null (la durée est sur chaque épisode).
     */
    @Column(name = "duration_seconds")
    @PositiveOrZero
    private Integer durationSeconds;

    /** URL de l'affiche verticale (format 2:3, ex : 400×600px). Affiché dans les cartes. */
    @Column(name = "poster_url")
    private String posterUrl;

    /** URL de la bannière horizontale (format 16:9). Affiché en hero sur la page de détail. */
    @Column(name = "banner_url")
    private String bannerUrl;

    /** URL de la bande-annonce (YouTube embed ou fichier vidéo direct). */
    @Column(name = "trailer_url")
    private String trailerUrl;

    /** URL du fichier vidéo principal (pour le lecteur). En production : lien CDN sécurisé. */
    @Column(name = "video_url")
    private String videoUrl;

    /**
     * Note moyenne calculée à partir des {@link Rating} des utilisateurs.
     * Échelle de 0.0 à 10.0. Mise à jour par {@link com.primevideo.repository.RatingRepository}.
     * Utilise {@code BigDecimal} et non {@code double} pour éviter les erreurs d'arrondi.
     */
    @DecimalMin("0.0") @DecimalMax("10.0")
    @Column(name = "rating_average", precision = 3, scale = 1)
    @Builder.Default
    private BigDecimal ratingAverage = BigDecimal.ZERO;

    /** Nombre total d'avis reçus — affiché à côté de la note (ex : "⭐ 8.5 (24 000 avis)"). */
    @Column(name = "rating_count")
    @Builder.Default
    private Integer ratingCount = 0;

    /**
     * Classification par âge.
     * Valeurs standard : 0 (Tous publics), 10, 12, 16, 18.
     * Utilisé par le contrôle parental des profils enfants ({@link Profile#maxAgeRating}).
     */
    @Column(name = "age_rating")
    @Builder.Default
    private Integer ageRating = 0;

    /**
     * Si {@code true}, ce contenu n'est visible qu'avec un abonnement actif.
     * Vérifié dans le controller avant d'autoriser la lecture.
     */
    @Column(name = "is_premium")
    @Builder.Default
    private Boolean isPremium = false;

    /** Si {@code false}, le contenu est masqué du catalogue (retiré temporairement). */
    @Column(name = "is_available")
    @Builder.Default
    private Boolean isAvailable = true;

    /** Date de sortie officielle (peut différer de l'année si sorti en fin d'année). */
    @Column(name = "release_date")
    private LocalDate releaseDate;

    /** Nom du réalisateur ou showrunner. */
    @Column(name = "director", length = 150)
    private String director;

    /**
     * Liste des acteurs principaux séparés par des virgules.
     * Exemple : "Leonardo DiCaprio, Joseph Gordon-Levitt, Elliot Page"
     * Stocké en TEXT pour les castings étoffés.
     */
    @Column(name = "cast_list", columnDefinition = "TEXT")
    private String castList;

    /** Code langue ISO 639-1 de l'audio original (ex : "fr", "en", "ja", "ko"). */
    @Column(length = 10)
    @Builder.Default
    private String language = "fr";

    /** Date d'ajout dans le catalogue — remplie automatiquement. */
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    /** Date de dernière modification — mise à jour automatiquement. */
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ── Relations ────────────────────────────────────────────────────────────

    /**
     * Saisons d'une série (vide pour un film).
     * Chaque saison contient des {@link Episode}.
     */
    @OneToMany(mappedBy = "content", cascade = CascadeType.ALL)
    @Builder.Default
    private List<Season> seasons = new ArrayList<>();

    /** Avis et notes laissés par les utilisateurs sur ce contenu. */
    @OneToMany(mappedBy = "content", cascade = CascadeType.ALL)
    @Builder.Default
    private List<Rating> ratings = new ArrayList<>();

    /** Références aux entrées watchlist pointant sur ce contenu. */
    @OneToMany(mappedBy = "content", cascade = CascadeType.ALL)
    @Builder.Default
    private List<WatchlistItem> watchlistItems = new ArrayList<>();

    // ── Métadonnées Spécialisées (v4.0) ──────────────────────────────────────

    @OneToOne(mappedBy = "content", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private AnimeMetadata animeMetadata;

    @OneToOne(mappedBy = "content", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private KDramaMetadata kdramaMetadata;

    @OneToOne(mappedBy = "content", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private WebtoonMetadata webtoonMetadata;

    // ── Enums ────────────────────────────────────────────────────────────────

    /** Types de contenus disponibles sur la plateforme. */
    public enum ContentType {
        FILM,          // Long-métrage
        SERIE,         // Série télévisée avec saisons
        DOCUMENTAIRE,  // Documentaire
        ANIME,         // Animation japonaise
        KDRAMA,        // Drama coréen
        WEBTOON,       // Adaptation de webtoon
        AFRICAIN,      // Cinéma Africain
        LIVE           // Streaming en direct
    }

    /** Genres cinématographiques disponibles pour le filtrage. */
    public enum Genre {
        ACTION, COMEDIE, DRAME, HORREUR, SCIENCE_FICTION, THRILLER,
        ROMANCE, ANIMATION, DOCUMENTAIRE, SPORT, AVENTURE, FANTAISIE,
        POLICIER, HISTORIQUE, MUSICAL, BIOGRAPHIE, AFRICAIN,
        SHONEN, SHOJO, SEINEN, ISEKAI, SCHOOL_LIFE,
        SORCELLERIE, ACTUALITE, REPORTAGE, SERIE, HISTOIRE
    }
}
