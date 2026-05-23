package com.primevideo.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Notification envoyée à un utilisateur.
 *
 * <p>Correspond à la table {@code notifications}. Peut être de type INFO,
 * NEW_CONTENT (nouvelle sortie), PAYMENT (confirmation), SUBSCRIPTION ou REMINDER.
 * Le champ {@code isRead} permet de distinguer les notifications lues des non lues.</p>
 */
@Entity
@Table(name = "notifications")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private NotificationType type = NotificationType.INFO;

    @Column(name = "is_read")
    @Builder.Default
    private Boolean isRead = false;

    @Column(name = "link_url")
    private String linkUrl;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public enum NotificationType {
        INFO, NEW_CONTENT, PAYMENT, SUBSCRIPTION, REMINDER
    }
}
