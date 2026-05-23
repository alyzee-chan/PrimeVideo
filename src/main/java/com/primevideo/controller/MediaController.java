package com.primevideo.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.File;

@Controller
@Slf4j
public class MediaController {

    @GetMapping("/media/{filename:.+}")
    @ResponseBody
    public ResponseEntity<Resource> serveVideo(@PathVariable String filename) {

        String projectDir = System.getProperty("user.dir");
        try {
            filename = java.net.URLDecoder.decode(filename, java.nio.charset.StandardCharsets.UTF_8.name());
        } catch (Exception e) {
            log.error("Failed to decode filename: {}", filename);
        }
        File file = new File(projectDir + File.separator + "BD-Prime", filename);

        if (!file.exists()) {
            log.error("❌ [MEDIA ERROR] Fichier introuvable : {}", file.getAbsolutePath());
            return ResponseEntity.notFound().build();
        }

        log.info("🎥 [SERVE] {} | Taille: {} bytes", filename, file.length());

        Resource resource = new FileSystemResource(file);
        MediaType mediaType = MediaTypeFactory.getMediaType(resource).orElse(MediaType.APPLICATION_OCTET_STREAM);

        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(HttpHeaders.ACCEPT_RANGES, "bytes")
                .body(resource);
    }
}
