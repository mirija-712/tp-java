package com.example.authentification.controller;

import com.example.authentification.dto.*;
import com.example.authentification.entity.User;
import com.example.authentification.service.AuthService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Contrôleur REST pour l'authentification (inscription et connexion).
 * <p>
 * Objectif : exposer les endpoints POST /api/auth/register et POST /api/auth/login.
 * Reçoit les requêtes HTTP, délègue au service, retourne des réponses JSON.
 * Utilise HttpSession pour stocker l'utilisateur connecté (cookie JSESSIONID).
 * </p>
 * <p>
 * Cette implémentation est volontairement dangereuse et ne doit jamais être utilisée en production.
 * </p>
 */
@RestController
@RequestMapping("/api/auth")  // Toutes les routes commencent par /api/auth
public class AuthController {

    // Clé pour stocker l'utilisateur en session (après login)
    private static final String SESSION_USER = "authUser";

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Inscription : POST /api/auth/register
     * Body JSON : {"email":"...", "password":"..."}
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest request) {
        authService.register(request);
        return ResponseEntity.ok(new AuthResponse(true, "Inscription réussie"));
    }

    /**
     * Connexion : POST /api/auth/login
     * En cas de succès, stocke l'utilisateur en session pour que /api/me fonctionne.
     * La session crée un cookie JSESSIONID envoyé automatiquement par le client.
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request, HttpSession session) {
        User user = authService.login(request);
        session.setAttribute(SESSION_USER, user);  // Lie l'utilisateur à la session
        return ResponseEntity.ok(new AuthResponse(true, "Connexion réussie"));
    }
}
