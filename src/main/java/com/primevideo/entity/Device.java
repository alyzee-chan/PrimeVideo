package com.primevideo.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Entité représentant un appareil connecté au compte utilisateur.
 */
@Entity
@Table(name = "devices")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Device {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "device_id", nullable = false)
    private String deviceId; // Identifiant unique de l'appareil (ex: UUID)

    private String name; // Nom de l'appareil (ex: TV Samsung Salon)

    @Enumerated(EnumType.STRING)
    private DeviceType type;

    private String os; // OS de l'appareil

    @Column(name = "last_seen_at")
    private LocalDateTime lastSeenAt;

    private String location; // Localisation approximative (ex: Paris, FR)

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public enum DeviceType {
        SMART_TV, PHONE, TABLET, COMPUTER, GAME_CONSOLE, FIRE_TV, CHROMECAST, APPLE_TV, OTHER
    }
}
