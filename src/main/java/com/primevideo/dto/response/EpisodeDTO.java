package com.primevideo.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class EpisodeDTO {
    private Long id;
    private Integer episodeNumber;
    private String title;
    private String description;
    private Integer durationSeconds;
    private String videoUrl;
    private String thumbnailUrl;
    private String durationFormatted;
}
