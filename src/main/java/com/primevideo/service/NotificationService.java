package com.primevideo.service;

import com.primevideo.entity.Subscription;
import com.primevideo.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final MailService mailService;
    private final com.primevideo.repository.NotificationRepository notificationRepository;

    @Async
    public void createNotification(User user, String title, String message, com.primevideo.entity.Notification.NotificationType type, String linkUrl) {
        com.primevideo.entity.Notification notif = com.primevideo.entity.Notification.builder()
                .user(user)
                .title(title)
                .message(message)
                .type(type)
                .linkUrl(linkUrl)
                .isRead(false)
                .build();
        notificationRepository.save(notif);
        log.info("Notification persistée pour {} : {}", user.getEmail(), title);
    }

    public java.util.List<com.primevideo.entity.Notification> getUnreadNotifications(Long userId) {
        return notificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId);
    }

    @org.springframework.transaction.annotation.Transactional
    public void markAllAsRead(Long userId) {
        notificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId).forEach(n -> {
            n.setIsRead(true);
            notificationRepository.save(n);
        });
    }

    @Async
    public void sendWelcomeEmail(User user) {
        log.info("Envoi de l'email de bienvenue à : {}", user.getEmail());
        String subject = "Bienvenue sur Prime Video, " + user.getFullName() + " !";
        String body = "Nous sommes ravis de vous compter parmi nous.\n" +
                      "Explorez des milliers de films et séries dès maintenant.";
        mailService.sendEmail(user.getEmail(), subject, body);
    }

    @Async
    public void sendSubscriptionConfirmation(User user, Subscription subscription) {
        log.info("Envoi confirmation abonnement à : {}", user.getEmail());
        mailService.sendPaymentConfirmationEmail(
                user.getEmail(),
                subscription.getPlan().name(),
                subscription.getPriceAmount().doubleValue(),
                subscription.getPriceCurrency(),
                "SUB-" + subscription.getId()
        );
        createNotification(user, "Abonnement activé", "Votre abonnement " + subscription.getPlan().name() + " est maintenant actif.", com.primevideo.entity.Notification.NotificationType.SUBSCRIPTION, "/subscription/current");
    }

    @Async
    public void sendNewDeviceAlert(User user, String deviceName, String location) {
        log.info("Envoi alerte nouvel appareil à : {}", user.getEmail());
        String subject = "Nouvelle connexion à votre compte Prime Video";
        String body = "Un nouvel appareil s'est connecté à votre compte :\n" +
                      "- Appareil : " + deviceName + "\n" +
                      "- Lieu : " + location + "\n\n" +
                      "Si ce n'était pas vous, veuillez sécuriser votre compte immédiatement.";
        mailService.sendEmail(user.getEmail(), subject, body);
    }

    @Async
    public void sendSubscriptionExpiryReminder(User user, Subscription sub) {
        log.info("Envoi rappel expiration abonnement à : {}", user.getEmail());
        String subject = "Votre abonnement Prime Video expire bientôt !";
        String body = "Bonjour " + user.getFullName() + ",\n\n" +
                      "Votre abonnement " + sub.getPlan().name() + " se termine le " + sub.getEndDate() + ".\n" +
                      "Renouvelez dès maintenant pour ne pas perdre l'accès à vos programmes préférés.\n\n" +
                      "🎁 Astuce : Renouvelez au moins 1 semaine à l'avance pour gagner des points de fidélité !";
        mailService.sendEmail(user.getEmail(), subject, body);
    }

    @Async
    public void sendRenewalSuccessEmail(User user, Subscription sub, int pointsEarned) {
        log.info("Envoi confirmation renouvellement à : {}", user.getEmail());
        String subject = "Abonnement renouvelé avec succès - Prime Video";
        String body = "Félicitations " + user.getFullName() + ",\n\n" +
                      "Votre abonnement a été renouvelé jusqu'au " + sub.getEndDate() + ".\n" +
                      "Vous avez gagné " + pointsEarned + " points de fidélité grâce à ce renouvellement.\n\n" +
                      "Merci de votre fidélité !";
        mailService.sendEmail(user.getEmail(), subject, body);
        createNotification(user, "Fidélité récompensée !", "Vous avez gagné " + pointsEarned + " points grâce à votre renouvellement.", com.primevideo.entity.Notification.NotificationType.REMINDER, "/account/settings");
    }
}
