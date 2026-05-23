package com.primevideo.exception;

/**
 * Exception levée quand une ressource demandée n'existe pas en base de données.
 *
 * <p>Exemples : {@code GET /catalog/999} quand le contenu id=999 n'existe pas,
 * recherche d'un utilisateur par id inconnu.</p>
 *
 * <p>Interceptée par {@link GlobalExceptionHandler} → retourne HTTP 404 Not Found.</p>
 */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) { super(message); }
}
