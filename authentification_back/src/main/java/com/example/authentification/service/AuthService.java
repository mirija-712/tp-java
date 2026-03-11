package com.example.authentification.service;

import com.example.authentification.dto.LoginRequest;
import com.example.authentification.dto.RegisterRequest;
import com.example.authentification.entity.User;
import com.example.authentification.exception.AuthenticationFailedException;
import com.example.authentification.exception.InvalidInputException;
import com.example.authentification.exception.ResourceConflictException;
import com.example.authentification.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.regex.Pattern;

/**
 * Service métier d'authentification (couche logique).
 * <p>
 * Objectif : centraliser la logique d'inscription, de connexion et de validation.
 * Les contrôleurs délèguent à ce service. Séparation des responsabilités.
 * </p>
 * <p>
 * Cette implémentation est volontairement dangereuse et ne doit jamais être utilisée en production.
 * Les mots de passe sont stockés en clair et les règles de validation sont minimales.
 * </p>
 */
@Service  // Bean Spring injectable
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);
    private static final int MIN_PASSWORD_LENGTH = 4;  // Règle TP1 : minimum 4 caractères
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^@]+@[^@]+\\.[^@]+$");  // Format xxx@yyy.zzz

    private final UserRepository userRepository;

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Inscription d'un nouvel utilisateur.
     * Valide les données, vérifie l'unicité de l'email, puis sauvegarde.
     *
     * @param request email et mot de passe
     * @return l'utilisateur créé
     */
    public User register(RegisterRequest request) {
        validateEmail(request.email());
        validatePassword(request.password());

        // Vérifier que l'email n'existe pas déjà
        if (userRepository.existsByEmail(request.email())) {
            log.warn("Inscription échouée : email déjà existant pour {}", request.email());
            throw new ResourceConflictException("Cet email est déjà utilisé");
        }

        // Créer et sauvegarder l'utilisateur (mot de passe en clair - dangereux !)
        User user = new User(request.email(), request.password());
        user = userRepository.save(user);
        log.info("Inscription réussie pour {}", request.email());  // Ne JAMAIS logger le mot de passe
        return user;
    }

    /**
     * Connexion d'un utilisateur.
     * Vérifie que l'email existe et que le mot de passe correspond.
     *
     * @param request email et mot de passe
     * @return l'utilisateur authentifié
     */
    public User login(LoginRequest request) {
        validateEmail(request.email());
        validatePassword(request.password());

        // Chercher l'utilisateur par email
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> {
                    log.warn("Connexion échouée : email inconnu {}", request.email());
                    return new AuthenticationFailedException("Email ou mot de passe incorrect");
                });

        // Comparer le mot de passe en clair (dangereux ! En prod : BCrypt)
        if (!user.getPasswordClear().equals(request.password())) {
            log.warn("Connexion échouée : mot de passe incorrect pour {}", request.email());
            throw new AuthenticationFailedException("Email ou mot de passe incorrect");
        }

        log.info("Connexion réussie pour {}", request.email());
        return user;
    }

    // Valide le format de l'email (non vide, format xxx@yyy.zzz)
    private void validateEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new InvalidInputException("L'email est requis");
        }
        if (!EMAIL_PATTERN.matcher(email.trim()).matches()) {
            throw new InvalidInputException("Format d'email invalide");
        }
    }

    // Valide la longueur minimale du mot de passe (4 caractères pour le TP1)
    private void validatePassword(String password) {
        if (password == null || password.length() < MIN_PASSWORD_LENGTH) {
            throw new InvalidInputException("Le mot de passe doit contenir au minimum " + MIN_PASSWORD_LENGTH + " caractères");
        }
    }
}
