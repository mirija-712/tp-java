-- =============================================================================
-- Data SQL : exécuté après schema.sql au démarrage
-- Objectif : insérer le compte de test obligatoire pour le TP1
-- INSERT IGNORE : ne fait rien si l'email existe déjà (évite les erreurs en redémarrage)
-- =============================================================================

-- Compte de test : toto@example.com / pwd1234
INSERT IGNORE INTO users (email, password_clear) VALUES ('toto@example.com', 'pwd1234');
