package com.example.authentification.exception;

/**
 * Exception levée lorsque l'authentification échoue (email ou mot de passe incorrect).
 * <p>
 * Objectif : signaler un échec de login. Le GlobalExceptionHandler la convertit en HTTP 401.
 * Le message générique "Email ou mot de passe incorrect" évite de révéler si l'email existe.
 * </p>
 * <p>
 * Cette implémentation est volontairement dangereuse et ne doit jamais être utilisée en production.
 * </p>
 */
public class AuthenticationFailedException extends RuntimeException {

    public AuthenticationFailedException(String message) {
        super(message);
    }

    public AuthenticationFailedException(String message, Throwable cause) {
        super(message, cause);
    }
}
