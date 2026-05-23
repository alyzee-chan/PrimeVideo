package com.primevideo.controller;

import com.primevideo.dto.request.ContentRequest;
import com.primevideo.entity.Content;
import com.primevideo.repository.ContentRepository;
import com.primevideo.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.format.DateTimeFormatter;
import java.util.List;
import com.primevideo.entity.User;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final ContentRepository contentRepository;
    private final UserRepository userRepository;
    private final com.primevideo.service.ReportService reportService;
    private final com.primevideo.service.subscription.SubscriptionService subscriptionService;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("totalUsers", userRepository.count());
        model.addAttribute("totalContents", contentRepository.count());
        model.addAttribute("recentContents", contentRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt")));
        return "admin/dashboard";
    }

    @GetMapping("/catalog")
    public String listCatalog(Model model) {
        model.addAttribute("contents", contentRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt")));
        return "admin/catalog";
    }

    @GetMapping("/users")
    public String listUsers(Model model) {
        model.addAttribute("users", userRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt")));
        return "admin/users";
    }

    @PostMapping("/users/end-subscription/{id}")
    public String endUserSubscription(@PathVariable Long id) {
        subscriptionService.cancelSubscription(id);
        return "redirect:/admin/users?subscriptionEnded=true";
    }

    @GetMapping("/users/export")
    public void exportUsersToCSV(HttpServletResponse response) throws IOException {
        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=\"users_export.csv\"");

        List<User> users = userRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
        PrintWriter writer = response.getWriter();

        // En-têtes CSV
        writer.println("ID,Nom complet,Email,Role,Est Actif,Est Prime,Date d'inscription");

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        for (User user : users) {
            writer.println(String.format("%d,%s,%s,%s,%b,%b,%s",
                    user.getId(),
                    user.getFullName(),
                    user.getEmail(),
                    user.getRole(),
                    user.getIsActive(),
                    user.getIsPrime(),
                    user.getCreatedAt() != null ? user.getCreatedAt().format(formatter) : ""
            ));
        }
        writer.flush();
    }

    @GetMapping("/catalog/export")
    public void exportCatalogToCSV(HttpServletResponse response) throws IOException {
        response.setContentType("text/csv; charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=\"catalog_export.csv\"");

        List<Content> contents = contentRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
        PrintWriter writer = response.getWriter();

        // BOM UTF-8 pour Excel
        writer.write('\ufeff');

        // En-têtes CSV
        writer.println("ID,Titre,Type,Genre,Année,Réalisateur,Est Premium,Date d'ajout");

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        for (Content content : contents) {
            writer.println(String.format("%d,\"%s\",%s,%s,%d,\"%s\",%b,%s",
                    content.getId(),
                    content.getTitle().replace("\"", "\"\""),
                    content.getType(),
                    content.getGenre(),
                    content.getYear(),
                    content.getDirector() != null ? content.getDirector().replace("\"", "\"\"") : "",
                    content.getIsPremium(),
                    content.getCreatedAt() != null ? content.getCreatedAt().format(formatter) : ""
            ));
        }
        writer.flush();
    }

    @GetMapping("/catalog/new")
    public String showCreateForm(Model model) {
        model.addAttribute("contentRequest", new ContentRequest());
        model.addAttribute("types", Content.ContentType.values());
        model.addAttribute("genres", Content.Genre.values());
        return "admin/content-form";
    }

    @GetMapping("/catalog/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Content content = contentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Contenu non trouvé"));
        
        ContentRequest request = ContentRequest.builder()
                .id(content.getId())
                .title(content.getTitle())
                .description(content.getDescription())
                .type(content.getType())
                .genre(content.getGenre())
                .year(content.getYear())
                .durationSeconds(content.getDurationSeconds())
                .durationHours(content.getDurationSeconds() != null ? content.getDurationSeconds() / 3600 : 0)
                .durationMinutes(content.getDurationSeconds() != null ? (content.getDurationSeconds() % 3600) / 60 : 0)
                .posterUrl(content.getPosterUrl())
                .bannerUrl(content.getBannerUrl())
                .videoUrl(content.getVideoUrl())
                .trailerUrl(content.getTrailerUrl())
                .director(content.getDirector())
                .castList(content.getCastList())
                .language(content.getLanguage())
                .releaseDate(content.getReleaseDate())
                .ageRating(content.getAgeRating())
                .ratingAverage(content.getRatingAverage())
                .isPremium(content.getIsPremium())
                .isAvailable(content.getIsAvailable())
                .build();
        
        if (content.getType() == Content.ContentType.ANIME && content.getAnimeMetadata() != null) {
            request.setEpisodeCount(content.getAnimeMetadata().getEpisodeCount());
        } else if (content.getType() == Content.ContentType.KDRAMA && content.getKdramaMetadata() != null) {
            request.setEpisodeCount(content.getKdramaMetadata().getEpisodeCount());
        } else if (content.getType() == Content.ContentType.SERIE && content.getSeasons() != null && !content.getSeasons().isEmpty()) {
            request.setEpisodeCount(content.getSeasons().get(0).getEpisodes().size());
        }

        model.addAttribute("contentRequest", request);
        model.addAttribute("types", Content.ContentType.values());
        model.addAttribute("genres", Content.Genre.values());
        return "admin/content-form";
    }

    @PostMapping("/catalog/save")
    public String saveContent(@Valid @ModelAttribute("contentRequest") ContentRequest request, 
                              BindingResult result, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("types", Content.ContentType.values());
            model.addAttribute("genres", Content.Genre.values());
            return "admin/content-form";
        }

        Content content;
        boolean isNewEpisodeMode = false;
        if (request.getId() != null) {
            content = contentRepository.findById(request.getId()).orElseThrow(() -> new RuntimeException("Contenu non trouvé"));
        } else {
            String titleToSearch = request.getTitle() != null ? request.getTitle().trim() : "";
            List<Content> existing = contentRepository.findByTitleIgnoreCase(titleToSearch);
            if (!existing.isEmpty() && (request.getType() == Content.ContentType.ANIME || request.getType() == Content.ContentType.SERIE || request.getType() == Content.ContentType.KDRAMA)) {
                content = existing.get(0);
                isNewEpisodeMode = true;
            } else {
                content = new Content();
            }
        }

        // Upload des fichiers dans BD-Prime
        String projectDir = System.getProperty("user.dir");
        java.io.File bdPrimeDir = new java.io.File(projectDir, "BD-Prime");
        if (!bdPrimeDir.exists()) bdPrimeDir.mkdirs();

        try {
            if (request.getVideoFile() != null && !request.getVideoFile().isEmpty()) {
                String filename = org.springframework.util.StringUtils.cleanPath(request.getVideoFile().getOriginalFilename());
                java.io.File target = new java.io.File(bdPrimeDir, filename);
                request.getVideoFile().transferTo(target);
                request.setVideoUrl("/media/" + filename);
            }
            if (request.getPosterFile() != null && !request.getPosterFile().isEmpty()) {
                String filename = org.springframework.util.StringUtils.cleanPath(request.getPosterFile().getOriginalFilename());
                java.io.File target = new java.io.File(bdPrimeDir, filename);
                request.getPosterFile().transferTo(target);
                request.setPosterUrl("/media/" + filename);
            }
            if (request.getBannerFile() != null && !request.getBannerFile().isEmpty()) {
                String filename = org.springframework.util.StringUtils.cleanPath(request.getBannerFile().getOriginalFilename());
                java.io.File target = new java.io.File(bdPrimeDir, filename);
                request.getBannerFile().transferTo(target);
                request.setBannerUrl("/media/" + filename);
            }
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }

        if (!isNewEpisodeMode) {
            content.setTitle(request.getTitle());
            content.setDescription(request.getDescription());
            content.setType(request.getType());
            content.setGenre(request.getGenre());
            content.setYear(request.getYear());
            content.setDurationSeconds(request.getDurationSeconds());
            if (request.getPosterUrl() != null) content.setPosterUrl(request.getPosterUrl());
            if (request.getBannerUrl() != null) content.setBannerUrl(request.getBannerUrl());
            if (request.getVideoUrl() != null) content.setVideoUrl(request.getVideoUrl());
            content.setTrailerUrl(request.getTrailerUrl());
            content.setDirector(request.getDirector());
            content.setCastList(request.getCastList());
            content.setLanguage(request.getLanguage());
            content.setReleaseDate(request.getReleaseDate());
            content.setAgeRating(request.getAgeRating() != null ? request.getAgeRating() : 0);
            content.setRatingAverage(request.getRatingAverage() != null ? request.getRatingAverage() : java.math.BigDecimal.ZERO);
            content.setIsPremium(request.getIsPremium() != null ? request.getIsPremium() : false);
            content.setIsAvailable(request.getIsAvailable() != null ? request.getIsAvailable() : true);
            
            // Gestion de la durée pour les films / documentaires
            if (request.getType() == Content.ContentType.FILM || request.getType() == Content.ContentType.DOCUMENTAIRE || request.getType() == Content.ContentType.AFRICAIN) {
                int h = request.getDurationHours() != null ? request.getDurationHours() : 0;
                int m = request.getDurationMinutes() != null ? request.getDurationMinutes() : 0;
                content.setDurationSeconds((h * 3600) + (m * 60));
            }
        }

        // Sauvegarde initiale
        content = contentRepository.save(content);

        // Si la vidéo a été ajoutée et c'est une série/anime, on l'ajoute ou on met à jour l'épisode
        if (request.getVideoUrl() != null && !request.getVideoUrl().trim().isEmpty()) {
            if (content.getType() == Content.ContentType.ANIME || content.getType() == Content.ContentType.SERIE || content.getType() == Content.ContentType.KDRAMA) {
                com.primevideo.entity.Season season;
                if (content.getSeasons() == null || content.getSeasons().isEmpty()) {
                    season = new com.primevideo.entity.Season();
                    season.setContent(content);
                    season.setSeasonNumber(1);
                    season.setTitle("Saison 1");
                    if (content.getSeasons() == null) content.setSeasons(new java.util.ArrayList<>());
                    content.getSeasons().add(season);
                } else {
                    season = content.getSeasons().get(0);
                }
                
                if (season.getEpisodes() == null) season.setEpisodes(new java.util.ArrayList<>());
                
                com.primevideo.entity.Episode targetEpisode = null;
                Integer reqEpNum = request.getUploadedEpisodeNumber();
                
                // 1. Chercher l'épisode spécifique si demandé
                if (reqEpNum != null && reqEpNum > 0) {
                    targetEpisode = season.getEpisodes().stream()
                        .filter(e -> e.getEpisodeNumber().equals(reqEpNum))
                        .findFirst().orElse(null);
                } else {
                    // 2. Sinon chercher le premier épisode vide (sans vidéo)
                    targetEpisode = season.getEpisodes().stream()
                        .filter(e -> e.getVideoUrl() == null || e.getVideoUrl().trim().isEmpty())
                        .findFirst().orElse(null);
                }
                
                // 3. Sinon créer un nouvel épisode
                if (targetEpisode == null) {
                    targetEpisode = new com.primevideo.entity.Episode();
                    targetEpisode.setSeason(season);
                    int epNum = (reqEpNum != null && reqEpNum > 0) ? reqEpNum : (season.getEpisodes().size() + 1);
                    targetEpisode.setEpisodeNumber(epNum);
                    targetEpisode.setTitle("Épisode " + epNum);
                    targetEpisode.setDescription(isNewEpisodeMode && request.getDescription() != null && !request.getDescription().isEmpty() ? request.getDescription() : "Nouvel épisode disponible");
                    season.getEpisodes().add(targetEpisode);
                }
                
                // Mettre à jour l'épisode avec la vidéo
                int h = request.getDurationHours() != null ? request.getDurationHours() : 0;
                int m = request.getDurationMinutes() != null ? request.getDurationMinutes() : 0;
                int duration = (h * 3600) + (m * 60);
                targetEpisode.setDurationSeconds(duration > 0 ? duration : 1440); // 1440 = 24min par défaut
                targetEpisode.setVideoUrl(request.getVideoUrl());
                
                // Effacer le videoUrl principal pour forcer la lecture via les épisodes
                content.setVideoUrl(null);
            }
        } else if (!isNewEpisodeMode && request.getId() == null && request.getEpisodeCount() != null && request.getEpisodeCount() > 0) {
            // Création initiale d'épisodes VIDES si l'utilisateur a juste mis un nombre d'épisodes
            if (content.getType() == Content.ContentType.SERIE || content.getType() == Content.ContentType.ANIME || content.getType() == Content.ContentType.KDRAMA) {
                if (content.getSeasons() == null || content.getSeasons().isEmpty()) {
                    com.primevideo.entity.Season s1 = com.primevideo.entity.Season.builder()
                        .seasonNumber(1).title("Saison 1").content(content).build();
                    java.util.List<com.primevideo.entity.Episode> epList = new java.util.ArrayList<>();
                    for (int i = 1; i <= request.getEpisodeCount(); i++) {
                        epList.add(com.primevideo.entity.Episode.builder()
                            .episodeNumber(i)
                            .title("Épisode " + i)
                            .season(s1)
                            .durationSeconds(1440)
                            .build());
                    }
                    s1.setEpisodes(epList);
                    if (content.getSeasons() == null) content.setSeasons(new java.util.ArrayList<>());
                    content.getSeasons().add(s1);
                }
            }
        }

        // Mise à jour des métadonnées (nombre d'épisodes)
        Integer eps = request.getEpisodeCount();
        if (content.getSeasons() != null && !content.getSeasons().isEmpty()) {
            // Auto-update episode count based on actual episodes
            int actualCount = content.getSeasons().get(0).getEpisodes().size();
            if (request.getType() == Content.ContentType.ANIME) {
                com.primevideo.entity.AnimeMetadata meta = content.getAnimeMetadata();
                if (meta == null) { meta = new com.primevideo.entity.AnimeMetadata(); meta.setContent(content); meta.setContentId(content.getId()); content.setAnimeMetadata(meta); }
                meta.setEpisodeCount(actualCount);
            } else if (request.getType() == Content.ContentType.KDRAMA) {
                com.primevideo.entity.KDramaMetadata meta = content.getKdramaMetadata();
                if (meta == null) { meta = new com.primevideo.entity.KDramaMetadata(); meta.setContent(content); meta.setContentId(content.getId()); content.setKdramaMetadata(meta); }
                meta.setEpisodeCount(actualCount);
            }
        } else if (eps != null && eps > 0 && !isNewEpisodeMode) {
            // Update based on input if no seasons exist
            if (request.getType() == Content.ContentType.ANIME) {
                com.primevideo.entity.AnimeMetadata meta = content.getAnimeMetadata();
                if (meta == null) { meta = new com.primevideo.entity.AnimeMetadata(); meta.setContent(content); meta.setContentId(content.getId()); content.setAnimeMetadata(meta); }
                meta.setEpisodeCount(eps);
            } else if (request.getType() == Content.ContentType.KDRAMA) {
                com.primevideo.entity.KDramaMetadata meta = content.getKdramaMetadata();
                if (meta == null) { meta = new com.primevideo.entity.KDramaMetadata(); meta.setContent(content); meta.setContentId(content.getId()); content.setKdramaMetadata(meta); }
                meta.setEpisodeCount(eps);
            }
        }

        contentRepository.save(content); // Sauvegarde finale
        return "redirect:/admin/catalog?success=true";
    }

    @GetMapping("/catalog/delete/{id}")
    public String deleteContent(@PathVariable Long id) {
        contentRepository.deleteById(id);
        return "redirect:/admin/catalog?deleted=true";
    }

    @GetMapping("/marketing")
    public String marketingDashboard(Model model) {
        model.addAttribute("contents", contentRepository.findAll());
        return "admin/marketing";
    }

    @GetMapping("/dump-catalog")
    @ResponseBody
    public String dumpCatalog() {
        var contents = contentRepository.findAll();
        StringBuilder sb = new StringBuilder("--- CATALOG DUMP ---\n");
        for (var c : contents) {
            sb.append("ID: ").append(c.getId())
              .append(" | Title: ").append(c.getTitle())
              .append(" | VideoUrl: ").append(c.getVideoUrl())
              .append("\n");
        }
        String dump = sb.toString();
        System.out.println(dump);
        return dump;
    }

    @GetMapping("/fix-videos")
    public String fixVideos() {
        var contents = contentRepository.findAll();
        for (var content : contents) {
            String title = content.getTitle().toLowerCase();
            String oldUrl = content.getVideoUrl();
            
            // Mapping des fichiers renommés vers les titres
            if (title.contains("stranger things")) content.setVideoUrl("/media/stranger_things_s1e1.mp4");
            else if (title.contains("the boys")) content.setVideoUrl("/media/the_boys_s1e1.mp4");
            else if (title.contains("banker")) content.setVideoUrl("/media/the_banker.mp4");
            else if (title.contains("gen v")) content.setVideoUrl("/media/gen_v_s1e1.mp4");
            else if (title.contains("transylvania") || title.contains("transylvanie") || title.contains("hotel")) 
                content.setVideoUrl("/media/hotel_transylvania_4.avi");
            else if (title.contains("tomorrow war")) content.setVideoUrl("/media/the_tomorrow_war.avi");
            else if (title.contains("apex")) content.setVideoUrl("/media/apex.avi");
            else if (title.contains("purge")) content.setVideoUrl("/media/the_purge_s1e1.mkv");
            
            // Fallback si l'URL est invalide ou vide
            if (content.getVideoUrl() == null || content.getVideoUrl().isBlank() || content.getVideoUrl().contains(" ") || content.getVideoUrl().equals("null")) {
                content.setVideoUrl("https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4");
            }
            
            System.out.println("FIX: [" + content.getTitle() + "] " + oldUrl + " -> " + content.getVideoUrl());
        }
        contentRepository.saveAll(contents);
        return "redirect:/admin/dashboard?fixed=true";
    }

    @GetMapping("/reports/sales/monthly")
    public void downloadMonthlySalesReport(HttpServletResponse response) throws IOException {
        byte[] pdfBytes = reportService.generateMonthlySalesReport();
        
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=\"rapport_ventes_" + 
                          java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("MM_yyyy")) + ".pdf\"");
        response.setContentLength(pdfBytes.length);
        
        response.getOutputStream().write(pdfBytes);
        response.getOutputStream().flush();
    }
}
