package com.primevideo.dto.request;

import com.primevideo.entity.Content;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.springframework.web.multipart.MultipartFile;
import java.time.LocalDate;
import java.math.BigDecimal;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class ContentRequest {
    private Long id; // Ajout de l'ID pour les modifications
    
    @NotBlank(message = "Le titre est obligatoire")
    private String title;
    
    @NotBlank(message = "La description est obligatoire")
    private String description;
    
    @NotNull(message = "Le type est obligatoire")
    private Content.ContentType type;
    
    private Content.Genre genre;
    private Integer year;
    private Integer durationSeconds;
    private Integer durationHours;
    private Integer durationMinutes;
    private Integer episodeCount;
    private Integer uploadedEpisodeNumber;
    private MultipartFile posterFile;
    private String posterUrl;
    private MultipartFile bannerFile;
    private String bannerUrl;
    private MultipartFile videoFile;
    private String videoUrl;
    private String trailerUrl;
    private String director;
    private String castList;
    private String language;
    private LocalDate releaseDate;
    private Integer ageRating;
    private Boolean isPremium;
    private Boolean isAvailable;
    
    // Note moyenne (0.0 à 10.0)
    private BigDecimal ratingAverage;

    // Métadonnées spécialisées (optionnelles)
    private String studio;
    private String airStatus;
    private String network;
}
