package com.primevideo.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/ads")
@Slf4j
public class AdsAIController {

    @PostMapping("/generate")
    public ResponseEntity<Map<String, Object>> generateAd(@RequestBody Map<String, Object> request) {
        log.info("Demande de génération de publicité IA pour le contenu : {}", request.get("contentId"));
        
        String jobId = UUID.randomUUID().toString();
        
        return ResponseEntity.accepted().body(Map.of(
            "jobId", jobId,
            "status", "QUEUED",
            "message", "La génération de la publicité a été mise en file d'attente."
        ));
    }

    @GetMapping("/jobs/{id}")
    public ResponseEntity<Map<String, Object>> getJobStatus(@PathVariable String id) {
        // Mocking job completion
        return ResponseEntity.ok(Map.of(
            "id", id,
            "status", "COMPLETED",
            "progress", 100,
            "adId", "ad_" + UUID.randomUUID().toString().substring(0, 8),
            "videoUrl", "https://cdn.primevideo.com/ads/generated_trailer.mp4"
        ));
    }
}
