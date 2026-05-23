package com.primevideo.controller;

import com.primevideo.entity.ViewingHistory;
import com.primevideo.entity.WatchlistItem;
import com.primevideo.service.UserActionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class UserActionController {

    private final UserActionService userActionService;

    // ── Pages Thymeleaf ───────────────────────────────────────────────────

    @GetMapping("/profiles/{profileId}/watchlist")
    public String watchlistPage(@PathVariable Long profileId, Model model) {
        model.addAttribute("watchlist", userActionService.getWatchlist(profileId));
        model.addAttribute("profileId", profileId);
        return "profile/watchlist";
    }

    @GetMapping("/profiles/{profileId}/history")
    public String historyPage(@PathVariable Long profileId, Model model) {
        model.addAttribute("history", userActionService.getHistory(profileId));
        model.addAttribute("profileId", profileId);
        return "profile/history";
    }

    // ── API REST ──────────────────────────────────────────────────────────

    @GetMapping("/api/profiles/{profileId}/watchlist")
    @ResponseBody
    public ResponseEntity<List<WatchlistItem>> apiGetWatchlist(@PathVariable Long profileId) {
        return ResponseEntity.ok(userActionService.getWatchlist(profileId));
    }

    @PostMapping("/api/profiles/{profileId}/watchlist/{contentId}")
    @ResponseBody
    public ResponseEntity<Void> apiAddToWatchlist(@PathVariable Long profileId, @PathVariable Long contentId) {
        userActionService.addToWatchlist(profileId, contentId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/api/profiles/{profileId}/watchlist/{contentId}")
    @ResponseBody
    public ResponseEntity<Void> apiRemoveFromWatchlist(@PathVariable Long profileId, @PathVariable Long contentId) {
        userActionService.removeFromWatchlist(profileId, contentId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/api/profiles/{profileId}/history")
    @ResponseBody
    public ResponseEntity<List<ViewingHistory>> apiGetHistory(@PathVariable Long profileId) {
        return ResponseEntity.ok(userActionService.getHistory(profileId));
    }

    @PostMapping("/api/profiles/{profileId}/history/{contentId}")
    @ResponseBody
    public ResponseEntity<Void> apiUpdateHistory(
            @PathVariable Long profileId, 
            @PathVariable Long contentId,
            @RequestParam(required = false, defaultValue = "0") Integer progressSeconds) {
        userActionService.updateHistory(profileId, contentId, progressSeconds);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/api/profiles/{profileId}/history/{contentId}")
    @ResponseBody
    public ResponseEntity<Void> apiRemoveFromHistory(@PathVariable Long profileId, @PathVariable Long contentId) {
        userActionService.removeFromHistory(profileId, contentId);
        return ResponseEntity.noContent().build();
    }
}
