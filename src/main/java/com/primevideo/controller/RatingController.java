package com.primevideo.controller;

import com.primevideo.dto.request.RatingRequest;
import com.primevideo.dto.response.ReviewDTO;
import com.primevideo.service.RatingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/catalog/contents/{contentId}")
@RequiredArgsConstructor
public class RatingController {

    private final RatingService ratingService;

    @GetMapping("/reviews")
    public ResponseEntity<List<ReviewDTO>> getReviews(@PathVariable Long contentId) {
        return ResponseEntity.ok(ratingService.getContentReviews(contentId));
    }

    @PostMapping("/ratings")
    public ResponseEntity<Void> postRating(
            @PathVariable Long contentId,
            @Valid @RequestBody RatingRequest request) {
        ratingService.addOrUpdateRating(contentId, request);
        return ResponseEntity.ok().build();
    }
}
