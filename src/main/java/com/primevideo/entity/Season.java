package com.primevideo.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Saison d'une série — regroupe ses {@link Episode}.
 *
 * <p>Correspond à la table {@code seasons}. Une série ({@link Content}) peut avoir
 * plusieurs saisons, chacune numérotée par {@code seasonNumber}.</p>
 */
@Entity
@Table(name = "seasons")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Season {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "content_id", nullable = false)
    private Content content;

    @Min(1)
    @Column(name = "season_number", nullable = false)
    private Integer seasonNumber;

    @NotBlank
    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "poster_url")
    private String posterUrl;

    @Column(name = "release_year")
    private Integer releaseYear;

    @OneToMany(mappedBy = "season", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Episode> episodes = new ArrayList<>();
}
