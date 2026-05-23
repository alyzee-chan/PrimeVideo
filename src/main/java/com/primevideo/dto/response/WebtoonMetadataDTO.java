package com.primevideo.dto.response;

import lombok.*;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class WebtoonMetadataDTO {
    private String originalTitle;
    private String author;
    private String artist;
    private String platform;
    private String platformUrl;
}
