package com.primevideo.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/image-search")
@Slf4j
@lombok.RequiredArgsConstructor
public class ImageSearchController {

    private final com.primevideo.service.CatalogService catalogService;
    private final com.primevideo.service.ChatbotService chatbotService;

    @PostMapping
    public ResponseEntity<Map<String, Object>> searchByImage(@RequestParam("image") MultipartFile image) {
        try {
            log.info("Début de l'analyse visuelle de l'image (IA Vision)...");
            
            // Étape 1 : Analyse du contenu visuel via l'IA (et non plus le nom de fichier)
            String aiDescription = chatbotService.analyzeImageContent(image.getBytes());
            log.info("L'IA a identifié : '{}'", aiDescription);
            
            // Étape 2 : Utilisation de la description de l'IA pour chercher dans le catalogue
            var results = catalogService.search(aiDescription, 0, 1);
            
            if (!results.isEmpty()) {
                var matched = results.getContent().get(0);
                return ResponseEntity.ok(Map.of(
                    "confidence", 0.95,
                    "recognizedType", "MEDIA",
                    "recognizedLabel", aiDescription,
                    "matchedContent", List.of(
                        Map.of("id", matched.getId(), "title", matched.getTitle())
                    ),
                    "processingTimeMs", 1200
                ));
            }

            return ResponseEntity.ok(Map.of(
                "confidence", 0.4,
                "recognizedLabel", aiDescription,
                "matchedContent", List.of(),
                "message", "L'IA a reconnu '" + aiDescription + "' mais ce contenu n'est pas encore dans notre catalogue.",
                "processingTimeMs", 800
            ));
        } catch (Exception e) {
            log.error("Erreur lors de l'analyse d'image", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
