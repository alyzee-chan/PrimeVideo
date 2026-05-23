package com.primevideo.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "webtoon_metadata")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class WebtoonMetadata {

    @Id
    @Column(name = "content_id")
    private Long contentId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "content_id", insertable = false, updatable = false)
    private Content content;

    private String originalTitle;
    private String author;
    private String artist;
    private String platform; // LINE WEBTOON, KAKAOPAGE, etc.
    private String platformUrl;
}
