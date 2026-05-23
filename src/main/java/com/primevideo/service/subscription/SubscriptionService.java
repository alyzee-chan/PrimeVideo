package com.primevideo.service.subscription;

import com.primevideo.dto.response.SubscriptionPlanDTO;
import com.primevideo.dto.response.UserSubscriptionDTO;
import com.primevideo.entity.Subscription;
import com.primevideo.entity.User;
import com.primevideo.repository.SubscriptionRepository;
import com.primevideo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final UserRepository userRepository;
    private final com.primevideo.service.NotificationService notificationService;

    /**
     * Retourne la liste des plans disponibles (Hardcoded pour simulation).
     */
    public List<SubscriptionPlanDTO> getAvailablePlans() {
        List<SubscriptionPlanDTO> plans = new ArrayList<>();

        plans.add(SubscriptionPlanDTO.builder()
                .id("plan_prime_monthly")
                .name("Prime Video Mensuel")
                .type("PRIME_MONTHLY")
                .price(new BigDecimal("4500"))
                .currency("CFA")
                .billingPeriod("MONTHLY")
                .trialDays(30)
                .features(List.of("Streaming illimité", "Qualité HD", "3 écrans simultanés"))
                .maxSimultaneousStreams(3)
                .maxDownloads(25)
                .build());

        plans.add(SubscriptionPlanDTO.builder()
                .id("plan_prime_annual")
                .name("Prime Video Annuel")
                .type("PRIME_ANNUAL")
                .price(new BigDecimal("45000"))
                .currency("CFA")
                .billingPeriod("ANNUAL")
                .trialDays(30)
                .features(List.of("Streaming illimité", "Qualité 4K Ultra HD", "3 écrans simultanés", "2 mois offerts"))
                .maxSimultaneousStreams(3)
                .maxDownloads(25)
                .build());

        plans.add(SubscriptionPlanDTO.builder()
                .id("plan_prime_video")
                .name("Prime Video Only")
                .type("PRIME_VIDEO_ONLY")
                .price(new BigDecimal("3500"))
                .currency("CFA")
                .billingPeriod("MONTHLY")
                .trialDays(30)
                .features(List.of("Streaming illimité", "Qualité HD", "Accès vidéo uniquement"))
                .maxSimultaneousStreams(2)
                .maxDownloads(15)
                .build());

        plans.add(SubscriptionPlanDTO.builder()
                .id("plan_student")
                .name("Prime Student")
                .type("STUDENT")
                .price(new BigDecimal("2300"))
                .currency("CFA")
                .billingPeriod("MONTHLY")
                .trialDays(90)
                .features(List.of("Streaming illimité", "Qualité HD", "Offre réservée aux étudiants"))
                .maxSimultaneousStreams(2)
                .maxDownloads(10)
                .build());

        plans.add(SubscriptionPlanDTO.builder()
                .id("plan_addon")
                .name("Channel Add-on")
                .type("CHANNEL_ADDON")
                .price(new BigDecimal("1500"))
                .currency("CFA")
                .billingPeriod("MONTHLY")
                .trialDays(7)
                .features(List.of("Accès à une chaîne spécifique"))
                .maxSimultaneousStreams(1)
                .maxDownloads(5)
                .build());

        return plans;
    }

    public Optional<UserSubscriptionDTO> getCurrentSubscription(Long userId) {
        return subscriptionRepository.findByUserIdAndStatus(userId, Subscription.SubscriptionStatus.ACTIVE)
                .map(this::toDTO);
    }

    @Transactional
    public UserSubscriptionDTO subscribe(User user, Subscription.Plan planType) {
        // Désactiver l'abonnement actuel s'il existe
        subscriptionRepository.findByUserIdAndStatus(user.getId(), Subscription.SubscriptionStatus.ACTIVE)
                .ifPresent(s -> {
                    s.setStatus(Subscription.SubscriptionStatus.EXPIRED);
                    subscriptionRepository.save(s);
                });

        SubscriptionPlanDTO planData = getAvailablePlans().stream()
                .filter(p -> p.getType().equals(planType.name()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Plan non trouvé"));

        Subscription subscription = Subscription.builder()
                .user(user)
                .plan(planType)
                .status(Subscription.SubscriptionStatus.ACTIVE)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusMonths(planData.getBillingPeriod().equals("MONTHLY") ? 1 : 12))
                .priceAmount(planData.getPrice())
                .priceCurrency(planData.getCurrency())
                .autoRenew(true)
                .build();

        Subscription saved = subscriptionRepository.save(subscription);
        
        // Mettre à jour le statut Prime de l'utilisateur
        user.setIsPrime(true);
        userRepository.save(user);

        // Notification asynchrone
        notificationService.sendSubscriptionConfirmation(user, saved);

        return toDTO(saved);
    }

    @Transactional
    public void cancelSubscription(Long userId) {
        subscriptionRepository.findByUserIdAndStatus(userId, Subscription.SubscriptionStatus.ACTIVE)
                .ifPresent(s -> {
                    s.setStatus(Subscription.SubscriptionStatus.CANCELLED);
                    s.setAutoRenew(false);
                    subscriptionRepository.save(s);
                });
    }

    /**
     * Renouvelle l'abonnement actuel.
     * Si renouvelé > 7 jours avant la fin : réduction et points de fidélité.
     */
    @Transactional
    public UserSubscriptionDTO renewSubscription(Long userId) {
        Subscription current = subscriptionRepository.findByUserIdAndStatus(userId, Subscription.SubscriptionStatus.ACTIVE)
                .orElseThrow(() -> new RuntimeException("Aucun abonnement actif à renouveler"));

        User user = current.getUser();
        LocalDate now = LocalDate.now();
        LocalDate expiry = current.getEndDate();

        SubscriptionPlanDTO planData = getAvailablePlans().stream()
                .filter(p -> p.getType().equals(current.getPlan().name()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Plan non trouvé"));

        // Calcul fidélité : si > 7 jours avant expiration
        int pointsEarned = 50; // Base
        if (now.isBefore(expiry.minusDays(7))) {
            pointsEarned += 100; // Bonus anticipation
            log.info("Renouvellement anticipé pour {}, bonus fidélité octroyé.", user.getEmail());
        }

        // Mise à jour points utilisateur
        user.setLoyaltyPoints(user.getLoyaltyPoints() + pointsEarned);
        userRepository.save(user);

        // Extension de la date (on ajoute à la date d'expiration actuelle pour ne pas perdre de jours)
        current.setEndDate(expiry.plusMonths(planData.getBillingPeriod().equals("MONTHLY") ? 1 : 12));
        current.setLastReminderSentAt(null); // Reset reminder tracking
        
        Subscription saved = subscriptionRepository.save(current);
        
        notificationService.sendRenewalSuccessEmail(user, saved, pointsEarned);

        return toDTO(saved);
    }

    /**
     * Tâche planifiée (simulation) pour envoyer les rappels d'expiration.
     * Dans un vrai projet, on utiliserait @Scheduled(cron = "0 0 9 * * *")
     */
    @Transactional
    public void checkAndNotifyExpiringSubscriptions() {
        // Logique simplifiée : on cherche les abonnements finissant dans moins de 3 jours 
        // et qui n'ont pas encore reçu de rappel.
        LocalDate threeDaysFromNow = LocalDate.now().plusDays(3);
        
        // On récupère tout et on filtre (pour l'exemple)
        subscriptionRepository.findAll().stream()
            .filter(s -> s.getStatus() == Subscription.SubscriptionStatus.ACTIVE)
            .filter(s -> s.getEndDate().isBefore(threeDaysFromNow))
            .filter(s -> s.getLastReminderSentAt() == null)
            .forEach(s -> {
                notificationService.sendSubscriptionExpiryReminder(s.getUser(), s);
                notificationService.createNotification(s.getUser(), "Abonnement expire bientôt", "Votre abonnement se termine le " + s.getEndDate() + ". Renouvelez maintenant pour gagner des points !", com.primevideo.entity.Notification.NotificationType.REMINDER, "/subscription/current");
                s.setLastReminderSentAt(java.time.LocalDateTime.now());
                subscriptionRepository.save(s);
            });
    }

    private UserSubscriptionDTO toDTO(Subscription s) {
        SubscriptionPlanDTO planDTO = getAvailablePlans().stream()
                .filter(p -> p.getType().equals(s.getPlan().name()))
                .findFirst()
                .orElse(null);

        return UserSubscriptionDTO.builder()
                .id(s.getId())
                .plan(planDTO)
                .status(s.getStatus().name())
                .startedAt(s.getStartDate().atStartOfDay())
                .currentPeriodEnd(s.getEndDate().atStartOfDay())
                .isAutoRenew(s.getAutoRenew())
                .build();
    }
}
