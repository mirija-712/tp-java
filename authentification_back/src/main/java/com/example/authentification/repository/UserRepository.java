package com.example.authentification.repository;

import com.example.authentification.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repository JPA pour l'entité User.
 * <p>
 * Objectif : fournir les opérations CRUD et des requêtes personnalisées sur la table users.
 * Spring Data JPA génère automatiquement l'implémentation à partir des noms de méthodes.
 * </p>
 */
public interface UserRepository extends JpaRepository<User, Long> {

    // Recherche un utilisateur par email (retourne Optional car peut être absent)
    Optional<User> findByEmail(String email);

    // Vérifie si un email existe déjà (pour éviter les doublons à l'inscription)
    boolean existsByEmail(String email);
}
