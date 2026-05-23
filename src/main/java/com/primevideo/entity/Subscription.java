package com.primevideo.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Abonnement d'un utilisateur à la plateforme.
 *
 * <p>Correspond à la table {@code subscriptions}. Un utilisateur peut avoir
 * plusieurs abonnements (historique), mais un seul ACTIVE à la fois.
 * Le prix est stocké en {@link java.math.BigDecimal} pour la précision comptable.</p>
 */
@Entity
@Table(name = "subscriptions")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Subscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private Plan plan;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private SubscriptionStatus status = SubscriptionStatus.ACTIVE;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "price_amount", precision = 10, scale = 2, nullable = false)
    private BigDecimal priceAmount;

    @Column(name = "price_currency", length = 3, nullable = false)
    @Builder.Default
    private String priceCurrency = "CFA";

    @Column(name = "auto_renew")
    @Builder.Default
    private Boolean autoRenew = true;

    /** Date du dernier email de rappel d'expiration envoyé. */
    @Column(name = "last_reminder_sent_at")
    private LocalDateTime lastReminderSentAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public enum Plan {
        PRIME_MONTHLY, PRIME_ANNUAL, PRIME_VIDEO_ONLY, STUDENT, CHANNEL_ADDON
    }

    public enum SubscriptionStatus {
        ACTIVE, TRIAL, PAST_DUE, CANCELLED, EXPIRED
    }
}
