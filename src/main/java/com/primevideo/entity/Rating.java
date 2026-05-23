package com.primevideo.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Note et avis d'un profil sur un contenu.
 *
 * <p>Correspond à la table {@code ratings}. La contrainte d'unicité
 * sur (profile_id, content_id) limite à un avis par profil par contenu.
 * Score entre 1 et 10. Quand un avis est sauvegardé, la moyenne sur
 * le contenu est recalculée via {@link com.primevideo.repository.RatingRepository}.</p>
 */
@Entity
@Table(name = "ratings", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"profile_id", "content_id"})
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Rating {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_id", nullable = false)
    private Profile profile;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "content_id", nullable = false)
    private Content content;

    @Min(1) @Max(10)
    @Column(nullable = false)
    private Integer score;   // Note de 1 à 10

    @Column(columnDefinition = "TEXT")
    private String comment;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
