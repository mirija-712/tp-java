package com.example.authentification.exception;

/**
 * Exception levée lorsque les données d'entrée sont invalides.
 * <p>
 * Objectif : signaler des erreurs de validation (email vide, format incorrect,
 * mot de passe trop court). Le GlobalExceptionHandler la convertit en HTTP 400.
 * </p>
 * <p>
 * Cette implémentation est volontairement dangereuse et ne doit jamais être utilisée en production.
 * </p>
 */
public class InvalidInputException extends RuntimeException {

    public InvalidInputException(String message) {
        super(message);
    }

    public InvalidInputException(String message, Throwable cause) {
        super(message, cause);
    }
}
