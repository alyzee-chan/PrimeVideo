package com.primevideo.controller;

import com.primevideo.dto.response.SubscriptionPlanDTO;
import com.primevideo.dto.response.UserSubscriptionDTO;
import com.primevideo.entity.Subscription;
import com.primevideo.entity.User;
import com.primevideo.repository.UserRepository;
import com.primevideo.service.subscription.SubscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class SubscriptionController {

    private final SubscriptionService subscriptionService;
    private final UserRepository userRepository;

    // ── Pages Thymeleaf ───────────────────────────────────────────────────

    @GetMapping("/subscriptions/plans")
    public String plansPage(Model model) {
        model.addAttribute("plans", subscriptionService.getAvailablePlans());
        return "subscription/plans";
    }

    @GetMapping("/subscriptions/current")
    public String currentSubscriptionPage(java.security.Principal principal, Model model) {
        if (principal == null) return "redirect:/auth/login";
        User user = userRepository.findByEmailIgnoreCase(principal.getName())
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable (" + principal.getName() + ")"));
        model.addAttribute("subscription", subscriptionService.getCurrentSubscription(user.getId()).orElse(null));
        return "subscription/current";
    }

    // ── API REST ──────────────────────────────────────────────────────────

    @GetMapping("/api/subscriptions/plans")
    @ResponseBody
    public ResponseEntity<List<SubscriptionPlanDTO>> apiGetPlans() {
        return ResponseEntity.ok(subscriptionService.getAvailablePlans());
    }

    @PostMapping("/api/subscriptions")
    @ResponseBody
    public ResponseEntity<UserSubscriptionDTO> apiSubscribe(
            @RequestParam Subscription.Plan plan,
            java.security.Principal principal) {
        if (principal == null) return ResponseEntity.status(401).build();
        User user = userRepository.findByEmailIgnoreCase(principal.getName())
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable (" + principal.getName() + ")"));
        return ResponseEntity.ok(subscriptionService.subscribe(user, plan));
    }

    @PostMapping("/subscriptions/subscribe")
    public String subscribeForm(@RequestParam Subscription.Plan plan) {
        return "redirect:/payment/checkout?plan=" + plan.name();
    }

    @DeleteMapping("/api/subscriptions/current")
    @ResponseBody
    public ResponseEntity<Void> apiCancel(java.security.Principal principal) {
        if (principal == null) return ResponseEntity.status(401).build();
        User user = userRepository.findByEmailIgnoreCase(principal.getName())
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable (" + principal.getName() + ")"));
        subscriptionService.cancelSubscription(user.getId());
        return ResponseEntity.noContent().build();
    }
}
