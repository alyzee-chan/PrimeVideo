package com.primevideo.service;

import com.primevideo.dto.response.AISearchResponse;
import com.primevideo.entity.Content;
import com.primevideo.repository.ContentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class AISearchService {

    private final ContentRepository contentRepository;

    // Cache pour stocker les jobs en mmoire (Simul)
    private final Map<String, AISearchResponse> jobTracker = new ConcurrentHashMap<>();

    public String submitJob(MultipartFile file) {
        String jobId = UUID.randomUUID().toString();
        
        AISearchResponse initialResponse = AISearchResponse.builder()
                .jobId(jobId)
                .status("PENDING")
                .matches(new ArrayList<>())
                .build();
        
        jobTracker.put(jobId, initialResponse);
        
        // Lancer l'analyse en "arrire-plan" simul
        processImageAI(jobId, file.getOriginalFilename());
        
        return jobId;
    }

    public AISearchResponse getJobStatus(String jobId) {
        return jobTracker.getOrDefault(jobId, AISearchResponse.builder().status("NOT_FOUND").build());
    }

    @Async
    protected void processImageAI(String jobId, String fileName) {
        try {
            AISearchResponse response = jobTracker.get(jobId);
            
            // Phase 1: Processing
            response.setStatus("PROCESSING");
            Thread.sleep(3000); // Simulation temps d'analyse IA

            // Phase 2: Completion
            List<AISearchResponse.MatchResult> matches = new ArrayList<>();
            
            // Logique de simulation base sur le nom du fichier
            String lowerName = fileName.toLowerCase();
            List<Content> allContent = contentRepository.findAll();
            
            for (Content c : allContent) {
                if (lowerName.contains(c.getTitle().toLowerCase().split(" ")[0])) {
                    matches.add(AISearchResponse.MatchResult.builder()
                            .contentId(c.getId())
                            .title(c.getTitle())
                            .type(c.getType().name())
                            .confidence(0.95)
                            .reason("Affiche identifie avec certitude")
                            .build());
                }
            }

            // Si rien n'est trouv, on renvoie des recommandations "intelligentes" (alatoires pour la dmo)
            if (matches.isEmpty() && !allContent.isEmpty()) {
                Collections.shuffle(allContent);
                Content random = allContent.get(0);
                matches.add(AISearchResponse.MatchResult.builder()
                        .contentId(random.getId())
                        .title(random.getTitle())
                        .type(random.getType().name())
                        .confidence(0.72)
                        .reason("Similarit visuelle dtecte (Style " + random.getGenre() + ")")
                        .build());
            }

            response.setMatches(matches);
            response.setStatus("COMPLETED");
            log.info("Job d'analyse IA termin pour le jobId: {}", jobId);

        } catch (InterruptedException e) {
            log.error("Erreur lors de l'analyse IA", e);
            AISearchResponse response = jobTracker.get(jobId);
            if (response != null) response.setStatus("FAILED");
        }
    }
}
