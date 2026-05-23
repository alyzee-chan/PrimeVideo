package com.primevideo.repository;

import com.primevideo.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

/**
 * Accès à la table {@code payments} en base de données.
 *
 * <p>Toutes les transactions (carte, PayPal, MoMo, Orange Money)
 * sont stockées dans cette table et accessibles via ce repository.</p>
 */
@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    /**
     * Retourne l'historique des paiements d'un utilisateur, du plus récent au plus ancien.
     * Utilisé sur la page /payment/history.
     *
     * <p>SQL généré : {@code SELECT * FROM payments WHERE user_id = ? ORDER BY created_at DESC}</p>
     */
    List<Payment> findByUserIdOrderByCreatedAtDesc(Long userId);

    /**
     * Recherche un paiement par son identifiant de transaction externe.
     * Utile pour vérifier si une transaction PayPal/MoMo a déjà été traitée (anti-doublon).
     *
     * <p>SQL généré : {@code SELECT * FROM payments WHERE transaction_id = ?}</p>
     */
    Optional<Payment> findByTransactionId(String transactionId);

    /**
     * Retourne les paiements d'un utilisateur filtrés par statut.
     * Exemple : tous les paiements réussis, ou tous les paiements en attente.
     *
     * <p>SQL généré : {@code SELECT * FROM payments WHERE user_id = ? AND status = ?}</p>
     */
    List<Payment> findByUserIdAndStatus(Long userId, Payment.PaymentStatus status);

    /**
     * Retourne tous les paiements effectués entre deux dates.
     * Utile pour les rapports de ventes mensuels.
     */
    List<Payment> findByPaidAtBetweenAndStatus(java.time.LocalDateTime start, java.time.LocalDateTime end, Payment.PaymentStatus status);
}
