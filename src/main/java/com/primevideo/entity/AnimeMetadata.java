package com.primevideo.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "anime_metadata")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AnimeMetadata {

    @Id
    @Column(name = "content_id")
    private Long contentId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "content_id", insertable = false, updatable = false)
    private Content content;

    private String japaneseTitle;
    private String romajiTitle;
    
    @Enumerated(EnumType.STRING)
    private AnimeSubGenre subGenre;

    private String studio;
    private Integer episodeCount;
    private Integer episodeDurationMinutes;

    @Enumerated(EnumType.STRING)
    private AirStatus airStatus;

    public enum AnimeSubGenre {
        SHONEN, SHOJO, SEINEN, JOSEI, ISEKAI, MECHA, SLICE_OF_LIFE, SPORTS, FANTASY, MYSTERY, CLASSICS
    }

    public enum AirStatus {
        ONGOING, COMPLETED, UPCOMING, HIATUS
    }
}
