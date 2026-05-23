package com.primevideo.controller;

import com.primevideo.dto.request.LoginRequest;
import com.primevideo.dto.request.RegisterRequest;
import com.primevideo.dto.request.ForgotPasswordRequest;
import com.primevideo.dto.request.ResetPasswordRequest;
import com.primevideo.dto.request.RefreshTokenRequest;
import com.primevideo.dto.request.MfaChallengeRequest;
import com.primevideo.dto.request.MfaVerifyRequest;
import com.primevideo.dto.response.AuthResponse;
import com.primevideo.dto.response.MfaSetupResponse;
import com.primevideo.dto.response.UserDTO;
import com.primevideo.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

/**
 * Controller gérant l'authentification : inscription, connexion, déconnexion.
 *
 * <p>Ce controller est DOUBLE : il gère à la fois les pages web (Thymeleaf)
 * et les endpoints API REST (JSON). Les deux partagent la même logique métier
 * via {@link AuthService}.</p>
 *
 * <p>Routes gérées :</p>
 * <ul>
 *   <li>GET  /auth/login     → affiche la page de connexion</li>
 *   <li>POST /auth/login     → traité par Spring Security directement (cf. SecurityConfig)</li>
 *   <li>GET  /auth/register  → affiche le formulaire d'inscription</li>
 *   <li>POST /auth/register  → traite le formulaire d'inscription (Thymeleaf)</li>
 *   <li>GET  /auth/logout    → traité par Spring Security directement</li>
 *   <li>POST /api/auth/login    → connexion API REST (retourne du JSON)</li>
 *   <li>POST /api/auth/register → inscription API REST (retourne du JSON)</li>
 * </ul>
 *
 * <p>Différence entre {@code @Controller} et {@code @RestController} :</p>
 * <ul>
 *   <li>{@code @Controller} — les méthodes retournent le nom d'un template Thymeleaf par défaut</li>
 *   <li>{@code @ResponseBody} (ou {@code @RestController}) — retourne directement du JSON</li>
 * </ul>
 */
@Controller
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // ── Pages Thymeleaf ────────────────────────────────────────────────────

    /**
     * Affiche la page de connexion.
     *
     * <p>Les paramètres {@code error} et {@code logout} sont ajoutés automatiquement
     * par Spring Security dans l'URL après un échec de connexion ou une déconnexion.
     * On les détecte pour afficher le message approprié dans le template.</p>
     *
     * @param error   présent si la dernière tentative de connexion a échoué
     * @param logout  présent si l'utilisateur vient de se déconnecter
     * @param model   objet Thymeleaf pour passer des données au template HTML
     * @return le nom du template : {@code src/main/resources/templates/auth/login.html}
     */
    @GetMapping("/auth/login")
    public String loginPage(@RequestParam(required = false) String error,
                            @RequestParam(required = false) String logout,
                            @RequestParam(required = false) String resetSuccess,
                            Model model) {
        if (error != null)  model.addAttribute("errorMsg", "Email ou mot de passe incorrect");
        if (logout != null) model.addAttribute("logoutMsg", "Déconnexion réussie");
        if (resetSuccess != null) model.addAttribute("successMsg", "Votre mot de passe a été réinitialisé avec succès. Connectez-vous !");
        return "auth/login";
    }

    /**
     * Affiche le formulaire d'inscription.
     *
     * <p>On injecte un objet {@code RegisterRequest} vide dans le modèle Thymeleaf.
     * Cela permet au template d'utiliser {@code th:object="${registerRequest}"}
     * et d'afficher les erreurs de validation champ par champ.</p>
     */
    @GetMapping("/auth/register")
    public String registerPage(Model model) {
        model.addAttribute("registerRequest", new RegisterRequest());
        return "auth/register";
    }

    /**
     * Affiche la page de configuration MFA (QR Code).
     */
    @GetMapping("/auth/mfa/setup")
    public String mfaSetupPage(java.security.Principal principal, Model model) {
        MfaSetupResponse setup = authService.initMfaSetup(principal.getName());
        model.addAttribute("setup", setup);
        return "auth/mfa-setup";
    }

    /**
     * Affiche la page de saisie du code MFA (Challenge).
     */
    @GetMapping("/auth/mfa/challenge")
    public String mfaChallengePage(@RequestParam String mfaToken, Model model) {
        model.addAttribute("mfaToken", mfaToken);
        return "auth/mfa-challenge";
    }

    /**
     * Traite le formulaire d'inscription soumis depuis la page HTML.
     *
     * <p>{@code @Valid} déclenche la validation des contraintes définies dans {@link RegisterRequest}
     * (email valide, mot de passe 8+ caractères, etc.).
     * Si des erreurs existent, {@code result.hasErrors()} est vrai et on ré-affiche le formulaire
     * avec les messages d'erreur — Thymeleaf les affiche via {@code th:errors="*{field}"}.</p>
     *
     * @param request     données du formulaire, déjà validées par {@code @Valid}
     * @param result      contient les éventuelles erreurs de validation
     * @param model       pour passer des données au template en cas d'erreur
     * @return redirection vers la page de login si succès, sinon ré-affichage du formulaire
     */
    @PostMapping("/auth/register")
    public String register(@Valid @ModelAttribute("registerRequest") RegisterRequest request,
                           BindingResult result, Model model) {
        // Si la validation @Valid a trouvé des erreurs → ré-afficher le formulaire avec les messages
        if (result.hasErrors()) {
            return "auth/register";
        }
        try {
            authService.register(request);
            // Redirection avec paramètre pour afficher un message de succès sur la page de login
            return "redirect:/auth/login?registered=true";
        } catch (Exception e) {
            // Erreur métier (ex : email déjà utilisé) → afficher le message dans le formulaire
            model.addAttribute("errorMsg", e.getMessage());
            return "auth/register";
        }
    }

    /**
     * Affiche la page de demande de réinitialisation de mot de passe.
     */
    @GetMapping("/auth/forgot-password")
    public String forgotPasswordPage(Model model) {
        model.addAttribute("forgotPasswordRequest", new ForgotPasswordRequest());
        return "auth/forgot-password";
    }

    /**
     * Traite la demande de réinitialisation.
     */
    @PostMapping("/auth/forgot-password")
    public String processForgotPassword(@Valid @ModelAttribute("forgotPasswordRequest") ForgotPasswordRequest request,
                                        BindingResult result, Model model) {
        if (result.hasErrors()) {
            return "auth/forgot-password";
        }
        try {
            authService.forgotPassword(request.getEmail());
            return "redirect:/auth/reset-password?email=" + request.getEmail() + "&sent=true";
        } catch (Exception e) {
            model.addAttribute("errorMsg", e.getMessage());
            return "auth/forgot-password";
        }
    }

    /**
     * Affiche la page de saisie du code et du nouveau mot de passe.
     */
    @GetMapping("/auth/reset-password")
    public String resetPasswordPage(@RequestParam(required = false) String email, Model model) {
        ResetPasswordRequest request = new ResetPasswordRequest();
        model.addAttribute("resetPasswordRequest", request);
        model.addAttribute("email", email);
        return "auth/reset-password";
    }

    /**
     * Traite la réinitialisation effective du mot de passe.
     */
    @PostMapping("/auth/reset-password")
    public String processResetPassword(@Valid @ModelAttribute("resetPasswordRequest") ResetPasswordRequest request,
                                       BindingResult result, Model model) {
        if (result.hasErrors()) {
            return "auth/reset-password";
        }
        try {
            authService.resetPassword(request.getToken(), request.getNewPassword());
            return "redirect:/auth/login?resetSuccess=true";
        } catch (Exception e) {
            model.addAttribute("errorMsg", e.getMessage());
            return "auth/reset-password";
        }
    }

    // ── Endpoints API REST (utilisables depuis Postman, une app mobile, etc.) ──

    /**
     * Inscription via API REST — reçoit et retourne du JSON.
     *
     * <p>Exemple de requête Postman :</p>
     * <pre>{@code
     * POST http://localhost:8082/api/auth/register
     * Content-Type: application/json
     *
     * {
     *   "fullName": "Jean Dupont",
     *   "email": "jean@test.com",
     *   "password": "MonMdp1234",
     *   "confirmPassword": "MonMdp1234"
     * }
     * }</pre>
     */
    @PostMapping("/api/auth/register")
    @ResponseBody   // Retourne du JSON au lieu d'un nom de template
    public ResponseEntity<UserDTO> apiRegister(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    /**
     * Connexion via API REST — retourne un token JWT à utiliser dans les requêtes suivantes.
     *
     * <p>Exemple de requête Postman :</p>
     * <pre>{@code
     * POST http://localhost:8082/api/auth/login
     * Content-Type: application/json
     *
     * { "email": "admin@primevideo.com", "password": "Admin1234" }
     * }</pre>
     *
     * <p>Réponse :</p>
     * <pre>{@code
     * { "accessToken": "eyJhbGci...", "tokenType": "Bearer", "expiresIn": 3600 }
     * }</pre>
     */
    @PostMapping("/api/auth/login")
    @ResponseBody
    public ResponseEntity<AuthResponse> apiLogin(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    /**
     * Endpoint pour rafraîchir l'access token.
     */
    @PostMapping("/api/auth/refresh")
    @ResponseBody
    public ResponseEntity<AuthResponse> apiRefresh(@Valid @RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(authService.refreshAccessToken(request.getRefreshToken()));
    }

    /**
     * Endpoint pour demander la réinitialisation du mot de passe.
     */
    @PostMapping("/api/auth/forgot-password")
    @ResponseBody
    public ResponseEntity<String> apiForgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        authService.forgotPassword(request.getEmail());
        return ResponseEntity.ok("Si un compte correspond à cet email, un code de réinitialisation vous a été envoyé.");
    }

    /**
     * Endpoint pour réinitialiser le mot de passe.
     */
    @PostMapping("/api/auth/reset-password")
    @ResponseBody
    public ResponseEntity<String> apiResetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request.getToken(), request.getNewPassword());
        return ResponseEntity.ok("Mot de passe réinitialisé avec succès.");
    }

    /**
     * Endpoint pour valider le second facteur MFA (TOTP).
     */
    @PostMapping("/api/auth/mfa/challenge")
    @ResponseBody
    public ResponseEntity<AuthResponse> apiMfaChallenge(@Valid @RequestBody MfaChallengeRequest request) {
        return ResponseEntity.ok(authService.verifyMfaChallenge(
                request.getMfaToken(), 
                request.getCode(), 
                request.getDeviceId()
        ));
    }

    /**
     * Endpoint pour initialiser la configuration MFA.
     */
    @PostMapping("/api/auth/mfa/setup")
    @ResponseBody
    public ResponseEntity<MfaSetupResponse> apiMfaSetup(java.security.Principal principal) {
        return ResponseEntity.ok(authService.initMfaSetup(principal.getName()));
    }

    /**
     * Endpoint pour activer définitivement le MFA.
     */
    @PostMapping("/api/auth/mfa/verify")
    @ResponseBody
    public ResponseEntity<String> apiMfaVerify(java.security.Principal principal, @Valid @RequestBody MfaVerifyRequest request) {
        authService.activateMfa(principal.getName(), request.getCode(), request.getBackupCodes());
        return ResponseEntity.ok("MFA activé avec succès");
    }
}
