package com.primevideo.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class MailService {

    private final JavaMailSender mailSender;

    public void sendEmail(String to, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("no-reply@primevideo.com");
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
            log.info("Email envoyé à : {}", to);
        } catch (Exception e) {
            log.error("Erreur lors de l'envoi de l'email à {} : {}", to, e.getMessage());
            // En développement, on ne bloque pas si l'envoi échoue
        }
    }

    public void sendVerificationEmail(String to, String token) {
        String subject = "Vérifiez votre adresse email - Prime Video";
        String body = "Cliquez sur le lien suivant pour vérifier votre compte : \n" +
                      "http://localhost:8080/api/auth/verify-email/" + token;
        sendEmail(to, subject, body);
    }

    public void sendPasswordResetEmail(String to, String token) {
        String subject = "Réinitialisation de votre mot de passe - Prime Video";
        String body = "Utilisez le code suivant pour réinitialiser votre mot de passe : " + token + "\n" +
                      "Ce code est valable 15 minutes.";
        sendEmail(to, subject, body);
    }

    public void sendPaymentConfirmationEmail(String to, String planName, double amount, String currency, String transactionId) {
        String subject = "Confirmation de votre abonnement Prime Video";
        String body = "Félicitations !\n\n" +
                      "Votre paiement pour le forfait " + planName + " a été accepté.\n" +
                      "Montant : " + amount + " " + currency + "\n" +
                      "ID de transaction : " + transactionId + "\n\n" +
                      "Vous pouvez maintenant profiter de tout notre catalogue sans limites.\n" +
                      "L'équipe Prime Video.";
        sendEmail(to, subject, body);
    }
}
