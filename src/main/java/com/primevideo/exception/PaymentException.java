package com.primevideo.exception;

/**
 * Exception levée quand un paiement est rejeté pour une raison métier.
 *
 * <p>Exemples : carte expirée, numéro de carte invalide (Luhn échoue),
 * numéro Mobile Money invalide, montant négatif détecté au niveau du service.</p>
 *
 * <p>Interceptée par {@link GlobalExceptionHandler} → retourne HTTP 422 Unprocessable Entity
 * (différent de 400 car les données sont techniquement valides, mais le paiement est refusé).</p>
 */
public class PaymentException extends RuntimeException {
    public PaymentException(String message) { super(message); }
}
