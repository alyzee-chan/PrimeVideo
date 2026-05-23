package com.primevideo.dto.response;

import lombok.*;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class KDramaMetadataDTO {
    private String koreanTitle;
    private String network;
    private Integer episodeCount;
    private Boolean isWebtoonAdapted;
    private String airDays;
    private String ost;
}
