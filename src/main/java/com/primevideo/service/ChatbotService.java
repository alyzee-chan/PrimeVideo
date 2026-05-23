package com.primevideo.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

@Service
@RequiredArgsConstructor
public class ChatbotService {

    @Value("${app.chatbot.api-key}")
    private String apiKey;

    @Value("${app.chatbot.api-url}")
    private String apiUrl;

    @Value("${app.chatbot.model}")
    private String model;

    private final RestTemplate restTemplate = new RestTemplate();

    public String getResponse(String userMessage) {
        String url = apiUrl;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + apiKey);

        // System prompt to act as Prime Video assistant
        String systemContext = "Tu es l'assistant IA de Prime Video. Ton but est d'aider les utilisateurs à trouver des films, gérer leur compte et expliquer les fonctionnalités (Watchlist, Reprise de lecture, etc.). Sois poli, concis et cinéphile.";

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", model);
        
        List<Map<String, String>> messages = List.of(
            Map.of("role", "system", "content", systemContext),
            Map.of("role", "user", "content", userMessage)
        );
        requestBody.put("messages", messages);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            ParameterizedTypeReference<Map<String, Object>> typeRef = new ParameterizedTypeReference<>() {};
            ResponseEntity<Map<String, Object>> responseEntity = restTemplate.exchange(url, HttpMethod.POST, entity, typeRef);
            Map<String, Object> response = responseEntity.getBody();

            if (response != null && response.containsKey("choices")) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
                Map<String, Object> firstChoice = choices.get(0);
                @SuppressWarnings("unchecked")
                Map<String, Object> message = (Map<String, Object>) firstChoice.get("message");
                return (String) message.get("content");
            }
            return "Désolé, je ne peux pas répondre pour le moment.";
        } catch (Exception e) {
            return "Une erreur est survenue lors de la communication avec l'IA : " + e.getMessage();
        }
    }

    public String transcribe(byte[] audioData) {
        String url = "https://api.groq.com/openai/v1/audio/transcriptions";
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.set("Authorization", "Bearer " + apiKey);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("model", "whisper-large-v3");
        
        ByteArrayResource contentsAsResource = new ByteArrayResource(audioData) {
            @Override
            public String getFilename() { return "audio.webm"; }
        };
        body.add("file", contentsAsResource);

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        try {
            ParameterizedTypeReference<Map<String, Object>> typeRef = new ParameterizedTypeReference<>() {};
            ResponseEntity<Map<String, Object>> responseEntity = restTemplate.exchange(url, HttpMethod.POST, requestEntity, typeRef);
            Map<String, Object> response = responseEntity.getBody();
            return (response != null && response.containsKey("text")) ? (String) response.get("text") : "";
        } catch (Exception e) {
            return "Transcription error: " + e.getMessage();
        }
    }

    /**
     * Analyse le contenu visuel d'une image via Groq Vision (Llama 3.2 Vision)
     */
    public String analyzeImageContent(byte[] imageBytes) {
        String url = apiUrl; // https://api.groq.com/openai/v1/chat/completions
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + apiKey);

        String base64Image = java.util.Base64.getEncoder().encodeToString(imageBytes);
        String dataUrl = "data:image/jpeg;base64," + base64Image;

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "meta-llama/llama-4-scout-17b-16e-instruct");
        
        Map<String, Object> userMessage = new HashMap<>();
        userMessage.put("role", "user");
        
        List<Map<String, Object>> content = List.of(
            Map.of("type", "text", "text", "Analyse cette image. Retourne UNIQUEMENT le titre du film, de la série ou du personnage principal. Si tu n'es pas sûr, donne 2 ou 3 mots-clés essentiels séparés par des espaces. NE FAIS AUCUNE PHRASE, NE DIS PAS 'Voici...', DONNE JUSTE LE NOM."),
            Map.of("type", "image_url", "image_url", Map.of("url", dataUrl))
        );
        userMessage.put("content", content);
        
        requestBody.put("messages", List.of(userMessage));
        requestBody.put("max_tokens", 20);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            ParameterizedTypeReference<Map<String, Object>> typeRef = new ParameterizedTypeReference<>() {};
            ResponseEntity<Map<String, Object>> responseEntity = restTemplate.exchange(url, HttpMethod.POST, entity, typeRef);
            Map<String, Object> response = responseEntity.getBody();

            if (response != null && response.containsKey("choices")) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
                Map<String, Object> firstChoice = choices.get(0);
                @SuppressWarnings("unchecked")
                Map<String, Object> message = (Map<String, Object>) firstChoice.get("message");
                String aiText = (String) message.get("content");
                
                // Nettoyage agressif : on retire les points finaux et les guillemets
                return aiText.replace("\"", "").replace(".", "").trim();
            }
            return "Inconnu";
        } catch (Exception e) {
            return "Erreur analyse image: " + e.getMessage();
        }
    }
}
