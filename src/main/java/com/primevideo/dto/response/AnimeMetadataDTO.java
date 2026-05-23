package com.primevideo.dto.response;

import com.primevideo.entity.AnimeMetadata;
import lombok.*;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class AnimeMetadataDTO {
    private String japaneseTitle;
    private String romajiTitle;
    private AnimeMetadata.AnimeSubGenre subGenre;
    private String studio;
    private Integer episodeCount;
    private Integer episodeDurationMinutes;
    private AnimeMetadata.AirStatus airStatus;
}
