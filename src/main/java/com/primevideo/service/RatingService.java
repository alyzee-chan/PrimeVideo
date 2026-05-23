package com.primevideo.service;

import com.primevideo.dto.request.RatingRequest;
import com.primevideo.dto.response.ReviewDTO;
import com.primevideo.entity.Content;
import com.primevideo.entity.Profile;
import com.primevideo.entity.Rating;
import com.primevideo.repository.ContentRepository;
import com.primevideo.repository.ProfileRepository;
import com.primevideo.repository.RatingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RatingService {

    private final RatingRepository ratingRepository;
    private final ProfileRepository profileRepository;
    private final ContentRepository contentRepository;

    public List<ReviewDTO> getContentReviews(Long contentId) {
        return ratingRepository.findByContentIdOrderByCreatedAtDesc(contentId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public void addOrUpdateRating(Long contentId, RatingRequest request) {
        Profile profile = profileRepository.findById(request.getProfileId()).orElseThrow(() -> new RuntimeException("Profil non trouvé"));
        Content content = contentRepository.findById(contentId).orElseThrow(() -> new RuntimeException("Contenu non trouvé"));

        Rating rating = ratingRepository.findByProfileIdAndContentId(request.getProfileId(), contentId)
                .orElse(Rating.builder()
                        .profile(profile)
                        .content(content)
                        .build());

        rating.setScore(request.getScore());
        rating.setComment(request.getComment());

        ratingRepository.save(rating);
        log.info("Avis ajout/mis  jour par le profil {} sur le contenu {}", request.getProfileId(), contentId);
    }

    public Double getAverageRating(Long contentId) {
        Double avg = ratingRepository.findAverageScoreByContentId(contentId);
        return avg != null ? Math.round(avg * 10.0) / 10.0 : 0.0;
    }

    public long getReviewCount(Long contentId) {
        return ratingRepository.countByContentId(contentId);
    }

    private ReviewDTO toDTO(Rating r) {
        return ReviewDTO.builder()
                .id(r.getId())
                .profileName(r.getProfile().getName())
                .profileAvatar(r.getProfile().getAvatarUrl())
                .score(r.getScore())
                .comment(r.getComment())
                .createdAt(r.getCreatedAt())
                .build();
    }
}
