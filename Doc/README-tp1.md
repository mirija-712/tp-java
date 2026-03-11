### TP1 – Serveur d’Authentification Dangereuse

**But du TP1**

Construire une **première version volontairement dangereuse** d’un serveur d’authentification en Java/Spring Boot + MySQL, qui servira de base aux TP2–TP4. Cette version fonctionne mais serait **inacceptable en production**.

Durée estimée : **10 h** – Tag Git final attendu : **`v1-tp1`**

---

### 1. Architecture imposée

- **Technos**
  - **Client lourd Java** : Swing, JavaFX ou WindowBuilder.
  - **Back-end** : Spring Boot (API REST) + base **MySQL**.

- **Fonctionnalités côté client**
  - **Inscription** : formulaire pour créer un compte avec `email` + `mot de passe`.
  - Règles mot de passe côté serveur : **minimum 4 caractères**, pas d’autre règle.

- **Fonctionnalités côté serveur**
  - Email **unique**.
  - Mot de passe **stocké en clair** dans la base.
  - Endpoint `POST /api/auth/login` :
    - Vérifie email + mot de passe.
    - Retourne succès/échec.
  - Compte de test **obligatoire** :
    - `toto@example.com`
    - `pwd1234`
  - Route protégée `GET /api/me` :
    - Accessible **uniquement** si l’utilisateur est authentifié.
  - Mécanisme d’auth possible :
    - Soit **session HTTP simple**, soit **token basique** généré côté serveur et stocké en base.

---

### 2. Modèle de données et structure Spring

- **Table `users` minimale**
  - `id`
  - `email`
  - `password_clear`
  - `created_at`

- **Structure de packages Spring recommandée**
  - `com.example.auth`
    - `controller`
    - `service`
    - `repository`
    - `entity`
    - `exception`

- **Gestion des exceptions (obligatoire)**
  - Exceptions personnalisées au minimum :
    - `InvalidInputException`
    - `AuthenticationFailedException`
    - `ResourceConflictException`
  - Mettre en place un `@ControllerAdvice` qui renvoie des réponses JSON cohérentes :
    - `timestamp`
    - `status`
    - `error`
    - `message`
    - `path`
  - **Codes HTTP attendus**
    - `400` si données invalides
    - `401` si login échoue
    - `409` si email déjà existant

---

### 3. Qualité logicielle obligatoire

- **Tests JUnit (minimum 8 tests)**
  - Validation email (vide, format incorrect).
  - Validation mot de passe (< 4 caractères).
  - Inscription OK.
  - Inscription refusée si email déjà existant.
  - Login OK.
  - Login KO si mot de passe incorrect.
  - Login KO si email inconnu.
  - Accès `/api/me` :
    - refus sans authentification
    - acceptation après login.

- **JavaDoc obligatoire**
  - Sur :
    - `User` (entité).
    - `AuthService` (service principal).
    - `AuthController` (endpoints).
    - Exceptions.
  - Le JavaDoc doit contenir une phrase explicite du type :
    - **« Cette implémentation est volontairement dangereuse et ne doit jamais être utilisée en production. »**

- **Logging dans un fichier texte**
  - Logger :
    - Inscription réussie.
    - Inscription échouée.
    - Connexion réussie.
    - Connexion échouée.
  - **Interdiction** : ne **jamais** logger le mot de passe.

---

### 4. README principal du projet (pour ton repo)

Pour le **README principal à la racine du projet**, tu devras expliquer :

- **Configuration / lancement**
  - Comment lancer **MySQL** et configurer `application.properties` (URL, user, mot de passe, création de la base).
  - Comment lancer l’**API Spring Boot** (`mvn spring-boot:run` ou via l’IDE).
  - Comment lancer le **client Java** (Swing/JavaFX).

- **Compte de test**
  - `toto@example.com`
  - `pwd1234`

- **Section « Analyse de sécurité TP1 »**
  - Au moins **5 risques majeurs** expliqués simplement, par exemple :
    - Mot de passe **stocké en clair** en base.
    - Mot de passe **faible** (min 4 caractères).
    - Session/token **trop simple** ou mal protégé.
    - Manque de protections contre les attaques **brute force**.
    - Manque de chiffrement des communications si pas de HTTPS.

---

### 5. Git : étapes et tags obligatoires pour le TP1

L’objectif est que l’historique Git montre l’évolution, **pas un seul gros commit**.

- **Étape 0 – Initialisation**
  - Créer le repository GitHub.
  - Premier commit : projet vide Spring Boot + structure de packages + squelette `README`.
  - Tag : **`v1.0-init`**

- **Étape 1 – Modèle de données**
  - Ajouter entité `User` + migration SQL ou script `schema.sql`.
  - Ajouter `UserRepository`.
  - Commit propre.
  - Tag : **`v1.1-model`**

- **Étape 2 – Inscription**
  - Implémenter `POST /api/auth/register`.
  - Validation minimale côté serveur.
  - Gestion exceptions + `@ControllerAdvice`.
  - Tests associés.
  - Tag : **`v1.2-register`**

- **Étape 3 – Connexion**
  - Implémenter `POST /api/auth/login`.
  - Gestion erreurs + logging.
  - Tests associés.
  - Tag : **`v1.3-login`**

- **Étape 4 – Route protégée**
  - Implémenter `GET /api/me`.
  - Mécanisme simple d’authentification (session ou token basique).
  - Tests associés.
  - Tag : **`v1.4-protected`**

- **Étape 5 – Finalisation qualité**
  - Compléter JavaDoc.
  - Nettoyer le code, supprimer les warnings inutiles.
  - Vérifier que **tous les tests passent**.
  - Tag final : **`v1-tp1`**

