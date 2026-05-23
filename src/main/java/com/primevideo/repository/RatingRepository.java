package com.primevideo.repository;

import com.primevideo.entity.Rating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

/**
 * Accès à la table {@code ratings} — notes et avis des utilisateurs.
 */
@Repository
public interface RatingRepository extends JpaRepository<Rating, Long> {

    /** Retourne tous les avis d'un contenu, du plus récent au plus ancien. */
    List<Rating> findByContentIdOrderByCreatedAtDesc(Long contentId);

    /** Retrouve l'avis laissé par un profil spécifique sur un contenu (pour modification). */
    Optional<Rating> findByProfileIdAndContentId(Long profileId, Long contentId);

    /**
     * Calcule la note moyenne d'un contenu.
     * SQL JPQL : {@code SELECT AVG(r.score) FROM Rating r WHERE r.content.id = ?}
     * Retourne null si aucun avis n'existe encore.
     */
    @Query("SELECT AVG(r.score) FROM Rating r WHERE r.content.id = :contentId")
    Double findAverageScoreByContentId(@Param("contentId") Long contentId);

    /** Compte le nombre total d'avis pour afficher "(24 000 avis)" sur la fiche. */
    long countByContentId(Long contentId);
}
