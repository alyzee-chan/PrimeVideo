package com.primevideo.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class SeasonDTO {
    private Long id;
    private Integer seasonNumber;
    private String title;
    private String description;
    private String posterUrl;
    private Integer releaseYear;
    private List<EpisodeDTO> episodes;
}
