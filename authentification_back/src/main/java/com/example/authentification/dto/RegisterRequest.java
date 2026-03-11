package com.example.authentification.dto;

/**
 * DTO (Data Transfer Object) pour l'inscription.
 * <p>
 * Objectif : structurer les données JSON reçues par POST /api/auth/register.
 * Record Java 16+ : classe immuable avec constructeur et getters auto-générés.
 * </p>
 */
public record RegisterRequest(String email, String password) {}
