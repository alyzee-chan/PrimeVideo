package com.primevideo.dto.request;

import com.primevideo.entity.Payment;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

/**
 * DTO pour les requêtes de paiement — reçu depuis le formulaire Thymeleaf ou l'API REST.
 *
 * <p>Les annotations Bean Validation sont vérifiées automatiquement par {@code @Valid} dans
 * {@link com.primevideo.controller.PaymentController}. En cas d'erreur, Spring retourne
 * automatiquement HTTP 400 avec les messages définis ici.</p>
 *
 * <p><strong>Règle critique :</strong> {@code amount} utilise {@link BigDecimal} (jamais {@code double}),
 * car en Java {@code 0.1 + 0.2 = 0.30000000000000004} — les flottants sont imprécis pour l'argent.</p>
 *
 * <p>Les champs carte/téléphone/paypal sont conditionnels selon {@code paymentMethod}.
 * La validation de cohérence (ex : si CARD → cardNumber obligatoire) est dans
 * {@link com.primevideo.service.payment.PaymentService#validatePaymentFields}.</p>
 */
@Data
public class PaymentRequest {

    // ─── Montant ───────────────────────────────────────────────────
    /** Montant à débiter. Doit être strictement positif (>= 0.01). Jamais négatif, jamais zéro. */
    @NotNull(message = "Le montant est obligatoire")
    @DecimalMin(value = "0.01", message = "Le montant minimum est de 0.01")
    @DecimalMax(value = "99999.99", message = "Le montant maximum est de 99 999.99")
    @Digits(integer = 5, fraction = 2, message = "Format de montant invalide (max 5 entiers, 2 décimales)")
    private BigDecimal amount;

    // On n'accepte jamais de montant négatif (règle métier critique)
    // La validation @DecimalMin("0.01") garantit amount > 0 TOUJOURS

    @NotBlank(message = "La devise est obligatoire")
    @Pattern(regexp = "^[A-Z]{3}$", message = "Devise invalide (format ISO 4217, ex: EUR, XAF, USD)")
    private String currency;

    @NotNull(message = "La méthode de paiement est obligatoire")
    private Payment.PaymentMethod paymentMethod;

    // ─── Carte bancaire ─────────────────────────────────────────────
    // Requis uniquement si paymentMethod == CARD (validé dans le service)
    private String cardNumber;
    private String expiryDate;
    private String cvv;

    @Size(max = 150, message = "Nom sur la carte trop long")
    private String cardHolderName;

    // ─── Mobile Money (MoMo / Orange Money) ─────────────────────────
    private String phoneNumber;


    // ─── PayPal ─────────────────────────────────────────────────────
    @Email(message = "Email PayPal invalide")
    @Size(max = 150)
    private String paypalEmail;


    // ─── Référence (abonnement) ──────────────────────────────────────
    private Long subscriptionPlanId;

    @Size(max = 255)
    private String description;
}
