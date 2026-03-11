package com.example.authentification.dto;

/**
 * DTO pour la connexion.
 * <p>
 * Objectif : structurer les données JSON reçues par POST /api/auth/login.
 * </p>
 */
public record LoginRequest(String email, String password) {}
