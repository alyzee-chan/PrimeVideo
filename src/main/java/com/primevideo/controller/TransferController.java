package com.primevideo.controller;

import com.primevideo.service.ProfileService;
import com.primevideo.service.subscription.SubscriptionService;
import com.primevideo.entity.User;
import com.primevideo.repository.UserRepository;
import com.primevideo.entity.Subscription;
import com.primevideo.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

@Controller
@RequestMapping("/transfer")
@RequiredArgsConstructor
public class TransferController {

    private final ProfileService profileService;
    private final SubscriptionService subscriptionService;
    private final UserRepository userRepository;
    private final SubscriptionRepository subscriptionRepository;

    @PostMapping("/profile/{id}")
    public String transferProfile(@PathVariable Long id, @RequestParam String targetEmail, RedirectAttributes ra) {
        try {
            profileService.transferProfile(id, targetEmail);
            ra.addFlashAttribute("successMsg", "Le profil a été transféré avec succès vers " + targetEmail);
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", "Erreur lors du transfert : " + e.getMessage());
        }
        return "redirect:/profiles/select";
    }

    @PostMapping("/subscription")
    public String transferSubscription(@RequestParam String targetEmail, Principal principal, RedirectAttributes ra) {
        try {
            User currentUser = userRepository.findByEmailIgnoreCase(principal.getName()).orElseThrow();
            Subscription currentSub = subscriptionRepository.findByUserIdAndStatus(currentUser.getId(), Subscription.SubscriptionStatus.ACTIVE)
                    .orElseThrow(() -> new RuntimeException("Aucun abonnement actif à transférer"));

            User targetUser = userRepository.findByEmailIgnoreCase(targetEmail.trim())
                    .orElseThrow(() -> new RuntimeException("Destinataire introuvable (" + targetEmail + ")"));

            // Désactiver l'abonnement actuel sur la cible si existe
            subscriptionRepository.findByUserIdAndStatus(targetUser.getId(), Subscription.SubscriptionStatus.ACTIVE)
                    .ifPresent(s -> {
                        s.setStatus(Subscription.SubscriptionStatus.CANCELLED);
                        subscriptionRepository.save(s);
                    });

            // Transférer
            currentSub.setUser(targetUser);
            subscriptionRepository.save(currentSub);
            
            // L'expéditeur n'est plus Prime
            currentUser.setIsPrime(false);
            userRepository.save(currentUser);

            ra.addFlashAttribute("successMsg", "Votre abonnement a été transféré avec succès à " + targetEmail);
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", "Erreur lors du transfert d'abonnement : " + e.getMessage());
        }
        return "redirect:/account/settings";
    }

    @PostMapping("/subscription/renew")
    public String renewSubscription(Principal principal, RedirectAttributes ra) {
        try {
            User user = userRepository.findByEmailIgnoreCase(principal.getName()).orElseThrow();
            subscriptionService.renewSubscription(user.getId());
            ra.addFlashAttribute("successMsg", "Votre abonnement a été renouvelé avec succès ! Points de fidélité ajoutés.");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", "Erreur lors du renouvellement : " + e.getMessage());
        }
        return "redirect:/home";
    }
}
