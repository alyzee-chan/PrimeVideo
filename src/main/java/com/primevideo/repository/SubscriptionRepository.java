package com.primevideo.repository;

import com.primevideo.entity.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

/**
 * Accès à la table {@code subscriptions} — abonnements des utilisateurs.
 */
@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    /**
     * Recherche l'abonnement actif d'un utilisateur.
     * Retourne un Optional vide si l'utilisateur n'a pas d'abonnement du statut demandé.
     * SQL : {@code SELECT * FROM subscriptions WHERE user_id = ? AND status = ?}
     */
    Optional<Subscription> findByUserIdAndStatus(Long userId, Subscription.SubscriptionStatus status);

    /** Retourne tout l'historique des abonnements d'un utilisateur. */
    List<Subscription> findByUserId(Long userId);

    /**
     * Vérifie rapidement si un utilisateur a un abonnement actif — sans charger l'objet.
     * Utilisé pour contrôler l'accès aux contenus premium.
     */
    boolean existsByUserIdAndStatus(Long userId, Subscription.SubscriptionStatus status);
}
