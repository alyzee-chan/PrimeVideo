package com.primevideo.repository;

import com.primevideo.entity.Content;
import com.primevideo.entity.Episode;
import com.primevideo.entity.Profile;
import com.primevideo.entity.ViewingHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ViewingHistoryRepository extends JpaRepository<ViewingHistory, Long> {
    Optional<ViewingHistory> findByProfileAndContentAndEpisode(Profile profile, Content content, Episode episode);
    
    java.util.List<ViewingHistory> findByProfileIdOrderByLastWatchedAtDesc(Long profileId);
    
    Optional<ViewingHistory> findByProfileIdAndContentId(Long profileId, Long contentId);
    
    void deleteByProfileIdAndContentId(Long profileId, Long contentId);
}
