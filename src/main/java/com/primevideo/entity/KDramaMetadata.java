package com.primevideo.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "kdrama_metadata")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class KDramaMetadata {

    @Id
    @Column(name = "content_id")
    private Long contentId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "content_id", insertable = false, updatable = false)
    private Content content;

    private String koreanTitle;
    private String network; // tvN, JTBC, etc.
    private Integer episodeCount;
    private Boolean isWebtoonAdapted;
    private String airDays; // e.g., "Samedi & Dimanche"
    
    @Column(columnDefinition = "TEXT")
    private String ost; // Original Sound Track details
}
