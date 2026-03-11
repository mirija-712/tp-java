package com.example.authentification.dto;

/**
 * Réponse pour GET /api/me (route protégée).
 * <p>
 * Objectif : retourner uniquement les infos publiques de l'utilisateur (id, email).
 * On n'expose jamais le mot de passe dans les réponses API.
 * </p>
 */
public record MeResponse(Long id, String email) {}
