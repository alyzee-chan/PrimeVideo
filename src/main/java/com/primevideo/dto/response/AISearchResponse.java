package com.primevideo.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AISearchResponse {
    private String jobId;
    private String status; // PENDING, PROCESSING, COMPLETED, FAILED
    private List<MatchResult> matches;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MatchResult {
        private Long contentId;
        private String title;
        private String type;
        private Double confidence; // 0.0 to 1.0
        private String reason; // e.g., "Affiche identifi", "Acteur dtect"
    }
}
