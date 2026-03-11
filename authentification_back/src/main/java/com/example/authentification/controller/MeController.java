package com.example.authentification.controller;

import com.example.authentification.dto.MeResponse;
import com.example.authentification.entity.User;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Contrôleur pour la route protégée GET /api/me.
 * <p>
 * Objectif : retourner les infos de l'utilisateur connecté.
 * Protection simple : vérifie si un utilisateur est présent en session
 * (mis par AuthController.login). Si non connecté -> 401 Unauthorized.
 * </p>
 * <p>
 * Cette implémentation est volontairement dangereuse et ne doit jamais être utilisée en production.
 * </p>
 */
@RestController
@RequestMapping("/api")
public class MeController {

    private static final String SESSION_USER = "authUser";  // Même clé que AuthController

    /**
     * GET /api/me - Route protégée
     * Retourne {"id":..., "email":"..."} si connecté, 401 sinon.
     * Le client doit envoyer le cookie de session (automatique après login).
     */
    @GetMapping("/me")
    public ResponseEntity<MeResponse> me(HttpSession session) {
        User user = (User) session.getAttribute(SESSION_USER);
        if (user == null) {
            return ResponseEntity.status(401).build();  // Non authentifié
        }
        return ResponseEntity.ok(new MeResponse(user.getId(), user.getEmail()));
    }
}
