### TP3 – Authentification Forte

**But du TP3**

Changer le **protocole d’authentification** : le mot de passe ne doit plus être transmis dans la requête de login (même pas sous forme hachée).  
Le client doit **prouver** qu’il connaît un secret, sans l’envoyer directement, via un protocole **HMAC + nonce + timestamp** et un **token d’accès**.

Durée estimée : **10 h** – Tag Git final attendu : **`v3-tp3`**  
Objectif couverture de tests : **≥ 80 %**

---

### 1. Concepts et nouveautés

- **Concepts introduits**
  - Clé secrète partagée (dérivée du mot de passe).
  - **HMAC** (HMAC-SHA256).
  - **Nonce** (valeur aléatoire unique).
  - **Timestamp**.
  - **Comparaison en temps constant**.
  - **Protection anti-rejeu**.

- **Hypothèses**
  - TLS est activé et fonctionnel (supposé, non configuré dans le TP).
  - Le serveur stocke un mot de passe **chiffré réversible** en base.
  - Une **Server Master Key (SMK)** est utilisée pour chiffrer/déchiffrer les mots de passe.

---

### 2. Protocole SSO en 2 étapes

#### Étape 1 – Côté client : préparation de la preuve

- Le client collecte :
  - `email`, `password` (saisi).
  - `nonce` (UUID aléatoire).
  - `timestamp` (epoch en secondes).

- Message à signer :
  - `message = email + ":" + nonce + ":" + timestamp`

- Signature HMAC :
  - `hmac = HMAC_SHA256(key = password, data = message)`

- Requête envoyée :
  - `POST /api/auth/login`
  - Payload JSON :
    - `email`, `nonce`, `timestamp`, `hmac`

#### Étape 2 – Côté serveur : vérification de la preuve

1. Vérifier que l’**email existe** → sinon **401**.
2. Vérifier que le **timestamp** est dans une fenêtre acceptable, par ex. ± 60 s → sinon **401**.
3. Vérifier le **nonce anti-rejeu** :
   - Refuser (401) si nonce déjà vu pour cet utilisateur.
4. Réserver / enregistrer le nonce (pour empêcher sa réutilisation).
5. Récupérer le **mot de passe en clair** (décryptage via SMK).
6. Recalculer la signature :
   - `message = email + ":" + nonce + ":" + timestamp`
   - `hmac_expected = HMAC_SHA256(key = password_plain, data = message)`
7. Comparer `hmac_expected` et `hmac` **en temps constant** → si différent, 401.
8. Marquer le nonce comme **consommé** (non réutilisable).
9. Émettre un **token SSO** et retourner :
   - `accessToken`
   - `expiresAt`

- Côté client : afficher/stocker l’`accessToken` reçu.

---

### 3. Considérations techniques

- **Table `users`**
  - `id`
  - `email`
  - `password_encrypted`
  - `created_at`

- **Table `auth_nonce`**
  - `id`
  - `user_id`
  - `nonce`
  - `expires_at`
  - `consumed`
  - `created_at`

- **Contraintes**
  - `(user_id, nonce)` unique.
  - `expires_at ≈ now + 2 minutes`.

- **Durées recommandées**
  - Fenêtre timestamp : **± 60 secondes**.
  - TTL du nonce en base : **120 secondes**.
  - Durée de vie de l’access token : **15 minutes**.

---

### 4. Exigences qualité et tests (TP3)

- **JUnit**
  - Minimum **15 tests**.
  - **Tests obligatoires** :
    - Login OK avec HMAC valide.
    - Login KO si HMAC invalide.
    - KO si timestamp **expiré**.
    - KO si timestamp **trop futur**.
    - KO si **nonce déjà utilisé**.
    - KO si **user inconnu**.
    - Comparaison en **temps constant** testée.
    - Token émis et accès `/api/me` **OK**.
    - Accès `/api/me` **sans token KO**.

- **SonarCloud**
  - Couverture **minimum 80 %**.
  - Quality gate **doit passer**.

- **JavaDoc**
  - Documenter le protocole et les **limites du chiffrement réversible**.

---

### 5. README (compléments TP3)

Dans le README principal, ajouter une section TP3 qui explique :

- Les **avantages** du protocole :
  - Aucun mot de passe (clair ou hashé) ne circule sur le réseau.
  - Le timestamp **limite la durée** d’une attaque.
  - Le nonce **empêche la réutilisation**.
  - Sans stockage du nonce, le nonce ne sert pas contre le rejeu (à expliquer).

- Les **limites** :
  - Mécanisme pédagogique.
  - En industrie, on évite de stocker un mot de passe **réversible**.
  - On préférerait un hash **non réversible et adaptatif**.
  - Ici, on accepte le chiffrement réversible pour simplifier l’apprentissage.

---

### 6. Git : étapes et tags obligatoires pour le TP3

- **`v3.0-start`**
  - Démarrage du TP3, mise à jour du README (objectifs TP3).

- **`v3.1-db-nonce`**
  - Création de la table `auth_nonce` (ou équivalent) + migration.

- **`v3.2-hmac-client`**
  - Implémentation du calcul HMAC côté client.

- **`v3.3-hmac-server`**
  - Implémentation de la vérification HMAC côté serveur.

- **`v3.4-anti-replay`**
  - Gestion complète du nonce anti-rejeu (stockage, TTL, consumed, vérifications).

- **`v3.5-token`**
  - Génération de l’`accessToken` et sécurisation de `/api/me` avec ce token.

- **`v3.6-tests-80`**
  - Écriture des tests, couverture ≥ 80 %, SonarCloud OK.

- **Tag final : `v3-tp3`**

