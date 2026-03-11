package com.example.authentification.dto;

/**
 * Réponse générique pour login et register.
 * <p>
 * Objectif : format uniforme des réponses success {"success":true, "message":"..."}.
 * </p>
 */
public record AuthResponse(boolean success, String message) {}
