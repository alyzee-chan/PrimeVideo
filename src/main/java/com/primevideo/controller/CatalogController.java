package com.primevideo.controller;

import com.primevideo.entity.Content;
import com.primevideo.service.CatalogService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import com.primevideo.dto.response.ContentDTO;
import java.security.Principal;

/**
 * Controller gérant le catalogue de contenus et la recherche.
 *
 * <p>Routes gérées :</p>
 * <ul>
 *   <li>GET / ou /home           → page d'accueil avec sections tendances/nouveautés</li>
 *   <li>GET /catalog             → liste paginée (tous types, ou filtré par ?type=FILM)</li>
 *   <li>GET /catalog/{id}        → page de détail d'un contenu</li>
 *   <li>GET /search?q=inception  → résultats de recherche</li>
 *   <li>GET /api/catalog         → liste paginée en JSON (API REST)</li>
 *   <li>GET /api/catalog/{id}    → détail en JSON (API REST)</li>
 * </ul>
 *
 * <p>La pagination fonctionne avec les paramètres {@code ?page=0&size=20} dans l'URL.
 * Spring Data retourne un objet {@code Page<ContentDTO>} qui contient les données
 * ET les métadonnées (totalPages, hasNext, hasPrevious...) utilisées dans le template
 * Thymeleaf pour générer les boutons de navigation.</p>
 */
@Controller
@RequiredArgsConstructor
public class CatalogController {

    private final CatalogService catalogService;
    private final com.primevideo.service.RatingService ratingService;
    private final com.primevideo.service.subscription.SubscriptionService subscriptionService;
    private final com.primevideo.repository.UserRepository userRepository;
    private final com.primevideo.service.UserActionService userActionService;
    private final com.primevideo.service.NotificationService notificationService;
    private final jakarta.servlet.http.HttpSession session;

    /**
     * Page d'accueil — affiche plusieurs sections de contenus.
     */
    @GetMapping("/")
    public String landing(Principal principal, Model model) {
        if (principal != null) {
            return "redirect:/home";
        }
        // Fetch some generic or latest content for the background masonry grid
        model.addAttribute("posters", catalogService.getLatest());
        return "landing";
    }

    /**
     * Page d'accueil — affiche plusieurs sections de contenus.
     */
    @GetMapping("/home")
    public String home(@RequestParam(required = false) String intro, Principal principal, Model model) {
        com.primevideo.entity.Profile selectedProfile = (com.primevideo.entity.Profile) session.getAttribute("selectedProfile");


        
        // --- Member-Specific Logic ---
        if (principal != null) {
            var user = userRepository.findByEmail(principal.getName()).orElse(null);
            if (user != null) {
                model.addAttribute("currentUser", user);
                model.addAttribute("subscription", subscriptionService.getCurrentSubscription(user.getId()).orElse(null));
                
                if (selectedProfile != null) {
                    model.addAttribute("history", userActionService.getHistory(selectedProfile.getId()));
                    model.addAttribute("watchlist", userActionService.getWatchlist(selectedProfile.getId()));
                }
                
                // --- Notifications Data ---
                model.addAttribute("unreadNotifs", notificationService.getUnreadNotifications(user.getId()));
                
                // Content for member dashboard
                model.addAttribute("trending", catalogService.getTrending());
                model.addAttribute("latest", catalogService.getLatest());
                model.addAttribute("africain", catalogService.getByType(Content.ContentType.AFRICAIN, 0, 6).getContent());
                
                return "catalog/member-home"; // Specialized view for members
            }
        }

        // Default view for guests
        model.addAttribute("trending", catalogService.getTrending());
        model.addAttribute("latest", catalogService.getLatest());
        model.addAttribute("films", catalogService.getByType(Content.ContentType.FILM, 0, 6).getContent());
        model.addAttribute("series", catalogService.getByType(Content.ContentType.SERIE, 0, 6).getContent());
        model.addAttribute("africain", catalogService.getByType(Content.ContentType.AFRICAIN, 0, 6).getContent());
        model.addAttribute("anime", catalogService.getByType(Content.ContentType.ANIME, 0, 6).getContent());
        model.addAttribute("kdrama", catalogService.getByType(Content.ContentType.KDRAMA, 0, 6).getContent());
        model.addAttribute("live", catalogService.getByType(Content.ContentType.LIVE, 0, 6).getContent());
        return "catalog/home";
    }


    /**
     * Liste paginée du catalogue, avec filtre optionnel par type.
     *
     * <p>Exemples d'URLs :</p>
     * <ul>
     *   <li>{@code /catalog} → tous les contenus, page 1</li>
     *   <li>{@code /catalog?type=FILM} → films uniquement</li>
     *   <li>{@code /catalog?type=ANIME&page=2} → animés, page 3 (page commence à 0)</li>
     * </ul>
     *
     * <p>{@code @RequestParam(defaultValue = "0")} signifie que si le paramètre n'est pas
     * dans l'URL, on utilise 0 par défaut — donc la première page.</p>
     */
    @GetMapping("/catalog")
    public String catalog(@RequestParam(defaultValue = "0") int page,
                          @RequestParam(defaultValue = "20") int size,
                          @RequestParam(required = false) String type,
                          @RequestParam(required = false) String genre,
                          Model model) {
        Page<ContentDTO> contents;
        if (type != null && !type.isBlank()) {
            Content.ContentType contentType = Content.ContentType.valueOf(type.toUpperCase());
            if (genre != null && !genre.isBlank()) {
                try {
                    contents = catalogService.getByTypeAndGenre(contentType, Content.Genre.valueOf(genre.toUpperCase()), page, size);
                } catch (IllegalArgumentException e) {
                    contents = catalogService.getByType(contentType, page, size);
                }
            } else {
                contents = catalogService.getByType(contentType, page, size);
            }
        } else {
            contents = catalogService.getAll(page, size);
        }
        model.addAttribute("contents", contents);
        model.addAttribute("currentType", type);
        model.addAttribute("currentGenre", genre);
        model.addAttribute("contentTypes", Content.ContentType.values());
        return "catalog/list";
    }

    /**
     * Page de détail d'un contenu.
     *
     * <p>{@code @PathVariable} extrait l'identifiant depuis l'URL.
     * Ex : {@code /catalog/42} → {@code id = 42}.</p>
     *
     * <p>Si l'id n'existe pas en base, {@link CatalogService} lève une
     * {@link com.primevideo.exception.ResourceNotFoundException} qui est interceptée
     * par {@link com.primevideo.exception.GlobalExceptionHandler} et retourne un HTTP 404.</p>
     */
    @GetMapping("/catalog/{id}")
    public String contentDetail(@PathVariable Long id, Principal principal, Model model) {
        ContentDTO content = catalogService.getById(id);
        com.primevideo.entity.Profile selectedProfile = (com.primevideo.entity.Profile) session.getAttribute("selectedProfile");

        // 🛡️ SÉCURITÉ : Contrôle parental (Bloque l'accès direct via URL)
        boolean isAdmin = principal != null && userRepository.findByEmail(principal.getName())
                .map(u -> u.getRole() == com.primevideo.entity.User.Role.ADMIN)
                .orElse(false);

        if (!isAdmin && content.getAgeRating() != null && content.getAgeRating() >= 18) {
            if (selectedProfile == null || (selectedProfile.getAgeRating() != null && selectedProfile.getAgeRating().name().startsWith("PG"))) {
                 return "redirect:/catalog?error=age_restricted";
            }
        }

        model.addAttribute("content", content);
        model.addAttribute("reviews", ratingService.getContentReviews(id));
        model.addAttribute("averageRating", ratingService.getAverageRating(id));
        model.addAttribute("reviewCount", ratingService.getReviewCount(id));

        boolean isSubscribed = false;
        if (principal != null) {
            var user = userRepository.findByEmail(principal.getName()).orElse(null);
            if (user != null) {
                // Admin has full access
                isSubscribed = user.getRole() == com.primevideo.entity.User.Role.ADMIN || 
                               subscriptionService.getCurrentSubscription(user.getId()).isPresent();
            }
        }
        model.addAttribute("isSubscribed", isSubscribed);

        // --- Ajout pour Watchlist et Reprise ---
        if (selectedProfile != null) {
            model.addAttribute("isInWatchlist", userActionService.getWatchlist(selectedProfile.getId()).stream()
                    .anyMatch(item -> item.getContent().getId().equals(id)));
            
            userActionService.getHistory(selectedProfile.getId()).stream()
                    .filter(h -> h.getContent().getId().equals(id))
                    .findFirst()
                    .ifPresent(h -> model.addAttribute("playbackHistory", h));
        }

        return "catalog/detail";
    }

    /**
     * Page du lecteur vidéo immersif (Style Prime Video).
     */
    @GetMapping("/watch/{id}")
    public String watchContent(@PathVariable Long id, 
                               @RequestParam(required = false) Long episode,
                               Principal principal,
                               Model model) {
        ContentDTO content = catalogService.getById(id);
        
        // 🛡️ SÉCURITÉ : Vérification de l'abonnement pour les contenus Premium
        if (Boolean.TRUE.equals(content.getIsPremium())) {
            boolean hasAccess = false;
            if (principal != null) {
                var user = userRepository.findByEmail(principal.getName()).orElse(null);
                if (user != null) {
                    // Admin has full access
                    hasAccess = user.getRole() == com.primevideo.entity.User.Role.ADMIN || 
                                subscriptionService.getCurrentSubscription(user.getId()).isPresent();
                }
            }
            
            if (!hasAccess) {
                return "redirect:/subscriptions/plans?error=subscription_required";
            }
        }

        model.addAttribute("content", content);
        
        if (episode != null && content.getSeasons() != null) {
            // Rechercher l'épisode dans les saisons
            for (var season : content.getSeasons()) {
                var episodes = season.getEpisodes();
                for (int i = 0; i < episodes.size(); i++) {
                    if (episodes.get(i).getId().equals(episode)) {
                        model.addAttribute("selectedEpisode", episodes.get(i));
                        // Épisode suivant dans la même saison
                        if (i + 1 < episodes.size()) {
                            model.addAttribute("nextEpisodeId", episodes.get(i + 1).getId());
                        }
                        break;
                    }
                }
            }
        }
        
        // Charger l'historique de lecture pour la reprise
        if (principal != null) {
            var user = userRepository.findByEmail(principal.getName()).orElse(null);
            if (user != null) {
                // Si l'utilisateur a un profil actif en session, il faudrait idéalement l'utiliser.
                // Pour l'instant, on utilise l'ID du premier profil ou on vérifie tous les profils de l'utilisateur.
                // Ici, pour correspondre à l'erreur sans complexifier : 
                if (user.getProfiles() != null && !user.getProfiles().isEmpty()) {
                    Long profileId = user.getProfiles().get(0).getId();
                    userActionService.getHistory(profileId).stream()
                        .filter(h -> h.getContent().getId().equals(id))
                        .findFirst()
                        .ifPresent(h -> model.addAttribute("playbackHistory", h));
                }
            }
        }
        
        return "catalog/player";
    }

    /**
     * Page de résultats de recherche.
     *
     * <p>Si le paramètre {@code q} est absent ou vide (l'utilisateur a juste ouvert la page),
     * on affiche la page vide sans résultats. La recherche ne se lance que si {@code q} est renseigné.</p>
     */
    @GetMapping("/search")
    public String search(@RequestParam(required = false) String q,
                         @RequestParam(defaultValue = "0") int page,
                         Model model) {
        if (q != null && !q.isBlank()) {
            String lowercaseQuery = q.toLowerCase();
            model.addAttribute("results", catalogService.search(lowercaseQuery, page, 20));
            model.addAttribute("query", q); // On garde l'originale pour l'affichage
        }
        return "catalog/search";
    }

    // ── Endpoints API REST ────────────────────────────────────────────────

    /**
     * Liste paginée du catalogue en JSON — pour une future appli mobile ou Postman.
     * {@code @ResponseBody} indique que la valeur de retour est sérialisée en JSON.
     */
    @GetMapping("/api/catalog")
    @ResponseBody
    public Page<ContentDTO> apiCatalog(@RequestParam(defaultValue = "0") int page,
                                       @RequestParam(defaultValue = "20") int size) {
        return catalogService.getAll(page, size);
    }

    /** Détail d'un contenu en JSON. */
    @GetMapping("/api/catalog/{id}")
    @ResponseBody
    public ContentDTO apiContent(@PathVariable Long id) {
        return catalogService.getById(id);
    }

    /** API de recherche rapide pour la navigation vocale. */
    @GetMapping("/api/catalog/quick-search")
    @ResponseBody
    public java.util.List<ContentDTO> quickSearch(@RequestParam String q) {
        return catalogService.search(q, 0, 5).getContent();
    }
}
