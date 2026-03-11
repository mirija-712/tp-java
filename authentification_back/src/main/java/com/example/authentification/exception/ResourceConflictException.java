package com.example.authentification.exception;

/**
 * Exception levée lorsqu'une ressource existe déjà (ex. email déjà utilisé à l'inscription).
 * <p>
 * Objectif : signaler un conflit (doublon). Le GlobalExceptionHandler la convertit en HTTP 409.
 * </p>
 * <p>
 * Cette implémentation est volontairement dangereuse et ne doit jamais être utilisée en production.
 * </p>
 */
public class ResourceConflictException extends RuntimeException {

    public ResourceConflictException(String message) {
        super(message);
    }

    public ResourceConflictException(String message, Throwable cause) {
        super(message, cause);
    }
}
