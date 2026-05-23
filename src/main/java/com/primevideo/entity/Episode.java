package com.primevideo.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;

/**
 * Épisode individuel d'une série — appartient à une {@link Season}.
 *
 * <p>Correspond à la table {@code episodes}. La durée est stockée en secondes
 * (même principe que pour les films). Un épisode peut avoir sa propre miniature.</p>
 */
@Entity
@Table(name = "episodes")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Episode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "season_id", nullable = false)
    private Season season;

    @Min(1)
    @Column(name = "episode_number", nullable = false)
    private Integer episodeNumber;

    @NotBlank
    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @PositiveOrZero
    @Column(name = "duration_seconds")
    private Integer durationSeconds;

    @Column(name = "video_url")
    private String videoUrl;

    @Column(name = "thumbnail_url")
    private String thumbnailUrl;

    @Column(name = "is_available")
    @Builder.Default
    private Boolean isAvailable = true;
}
