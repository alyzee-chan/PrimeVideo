package com.primevideo.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Gestionnaire centralisé de toutes les exceptions de l'application.
 *
 * <p>Sans ce gestionnaire, chaque exception non attrapée retournerait une page d'erreur
 * générique Spring Boot peu lisible. Avec {@code @RestControllerAdvice}, toutes les
 * exceptions remontent ici et sont transformées en réponses JSON structurées et cohérentes.</p>
 *
 * <p>Hiérarchie des erreurs gérées :</p>
 * <table>
 *   <tr><th>Exception</th><th>HTTP</th><th>Cause typique</th></tr>
 *   <tr><td>MethodArgumentNotValidException</td><td>400</td><td>Données invalides (@Valid)</td></tr>
 *   <tr><td>BusinessException</td><td>400</td><td>Règle métier violée (email déjà pris, etc.)</td></tr>
 *   <tr><td>PaymentException</td><td>422</td><td>Paiement rejeté (carte expirée, numéro invalide)</td></tr>
 *   <tr><td>ResourceNotFoundException</td><td>404</td><td>Ressource introuvable en base</td></tr>
 *   <tr><td>Exception (générique)</td><td>500</td><td>Erreur inattendue</td></tr>
 * </table>
 *
 * <p>Format JSON retourné (identique pour toutes les erreurs) :</p>
 * <pre>{@code
 * {
 *   "code": "VALIDATION_ERROR",
 *   "message": "Données invalides",
 *   "errors": { "amount": "Le montant doit être positif" },
 *   "timestamp": "2024-01-15T10:30:00"
 * }
 * }</pre>
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    // Erreurs de validation (@Valid) → HTTP 400
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationErrors(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String field = ((FieldError) error).getField();
            String message = error.getDefaultMessage();
            fieldErrors.put(field, message);
        });

        return ResponseEntity.badRequest().body(Map.of(
            "code", "VALIDATION_ERROR",
            "message", "Données invalides",
            "errors", fieldErrors,
            "timestamp", LocalDateTime.now().toString()
        ));
    }

    // Erreurs métier → HTTP 400
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Map<String, Object>> handleBusinessException(BusinessException ex) {
        log.warn("Erreur métier : {}", ex.getMessage());
        return ResponseEntity.badRequest().body(Map.of(
            "code", "BUSINESS_ERROR",
            "message", ex.getMessage(),
            "timestamp", LocalDateTime.now().toString()
        ));
    }

    // Erreurs de paiement → HTTP 422
    @ExceptionHandler(PaymentException.class)
    public ResponseEntity<Map<String, Object>> handlePaymentException(PaymentException ex) {
        log.warn("Erreur paiement : {}", ex.getMessage());
        return ResponseEntity.unprocessableEntity().body(Map.of(
            "code", "PAYMENT_ERROR",
            "message", ex.getMessage(),
            "timestamp", LocalDateTime.now().toString()
        ));
    }

    // Ressource introuvable → HTTP 404
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(ResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
            "code", "NOT_FOUND",
            "message", ex.getMessage(),
            "timestamp", LocalDateTime.now().toString()
        ));
    }

    // Ressource statique introuvable (ex: favicon.ico) → HTTP 404
    @ExceptionHandler(org.springframework.web.servlet.resource.NoResourceFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNoResourceFound(org.springframework.web.servlet.resource.NoResourceFoundException ex) {
        log.warn("Ressource statique manquante : {}", ex.getResourcePath());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
            "code", "NOT_FOUND",
            "message", "Ressource introuvable",
            "timestamp", LocalDateTime.now().toString()
        ));
    }

    // Erreur inattendue → HTTP 500
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex) {
        log.error("Erreur inattendue : ", ex);
        return ResponseEntity.internalServerError().body(Map.of(
            "code", "INTERNAL_ERROR",
            "message", ex.getClass().getName() + " : " + ex.getMessage(),
            "timestamp", LocalDateTime.now().toString()
        ));
    }
}
