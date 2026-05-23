package com.primevideo.controller;

import com.primevideo.entity.User;
import com.primevideo.repository.UserRepository;
import com.primevideo.service.DeviceService;
import com.primevideo.service.subscription.SubscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@Controller
@RequestMapping("/account")
@RequiredArgsConstructor
public class AccountController {

    private final UserRepository userRepository;
    private final DeviceService deviceService;
    private final SubscriptionService subscriptionService;

    @GetMapping("/settings")
    public String settings(Principal principal, Model model) {
        User user = userRepository.findByEmailIgnoreCase(principal.getName()).orElseThrow(() -> new RuntimeException("Utilisateur introuvable (" + principal.getName() + ")"));
        model.addAttribute("user", user);
        model.addAttribute("subscription", subscriptionService.getCurrentSubscription(user.getId()).orElse(null));
        return "profile/settings";
    }

    @PostMapping("/settings/update")
    public String updateSettings(@RequestParam String fullName, Principal principal) {
        User user = userRepository.findByEmailIgnoreCase(principal.getName()).orElseThrow(() -> new RuntimeException("Utilisateur introuvable (" + principal.getName() + ")"));
        user.setFullName(fullName);
        userRepository.save(user);
        return "redirect:/account/settings?updated=true";
    }

    @GetMapping("/devices")
    public String devices(Principal principal, Model model) {
        User user = userRepository.findByEmailIgnoreCase(principal.getName()).orElseThrow(() -> new RuntimeException("Utilisateur introuvable (" + principal.getName() + ")"));
        model.addAttribute("devices", deviceService.getUserDevices(user));
        return "profile/devices";
    }

    @PostMapping("/devices/remove/{deviceId}")
    public String removeDevice(Principal principal, @PathVariable String deviceId) {
        User user = userRepository.findByEmailIgnoreCase(principal.getName()).orElseThrow(() -> new RuntimeException("Utilisateur introuvable (" + principal.getName() + ")"));
        deviceService.unregisterDevice(user.getId(), deviceId);
        return "redirect:/account/devices?removed=true";
    }
}
