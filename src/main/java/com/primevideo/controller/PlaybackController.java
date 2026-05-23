package com.primevideo.controller;

import com.primevideo.entity.ViewingHistory;
import com.primevideo.service.UserActionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/player")
@RequiredArgsConstructor
public class PlaybackController {

    private final UserActionService userActionService;

    @PostMapping("/progress")
    public ResponseEntity<Void> updateProgress(
            @RequestParam Long profileId,
            @RequestParam Long contentId,
            @RequestParam int seconds) {
        userActionService.updateHistory(profileId, contentId, seconds);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/progress")
    public ResponseEntity<Integer> getProgress(
            @RequestParam Long profileId,
            @RequestParam Long contentId) {
        return userActionService.getHistory(profileId).stream()
                .filter(h -> h.getContent().getId().equals(contentId))
                .map(ViewingHistory::getProgressSeconds)
                .findFirst()
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.ok(0));
    }
}
