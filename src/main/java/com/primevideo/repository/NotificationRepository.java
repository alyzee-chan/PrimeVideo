package com.primevideo.repository;

import com.primevideo.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * Accès à la table {@code notifications} — notifications des utilisateurs.
 */
@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    /** Toutes les notifications d'un utilisateur, de la plus récente à la plus ancienne. */
    List<Notification> findByUserIdOrderByCreatedAtDesc(Long userId);

    /** Uniquement les notifications non lues — pour la page de notifications. */
    List<Notification> findByUserIdAndIsReadFalseOrderByCreatedAtDesc(Long userId);

    /**
     * Compte les notifications non lues — affiche le badge rouge sur la cloche (ex : "3").
     * SQL : {@code SELECT COUNT(*) FROM notifications WHERE user_id = ? AND is_read = 0}
     */
    long countByUserIdAndIsReadFalse(Long userId);
}
