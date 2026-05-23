package com.primevideo.controller;

import com.primevideo.dto.response.AISearchResponse;
import com.primevideo.service.AISearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Controller
@RequiredArgsConstructor
public class AISearchController {

    private final AISearchService aiSearchService;

    @GetMapping("/search/by-image")
    public String imageSearchPage() {
        return "search/image-search";
    }

    @PostMapping("/api/search/by-image")
    @ResponseBody
    public ResponseEntity<Map<String, String>> uploadImage(@RequestParam("file") MultipartFile file) {
        String jobId = aiSearchService.submitJob(file);
        return ResponseEntity.ok(Map.of("jobId", jobId));
    }

    @GetMapping("/api/search/by-image/status/{jobId}")
    @ResponseBody
    public ResponseEntity<AISearchResponse> getStatus(@PathVariable String jobId) {
        return ResponseEntity.ok(aiSearchService.getJobStatus(jobId));
    }
}
