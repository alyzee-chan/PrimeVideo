package com.primevideo.exception;

/**
 * Exception levée quand une règle métier est violée.
 *
 * <p>Exemples : email déjà utilisé à l'inscription, mots de passe différents,
 * profil inexistant, tentative d'action non autorisée.</p>
 *
 * <p>Interceptée par {@link GlobalExceptionHandler} → retourne HTTP 400 Bad Request.</p>
 */
public class BusinessException extends RuntimeException {
    public BusinessException(String message) { super(message); }
}
