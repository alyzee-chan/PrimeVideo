package com.primevideo.service;

import com.primevideo.entity.*;
import com.primevideo.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserActionService {

    private final WatchlistItemRepository watchlistRepository;
    private final ViewingHistoryRepository historyRepository;
    private final ProfileRepository profileRepository;
    private final ContentRepository contentRepository;

    // ── Watchlist ─────────────────────────────────────────────────────────

    public List<WatchlistItem> getWatchlist(Long profileId) {
        return watchlistRepository.findByProfileIdOrderByAddedAtDesc(profileId);
    }

    @Transactional
    public void addToWatchlist(Long profileId, Long contentId) {
        if (watchlistRepository.existsByProfileIdAndContentId(profileId, contentId)) {
            return;
        }

        Profile profile = profileRepository.findById(profileId).orElseThrow(() -> new RuntimeException("Profil non trouvé"));
        Content content = contentRepository.findById(contentId).orElseThrow(() -> new RuntimeException("Contenu non trouvé"));

        WatchlistItem item = WatchlistItem.builder()
                .profile(profile)
                .content(content)
                .addedAt(LocalDateTime.now())
                .build();

        watchlistRepository.save(item);
        log.info("Contenu {} ajout  la watchlist du profil {}", contentId, profileId);
    }

    @Transactional
    public void removeFromWatchlist(Long profileId, Long contentId) {
        watchlistRepository.deleteByProfileIdAndContentId(profileId, contentId);
        log.info("Contenu {} retir de la watchlist du profil {}", contentId, profileId);
    }

    // ── Viewing History ───────────────────────────────────────────────────

    public List<ViewingHistory> getHistory(Long profileId) {
        return historyRepository.findByProfileIdOrderByLastWatchedAtDesc(profileId);
    }

    @Transactional
    public void updateHistory(Long profileId, Long contentId, Integer progressSeconds) {
        Profile profile = profileRepository.findById(profileId).orElseThrow(() -> new RuntimeException("Profil non trouvé"));
        Content content = contentRepository.findById(contentId).orElseThrow(() -> new RuntimeException("Contenu non trouvé"));

        ViewingHistory history = historyRepository.findByProfileIdAndContentId(profileId, contentId)
                .orElse(ViewingHistory.builder()
                        .profile(profile)
                        .content(content)
                        .build());

        history.setProgressSeconds(progressSeconds);
        history.setLastWatchedAt(LocalDateTime.now());
        
        // Simuler la compltion si on est proche de la fin (ex: > 90% simul)
        if (progressSeconds > 3000) { // Valeur arbitraire pour l'exemple
            history.setCompleted(true);
        }

        historyRepository.save(history);
        log.info("Historique mis  jour pour le profil {} sur le contenu {}", profileId, contentId);
    }

    @Transactional
    public void removeFromHistory(Long profileId, Long contentId) {
        historyRepository.deleteByProfileIdAndContentId(profileId, contentId);
        log.info("Contenu {} retir de l'historique du profil {}", contentId, profileId);
    }
}
