package com.primevideo.repository;

import com.primevideo.entity.WatchlistItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

/**
 * Accès à la table {@code watchlist_items} — liste "À regarder plus tard".
 */
@Repository
public interface WatchlistItemRepository extends JpaRepository<WatchlistItem, Long> {

    /** Retourne toute la watchlist d'un profil, du plus récent ajout au plus ancien. */
    List<WatchlistItem> findByProfileIdOrderByAddedAtDesc(Long profileId);

    /** Retrouve une entrée spécifique (pour vérifier si un contenu est déjà dans la liste). */
    Optional<WatchlistItem> findByProfileIdAndContentId(Long profileId, Long contentId);

    /** Vérifie rapidement si un contenu est dans la watchlist — pour afficher le bouton ✓ ou +. */
    boolean existsByProfileIdAndContentId(Long profileId, Long contentId);

    /** Supprime un contenu de la watchlist (quand l'utilisateur clique sur "Retirer"). */
    void deleteByProfileIdAndContentId(Long profileId, Long contentId);
}
