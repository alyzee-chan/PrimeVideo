package com.primevideo.service.payment;

import com.primevideo.dto.request.PaymentRequest;
import com.primevideo.dto.response.PaymentResponse;
import com.primevideo.entity.Payment;
import com.primevideo.entity.User;
import com.primevideo.exception.PaymentException;
import com.primevideo.repository.PaymentRepository;
import com.primevideo.service.MailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final BillingService billingService;
    private final MailService mailService;

    /**
     * Point d'entrée principal — traite un paiement selon la méthode choisie.
     * La validation des champs est déjà faite par @Valid dans le controller.
     * Ici on ajoute les validations métier (cohérence entre méthode et champs).
     */
    @Transactional
    public PaymentResponse processPayment(PaymentRequest request, User user) {
        log.info("Traitement paiement — méthode: {}, montant: {} {}",
                request.getPaymentMethod(), request.getAmount(), request.getCurrency());

        // ── Validation métier niveau service ──────────────────────────────

        // Règle absolue : montant positif (double vérification en plus de @Valid)
        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new PaymentException("Le montant doit être strictement positif");
        }

        // Vérification des champs requis selon la méthode
        validatePaymentFields(request);

        // ── Traitement selon la méthode ───────────────────────────────────
        PaymentResponse response = switch (request.getPaymentMethod()) {
            case CARD           -> processCardPayment(request, user);
            case PAYPAL         -> processPayPalPayment(request, user);
            case MOBILE_MONEY_MOMO -> processMoMoPayment(request, user);
            case ORANGE_MONEY   -> processOrangeMoneyPayment(request, user);
        };

        // ── Envoi de l'email de confirmation ─────────────────────────────
        if (response.getStatus() == com.primevideo.entity.Payment.PaymentStatus.SUCCESS) {
            try {
                mailService.sendPaymentConfirmationEmail(
                    user.getEmail(),
                    request.getDescription(),
                    request.getAmount().doubleValue(),
                    request.getCurrency(),
                    response.getTransactionId()
                );
            } catch (Exception e) {
                log.error("Erreur lors de l'envoi de l'email de confirmation : {}", e.getMessage());
            }
        }

        return response;
    }

    // ────────────────────────────────────────────────────────────────────────
    // CARTE BANCAIRE
    // ────────────────────────────────────────────────────────────────────────
    private PaymentResponse processCardPayment(PaymentRequest request, User user) {
        String cardNumber = request.getCardNumber().replaceAll("\\s", "");

        // Validation algorithme de Luhn (vérifie que le numéro de carte est valide)
        if (!isValidLuhn(cardNumber)) {
            throw new PaymentException("Numéro de carte bancaire invalide");
        }

        // Vérification date d'expiration
        if (!isCardNotExpired(request.getExpiryDate())) {
            throw new PaymentException("La carte bancaire est expirée");
        }

        // En production, ici on appellerait Stripe, PayDunya, etc.
        // On simule une transaction réussie
        String transactionId = "CARD-" + UUID.randomUUID().toString().toUpperCase();

        Payment payment = Payment.builder()
                .user(user)
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .paymentMethod(Payment.PaymentMethod.CARD)
                .status(Payment.PaymentStatus.SUCCESS)
                .transactionId(transactionId)
                .cardLastFour(cardNumber.substring(cardNumber.length() - 4))
                .cardBrand(detectCardBrand(cardNumber))
                .description(request.getDescription())
                .paidAt(LocalDateTime.now())
                .build();

        Payment saved = paymentRepository.save(payment);
        billingService.generateInvoice(saved);
        log.info("Paiement carte réussi — transaction: {}", transactionId);

        return toResponse(saved, "Paiement par carte accepté avec succès");
    }

    // ────────────────────────────────────────────────────────────────────────
    // PAYPAL
    // ────────────────────────────────────────────────────────────────────────
    private PaymentResponse processPayPalPayment(PaymentRequest request, User user) {
        String transactionId = "PP-" + UUID.randomUUID().toString().toUpperCase();

        Payment payment = Payment.builder()
                .user(user)
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .paymentMethod(Payment.PaymentMethod.PAYPAL)
                .status(Payment.PaymentStatus.SUCCESS)
                .transactionId(transactionId)
                .paypalEmail(request.getPaypalEmail())
                .description(request.getDescription())
                .paidAt(LocalDateTime.now())
                .build();

        Payment saved = paymentRepository.save(payment);
        billingService.generateInvoice(saved);
        log.info("Paiement PayPal réussi — email: {}, transaction: {}",
                request.getPaypalEmail(), transactionId);

        return toResponse(saved, "Paiement PayPal accepté avec succès");
    }

    // ────────────────────────────────────────────────────────────────────────
    // MOBILE MONEY (MTN MoMo)
    // ────────────────────────────────────────────────────────────────────────
    private PaymentResponse processMoMoPayment(PaymentRequest request, User user) {
        // Validation numéro MoMo (commence par 6)
        String phone = request.getPhoneNumber();
        if (phone == null || !phone.matches("^6[0-9]{8}$")) {
            throw new PaymentException("Numéro MoMo invalide. Format attendu : 6XXXXXXXX");
        }

        String transactionId = "MOMO-" + UUID.randomUUID().toString().toUpperCase();

        Payment payment = Payment.builder()
                .user(user)
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .paymentMethod(Payment.PaymentMethod.MOBILE_MONEY_MOMO)
                .status(Payment.PaymentStatus.SUCCESS)
                .transactionId(transactionId)
                .phoneNumber(phone)
                .description(request.getDescription())
                .paidAt(LocalDateTime.now())
                .build();

        Payment saved = paymentRepository.save(payment);
        billingService.generateInvoice(saved);
        log.info("Paiement MoMo réussi — téléphone: {}, transaction: {}", phone, transactionId);

        return toResponse(saved, "Simulation USSD : [MTN MoMo] Un message de confirmation a été envoyé au " + phone + ". Veuillez composer le *126# pour valider le paiement de " + request.getAmount() + " " + request.getCurrency());
    }

    // ────────────────────────────────────────────────────────────────────────
    // ORANGE MONEY
    // ────────────────────────────────────────────────────────────────────────
    private PaymentResponse processOrangeMoneyPayment(PaymentRequest request, User user) {
        // Validation numéro Orange (commence par 6)
        String phone = request.getPhoneNumber();
        if (phone == null || !phone.matches("^6[0-9]{8}$")) {
            throw new PaymentException("Numéro Orange Money invalide. Format attendu : 6XXXXXXXX");
        }

        String transactionId = "OM-" + UUID.randomUUID().toString().toUpperCase();

        Payment payment = Payment.builder()
                .user(user)
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .paymentMethod(Payment.PaymentMethod.ORANGE_MONEY)
                .status(Payment.PaymentStatus.SUCCESS)
                .transactionId(transactionId)
                .phoneNumber(phone)
                .description(request.getDescription())
                .paidAt(LocalDateTime.now())
                .build();

        Payment saved = paymentRepository.save(payment);
        billingService.generateInvoice(saved);
        log.info("Paiement Orange Money réussi — téléphone: {}, transaction: {}", phone, transactionId);

        return toResponse(saved, "Simulation USSD : [Orange Money] Une demande de retrait de " + request.getAmount() + " " + request.getCurrency() + " a été envoyée au " + phone + ". Composez le #150*50# pour valider.");
    }

    // ────────────────────────────────────────────────────────────────────────
    // MÉTHODES UTILITAIRES
    // ────────────────────────────────────────────────────────────────────────

    private void validatePaymentFields(PaymentRequest request) {
        switch (request.getPaymentMethod()) {
            case CARD -> {
                if (request.getCardNumber() == null || request.getCardNumber().isBlank())
                    throw new PaymentException("Le numéro de carte est obligatoire");
                if (request.getExpiryDate() == null || request.getExpiryDate().isBlank())
                    throw new PaymentException("La date d'expiration est obligatoire");
                if (request.getCvv() == null || request.getCvv().isBlank())
                    throw new PaymentException("Le CVV est obligatoire");
            }
            case PAYPAL -> {
                if (request.getPaypalEmail() == null || request.getPaypalEmail().isBlank())
                    throw new PaymentException("L'email PayPal est obligatoire");
            }
            case MOBILE_MONEY_MOMO, ORANGE_MONEY -> {
                if (request.getPhoneNumber() == null || request.getPhoneNumber().isBlank())
                    throw new PaymentException("Le numéro de téléphone est obligatoire");
            }
        }
    }

    /** Algorithme de Luhn — vérifie la validité mathématique d'un numéro de carte */
    private boolean isValidLuhn(String number) {
        int sum = 0;
        boolean alternate = false;
        for (int i = number.length() - 1; i >= 0; i--) {
            if (!Character.isDigit(number.charAt(i))) return false;
            int n = number.charAt(i) - '0';
            if (alternate) {
                n *= 2;
                if (n > 9) n -= 9;
            }
            sum += n;
            alternate = !alternate;
        }
        return (sum % 10 == 0) && number.length() >= 13;
    }

    /** Vérifie que la carte n'est pas expirée */
    private boolean isCardNotExpired(String expiryDate) {
        try {
            String[] parts = expiryDate.split("/");
            int month = Integer.parseInt(parts[0]);
            int year = 2000 + Integer.parseInt(parts[1]);
            LocalDateTime expiry = LocalDateTime.of(year, month, 1, 23, 59, 59)
                    .withDayOfMonth(java.time.YearMonth.of(year, month).lengthOfMonth());
            return LocalDateTime.now().isBefore(expiry);
        } catch (Exception e) {
            return false;
        }
    }

    /** Détecte la marque de la carte selon son préfixe */
    private String detectCardBrand(String number) {
        if (number.startsWith("4")) return "VISA";
        if (number.matches("^5[1-5].*")) return "MASTERCARD";
        if (number.startsWith("34") || number.startsWith("37")) return "AMEX";
        return "UNKNOWN";
    }

    private PaymentResponse toResponse(Payment payment, String message) {
        return PaymentResponse.builder()
                .id(payment.getId())
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .paymentMethod(payment.getPaymentMethod())
                .status(payment.getStatus())
                .transactionId(payment.getTransactionId())
                .cardLastFour(payment.getCardLastFour())
                .cardBrand(payment.getCardBrand())
                .phoneNumber(payment.getPhoneNumber())
                .paypalEmail(payment.getPaypalEmail())
                .description(payment.getDescription())
                .paidAt(payment.getPaidAt())
                .createdAt(payment.getCreatedAt())
                .message(message)
                .build();
    }

    public List<PaymentResponse> getUserPayments(Long userId) {
        return paymentRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(p -> toResponse(p, null))
                .collect(Collectors.toList());
    }
}
