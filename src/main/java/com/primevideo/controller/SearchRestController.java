package com.primevideo.controller;

import com.primevideo.dto.response.ContentDTO;
import com.primevideo.service.CatalogService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
public class SearchRestController {

    private final CatalogService catalogService;

    @GetMapping
    public List<ContentDTO> searchAutocomplete(@RequestParam String q) {
        if (q == null || q.trim().length() < 2) {
            return List.of();
        }
        // Limit results to 5 for autocomplete
        return catalogService.search(q.trim(), 0, 5).getContent();
    }
}
