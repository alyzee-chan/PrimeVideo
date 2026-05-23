package com.primevideo.controller;

import com.primevideo.service.NotificationService;
import com.primevideo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final UserRepository userRepository;

    @PostMapping("/mark-all-read")
    public ResponseEntity<Void> markAllAsRead(Principal principal) {
        if (principal == null) return ResponseEntity.status(401).build();
        
        userRepository.findByEmail(principal.getName()).ifPresent(user -> {
            notificationService.markAllAsRead(user.getId());
        });
        
        return ResponseEntity.ok().build();
    }
}
