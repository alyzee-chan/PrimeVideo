package com.primevideo.service;

import com.primevideo.dto.response.AnimeMetadataDTO;
import com.primevideo.dto.response.ContentDTO;
import com.primevideo.dto.response.SeasonDTO;
import com.primevideo.dto.response.EpisodeDTO;
import com.primevideo.dto.response.KDramaMetadataDTO;
import com.primevideo.dto.response.WebtoonMetadataDTO;
import com.primevideo.entity.Content;
import com.primevideo.entity.Profile;
import com.primevideo.exception.ResourceNotFoundException;
import com.primevideo.repository.ContentRepository;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service de gestion du catalogue de contenus.
 *
 * <p>Responsabilités :</p>
 * <ul>
 *   <li>Récupérer et paginer les contenus depuis {@link ContentRepository}</li>
 *   <li>Convertir les entités {@link Content} en DTOs {@link ContentDTO} (jamais exposer l'entité brute)</li>
 *   <li>Formatter la durée en texte lisible (ex : 8325 secondes → "2h 18min")</li>
 *   <li>Gérer la recherche plein-texte dans le catalogue</li>
 * </ul>
 *
 * <p>La <strong>pagination</strong> fonctionne avec l'objet {@code Pageable} de Spring Data.
 * On lui indique le numéro de page (0-indexé) et la taille, et il gère automatiquement
 * l'offset SQL ({@code LIMIT x OFFSET y}).</p>
 */
@Service
@RequiredArgsConstructor
public class CatalogService {

    private final ContentRepository contentRepository;
    private final HttpSession session;

    public Page<ContentDTO> getAll(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return contentRepository.findAllByAgeRatingLessThanEqualAndIsAvailableTrue(getMaxAge(), pageable).map(this::toDTO);
    }

    public Page<ContentDTO> getByType(Content.ContentType type, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("ratingAverage").descending());
        return contentRepository.findByTypeAndIsAvailableTrueAndAgeRatingLessThanEqual(type, getMaxAge(), pageable).map(this::toDTO);
    }

    public Page<ContentDTO> getByTypeAndGenre(Content.ContentType type, Content.Genre genre, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("ratingAverage").descending());
        return contentRepository.findByTypeAndGenreAndIsAvailableTrueAndAgeRatingLessThanEqual(type, genre, getMaxAge(), pageable).map(this::toDTO);
    }

    public ContentDTO getById(Long id) {
        Content content = contentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contenu introuvable : id=" + id));
        
        // Vérification de l'âge pour le détail
        if (content.getAgeRating() > getMaxAge()) {
            throw new ResourceNotFoundException("Contenu non autorisé pour ce profil");
        }
        
        return toDTO(content);
    }

    public Page<ContentDTO> search(String query, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("ratingAverage").descending());
        String cleanQuery = query.toLowerCase().trim();
        
        // 1. Recherche directe (Substring)
        Page<ContentDTO> results = contentRepository.searchWithAgeLimit(cleanQuery, getMaxAge(), pageable).map(this::toDTO);
        if (!results.isEmpty()) return results;

        // 2. Recherche par mots-clés
        if (cleanQuery.contains(" ")) {
            String[] words = cleanQuery.split("\\s+");
            String bestWord = "";
            for (String word : words) {
                if (word.length() > bestWord.length()) bestWord = word;
            }
            if (bestWord.length() >= 3) {
                results = contentRepository.searchWithAgeLimit(bestWord, getMaxAge(), pageable).map(this::toDTO);
                if (!results.isEmpty()) return results;
            }
        }
        
        // 3. RECHERCHE FLOU (Fuzzy Match) - Titre Principal + Titres Originaux (Japonais, Romaji, Coréen)
        List<Content> allContent = contentRepository.findAll();
        Content bestMatch = null;
        int minDistance = Integer.MAX_VALUE;
        
        for (Content c : allContent) {
            // Liste des titres à tester pour ce contenu
            java.util.List<String> titlesToTest = new java.util.ArrayList<>();
            titlesToTest.add(c.getTitle().toLowerCase());
            
            if (c.getAnimeMetadata() != null) {
                if (c.getAnimeMetadata().getJapaneseTitle() != null) titlesToTest.add(c.getAnimeMetadata().getJapaneseTitle().toLowerCase());
                if (c.getAnimeMetadata().getRomajiTitle() != null) titlesToTest.add(c.getAnimeMetadata().getRomajiTitle().toLowerCase());
            }
            if (c.getKdramaMetadata() != null) {
                if (c.getKdramaMetadata().getKoreanTitle() != null) titlesToTest.add(c.getKdramaMetadata().getKoreanTitle().toLowerCase());
            }
            if (c.getWebtoonMetadata() != null) {
                if (c.getWebtoonMetadata().getOriginalTitle() != null) titlesToTest.add(c.getWebtoonMetadata().getOriginalTitle().toLowerCase());
            }

            for (String testTitle : titlesToTest) {
                int dist = levenshteinDistance(cleanQuery, testTitle);
                // Seuil de tolérance : 40% d'erreur max
                if (dist < minDistance && dist <= (testTitle.length() * 0.4)) {
                    minDistance = dist;
                    bestMatch = c;
                }
            }
        }
        
        if (bestMatch != null) {
            List<ContentDTO> list = List.of(toDTO(bestMatch));
            return new org.springframework.data.domain.PageImpl<>(list, pageable, 1);
        }
        
        return Page.empty();
    }

    private int levenshteinDistance(String s1, String s2) {
        int[] prev = new int[s2.length() + 1];
        for (int j = 0; j <= s2.length(); j++) prev[j] = j;
        for (int i = 1; i <= s1.length(); i++) {
            int[] curr = new int[s2.length() + 1];
            curr[0] = i;
            for (int j = 1; j <= s2.length(); j++) {
                int d1 = prev[j] + 1;
                int d2 = curr[j - 1] + 1;
                int d3 = prev[j - 1] + (s1.charAt(i - 1) == s2.charAt(j - 1) ? 0 : 1);
                curr[j] = Math.min(Math.min(d1, d2), d3);
            }
            prev = curr;
        }
        return prev[s2.length()];
    }

    public List<ContentDTO> getTrending() {
        return contentRepository.findMostWatchedWithAgeLimit(getMaxAge(), PageRequest.of(0, 10))
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    public List<ContentDTO> getLatest() {
        return contentRepository.findLatestAvailableWithAgeLimit(getMaxAge(), PageRequest.of(0, 12))
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    private Integer getMaxAge() {
        Profile selected = (Profile) session.getAttribute("selectedProfile");
        if (selected == null) return 100; // Par défaut, tout afficher si pas de profil (ou rediriger)
        
        return switch (selected.getAgeRating()) {
            case ALL -> 0;     // Tout public : 0
            case R_16 -> 15;   // Déconseillé aux moins de 16 ans : max 15 (ne voit pas 16+)
            case R_18 -> 17;   // Déconseillé aux moins de 18 ans : max 17 (ne voit pas 18+)
            case NONE -> 100;  // Aucune restriction : 100
            default -> 0;      // Fallback sécurisé pour U, PG, PG_13 (existant en base)
        };
    }

    public ContentDTO toDTO(Content c) {
        return ContentDTO.builder()
                .id(c.getId())
                .title(c.getTitle())
                .description(c.getDescription())
                .type(c.getType())
                .genre(c.getGenre())
                .year(c.getYear())
                .durationSeconds(c.getDurationSeconds())
                .posterUrl(c.getPosterUrl())
                .bannerUrl(c.getBannerUrl())
                .trailerUrl(c.getTrailerUrl())
                .videoUrl(c.getVideoUrl())
                .ratingAverage(c.getRatingAverage())
                .ratingCount(c.getRatingCount())
                .ageRating(c.getAgeRating())
                .isPremium(c.getIsPremium())
                .director(c.getDirector())
                .castList(c.getCastList())
                .language(c.getLanguage())
                .releaseDate(c.getReleaseDate())
                .durationFormatted(formatDuration(c.getDurationSeconds()))
                .hasAccess(true)   // Logique d'abonnement à affiner
                .animeMetadata(c.getAnimeMetadata() != null ? AnimeMetadataDTO.builder()
                        .japaneseTitle(c.getAnimeMetadata().getJapaneseTitle())
                        .romajiTitle(c.getAnimeMetadata().getRomajiTitle())
                        .subGenre(c.getAnimeMetadata().getSubGenre())
                        .studio(c.getAnimeMetadata().getStudio())
                        .episodeCount(c.getAnimeMetadata().getEpisodeCount())
                        .episodeDurationMinutes(c.getAnimeMetadata().getEpisodeDurationMinutes())
                        .airStatus(c.getAnimeMetadata().getAirStatus())
                        .build() : null)
                .kdramaMetadata(c.getKdramaMetadata() != null ? KDramaMetadataDTO.builder()
                        .koreanTitle(c.getKdramaMetadata().getKoreanTitle())
                        .network(c.getKdramaMetadata().getNetwork())
                        .episodeCount(c.getKdramaMetadata().getEpisodeCount())
                        .isWebtoonAdapted(c.getKdramaMetadata().getIsWebtoonAdapted())
                        .airDays(c.getKdramaMetadata().getAirDays())
                        .ost(c.getKdramaMetadata().getOst())
                        .build() : null)
                .webtoonMetadata(c.getWebtoonMetadata() != null ? WebtoonMetadataDTO.builder()
                        .originalTitle(c.getWebtoonMetadata().getOriginalTitle())
                        .author(c.getWebtoonMetadata().getAuthor())
                        .artist(c.getWebtoonMetadata().getArtist())
                        .platform(c.getWebtoonMetadata().getPlatform())
                        .platformUrl(c.getWebtoonMetadata().getPlatformUrl())
                        .build() : null)
                .seasons(c.getSeasons() != null ? c.getSeasons().stream()
                        .map(s -> SeasonDTO.builder()
                                .id(s.getId())
                                .seasonNumber(s.getSeasonNumber())
                                .title(s.getTitle())
                                .description(s.getDescription())
                                .posterUrl(s.getPosterUrl())
                                .releaseYear(s.getReleaseYear())
                                .episodes(s.getEpisodes() != null ? s.getEpisodes().stream()
                                        .map(e -> EpisodeDTO.builder()
                                                .id(e.getId())
                                                .episodeNumber(e.getEpisodeNumber())
                                                .title(e.getTitle())
                                                .description(e.getDescription())
                                                .durationSeconds(e.getDurationSeconds())
                                                .videoUrl(e.getVideoUrl())
                                                .thumbnailUrl(e.getThumbnailUrl())
                                                .durationFormatted(formatDuration(e.getDurationSeconds()))
                                                .build())
                                        .collect(Collectors.toList()) : null)
                                .build())
                        .collect(Collectors.toList()) : null)
                .build();
    }

    private String formatDuration(Integer seconds) {
        if (seconds == null || seconds == 0) return "";
        int h = seconds / 3600;
        int m = (seconds % 3600) / 60;
        if (h > 0) return h + "h " + m + "min";
        return m + "min";
    }
}
