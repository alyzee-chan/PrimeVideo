package com.primevideo.controller;

import com.primevideo.service.ChatbotService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/chatbot")
@RequiredArgsConstructor
public class ChatbotController {

    private final ChatbotService chatbotService;

    @PostMapping("/ask")
    public Map<String, String> ask(@RequestBody Map<String, String> request) {
        String userMessage = request.get("message");
        String response = chatbotService.getResponse(userMessage);
        return Map.of("response", response);
    }

    @PostMapping("/transcribe")
    public Map<String, String> transcribe(@RequestParam("audio") org.springframework.web.multipart.MultipartFile audioFile) throws java.io.IOException {
        String text = chatbotService.transcribe(audioFile.getBytes());
        return Map.of("text", text);
    }
}
