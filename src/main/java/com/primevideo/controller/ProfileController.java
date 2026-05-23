package com.primevideo.controller;

import com.primevideo.entity.Profile;
import com.primevideo.service.ProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

@Controller
@RequestMapping("/profiles")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    @GetMapping("/api/list")
    @ResponseBody
    public ResponseEntity<List<Profile>> getProfilesApi(Principal principal) {
        if (principal == null) return ResponseEntity.status(401).build();
        return ResponseEntity.ok(profileService.getProfiles(principal.getName()));
    }

    /**
     * Affiche l'écran "Qui regarde ?" (Who's watching?)
     */
    @GetMapping("/select")
    public String showProfileSelection(Principal principal, Model model) {
        if (principal == null) return "redirect:/auth/login";
        model.addAttribute("profiles", profileService.getProfiles(principal.getName()));
        return "profiles/select";
    }

    /**
     * Sélectionne un profil. Si un PIN est configuré, redirige vers la vérification.
     */
    @GetMapping("/select/{id}")
    public String selectProfile(@PathVariable Long id, HttpSession session, Model model) {
        Profile profile = profileService.getById(id);
        
        if (profile.getPin() != null && !profile.getPin().isBlank()) {
            return "redirect:/profiles/verify-pin/" + id;
        }
        
        session.setAttribute("selectedProfile", profile);
        return "redirect:/home";
    }

    /**
     * Affiche la page de saisie du PIN.
     */
    @GetMapping("/verify-pin/{id}")
    public String showPinVerify(@PathVariable Long id, Model model) {
        model.addAttribute("profileId", id);
        model.addAttribute("profile", profileService.getById(id));
        return "profiles/verify-pin";
    }

    /**
     * Vérifie le PIN et active la session.
     */
    @PostMapping("/verify-pin/{id}")
    public String verifyPinAndSelect(@PathVariable Long id, @RequestParam String pin, HttpSession session, Model model) {
        if (profileService.verifyPin(id, pin)) {
            session.setAttribute("selectedProfile", profileService.getById(id));
            return "redirect:/home";
        }
        model.addAttribute("error", "Code PIN incorrect");
        model.addAttribute("profileId", id);
        model.addAttribute("profile", profileService.getById(id));
        return "profiles/verify-pin";
    }

    /**
     * Affiche le formulaire de création de profil.
     */
    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("profile", new Profile());
        return "profiles/create";
    }

    /**
     * Gère la soumission du formulaire de création.
     */
    @PostMapping("/create")
    public String createProfileForm(Principal principal, @ModelAttribute Profile profile) {
        if (principal == null) return "redirect:/auth/login";
        
        // Sécurité : Valeurs par défaut si non renseignées dans le formulaire
        if (profile.getAgeRating() == null) profile.setAgeRating(Profile.AgeRating.ALL);
        if (profile.getLanguage() == null) profile.setLanguage("fr");
        if (profile.getIsKids() == null) profile.setIsKids(false);
        
        profileService.createProfile(principal.getName(), profile);
        return "redirect:/profiles/select";
    }

    @PostMapping("/api")
    @ResponseBody
    public ResponseEntity<Profile> createProfileApi(Principal principal, @RequestBody Profile profile) {
        if (principal == null) return ResponseEntity.status(401).build();
        
        // Sécurité : Valeurs par défaut
        if (profile.getAgeRating() == null) profile.setAgeRating(Profile.AgeRating.ALL);
        if (profile.getLanguage() == null) profile.setLanguage("fr");
        if (profile.getIsKids() == null) profile.setIsKids(false);
        
        return ResponseEntity.ok(profileService.createProfile(principal.getName(), profile));
    }

    @PutMapping("/api/{id}")
    @ResponseBody
    public ResponseEntity<Profile> updateProfile(@PathVariable Long id, @RequestBody Profile profile) {
        return ResponseEntity.ok(profileService.updateProfile(id, profile));
    }

    @DeleteMapping("/api/{id}")
    @ResponseBody
    public ResponseEntity<Void> deleteProfileApi(@PathVariable Long id) {
        profileService.deleteProfile(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Gère la suppression du profil depuis le formulaire web.
     */
    @PostMapping("/delete/{id}")
    public String deleteProfileForm(@PathVariable Long id) {
        profileService.deleteProfile(id);
        return "redirect:/profiles/select";
    }

    @PostMapping("/api/{id}/verify-pin")
    @ResponseBody
    public ResponseEntity<Boolean> verifyPin(@PathVariable Long id, @RequestBody String pin) {
        return ResponseEntity.ok(profileService.verifyPin(id, pin));
    }

    /**
     * Affiche le formulaire d'édition de profil.
     */
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        model.addAttribute("profile", profileService.getById(id));
        return "profiles/edit";
    }

    /**
     * Gère la mise à jour du profil.
     */
    @PostMapping("/edit/{id}")
    public String updateProfileForm(@PathVariable Long id, @ModelAttribute Profile profile) {
        profileService.updateProfile(id, profile);
        return "redirect:/profiles/select";
    }
}
