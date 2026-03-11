-- =============================================================================
-- Schema SQL : créé au démarrage de l'application (spring.sql.init.mode=always)
-- Objectif : créer la table users si elle n'existe pas
-- =============================================================================

-- id : clé primaire auto-incrémentée
-- email : unique (pas de doublon), obligatoire
-- password_clear : mot de passe en clair (dangereux - TP1 volontairement non sécurisé)
-- created_at : date de création (rempli automatiquement)
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_clear VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
