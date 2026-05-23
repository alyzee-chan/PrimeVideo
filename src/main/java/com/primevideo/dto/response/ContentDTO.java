package com.primevideo.dto.response;

import com.primevideo.entity.Content;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class ContentDTO {
    private Long id;
    private String title;
    private String description;
    private Content.ContentType type;
    private Content.Genre genre;
    private Integer year;
    private Integer durationSeconds;
    private String posterUrl;
    private String bannerUrl;
    private String trailerUrl;
    private String videoUrl;
    private BigDecimal ratingAverage;
    private Integer ratingCount;
    private Integer ageRating;
    private Boolean isPremium;
    private String director;
    private String castList;
    private String language;
    private LocalDate releaseDate;
    // Champs calculés
    private String durationFormatted;   // "2h 18min"
    private Boolean inWatchlist;        // true si dans la watchlist du profil courant
    private Boolean hasAccess;          // true si l'utilisateur peut regarder ce contenu

    // Métadonnées spécialisées
    private AnimeMetadataDTO animeMetadata;
    private KDramaMetadataDTO kdramaMetadata;
    private WebtoonMetadataDTO webtoonMetadata;

    // Séries et Épisodes
    private java.util.List<SeasonDTO> seasons;
}
