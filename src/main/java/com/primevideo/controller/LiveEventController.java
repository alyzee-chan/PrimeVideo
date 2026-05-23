package com.primevideo.controller;

import com.primevideo.entity.LiveEvent;
import com.primevideo.repository.LiveEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/live")
@RequiredArgsConstructor
public class LiveEventController {

    private final LiveEventRepository liveEventRepository;

    @GetMapping
    public ResponseEntity<List<LiveEvent>> getAllLiveEvents() {
        return ResponseEntity.ok(liveEventRepository.findAll());
    }

    @GetMapping("/active")
    public ResponseEntity<List<LiveEvent>> getActiveLiveEvents() {
        return ResponseEntity.ok(liveEventRepository.findByStatus(LiveEvent.LiveEventStatus.LIVE));
    }

    @GetMapping("/{id}")
    public ResponseEntity<LiveEvent> getLiveEventById(@PathVariable Long id) {
        return liveEventRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
