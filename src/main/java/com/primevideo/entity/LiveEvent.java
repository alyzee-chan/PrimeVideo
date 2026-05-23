package com.primevideo.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "live_events")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class LiveEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    private LiveEventType type;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Enumerated(EnumType.STRING)
    private LiveEventStatus status;

    @Column(name = "thumbnail_url")
    private String thumbnailUrl;

    @Column(name = "is_prime")
    @Builder.Default
    private Boolean isPrime = true;

    @Column(name = "viewer_count")
    private Integer viewerCount;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public enum LiveEventType {
        SPORT, CONCERT, CEREMONY, NEWS, TALK_SHOW, OTHER
    }

    public enum LiveEventStatus {
        SCHEDULED, LIVE, REPLAY_AVAILABLE, ENDED
    }
}
