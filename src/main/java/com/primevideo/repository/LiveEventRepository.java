package com.primevideo.repository;

import com.primevideo.entity.LiveEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface LiveEventRepository extends JpaRepository<LiveEvent, Long> {
    List<LiveEvent> findByStatus(LiveEvent.LiveEventStatus status);
    List<LiveEvent> findByStartTimeBetween(LocalDateTime start, LocalDateTime end);
}
