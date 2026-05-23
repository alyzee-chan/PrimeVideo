package com.primevideo.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Historique de visionnage d'un profil.
 *
 * <p>Correspond à la table {@code viewing_history}. Stocke où en est l'utilisateur
 * dans chaque contenu ({@code progressSeconds}) pour permettre la reprise automatique.
 * Si {@code episode} est null, il s'agit d'un film ; sinon d'un épisode de série.</p>
 */
@Entity
@Table(name = "viewing_history")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ViewingHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_id", nullable = false)
    private Profile profile;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "content_id", nullable = false)
    private Content content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "episode_id")
    private Episode episode;   // Null si c'est un film

    @Column(name = "progress_seconds")
    @Builder.Default
    private Integer progressSeconds = 0;   // Où en est l'utilisateur dans le visionnage

    @Column(name = "completed")
    @Builder.Default
    private Boolean completed = false;

    @UpdateTimestamp
    @Column(name = "last_watched_at")
    private LocalDateTime lastWatchedAt;
}
