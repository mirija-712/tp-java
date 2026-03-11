### TP2 – Authentification Fragile

**But du TP2**

Améliorer l’authentification du TP1 avec une **politique de mot de passe stricte**, un **stockage correct côté serveur (hash adaptatif)**, un **verrouillage anti brute force**, et introduire une première démarche **qualité industrielle** avec **SonarCloud**.  
Malgré tout, l’authentification reste fragile car le secret circule encore lors du login.

Durée estimée : **10 h** – Tag Git final attendu : **`v2-tp2`**

---

### 1. Fonctionnalités côté client

- **Inscription**
  - Double saisie de mot de passe : `password` et `passwordConfirm`.
  - Indicateur visuel de **force du mot de passe** :
    - Rouge : non conforme.
    - Orange : conforme mais faible.
    - Vert : conforme et bon niveau.

- **Politique de mot de passe côté client (à refléter côté serveur)**
  - Minimum **12 caractères**.
  - Au moins **1 majuscule**.
  - Au moins **1 minuscule**.
  - Au moins **1 chiffre**.
  - Au moins **1 caractère spécial**.

---

### 2. Fonctionnalités côté serveur

- **Stockage sécurisé du mot de passe**
  - Utiliser un **hash adaptatif** (par ex. `BCryptPasswordEncoder`).
  - Remplacer `password_clear` par `password_hash` en base.

- **Login**
  - Vérifier le mot de passe via le hash (BCrypt).
  - Conserver la route protégée `GET /api/me` accessible uniquement si authentifié.

- **Anti brute force**
  - Compter les échecs successifs de login.
  - **Après 5 échecs**, bloquer l’utilisateur pendant environ **2 minutes**.
  - Retourner un code HTTP de type **423 (Locked)** ou **429 (Too Many Requests)**, à **justifier** dans le README.

---

### 3. Qualité logicielle et SonarCloud (obligatoires)

- **JUnit**
  - Minimum **10 tests** (ceux du TP1 + nouveaux).
  - Tests recommandés supplémentaires :
    - Non-divulgation des erreurs (même message pour email inconnu ou mauvais mot de passe).
    - Lockout qui expire correctement (l’utilisateur se débloque après la durée prévue).

- **JavaDoc**
  - Obligatoire sur toutes les classes :
    - `security`, `validator`, `services`, `exceptions`.
  - Ajouter dans le JavaDoc d’`AuthService` une phrase du type :
    - **« TP2 améliore le stockage mais ne protège pas encore contre le rejeu. »**

- **Exceptions et ControllerAdvice**
  - Exceptions personnalisées et `@ControllerAdvice` **obligatoires**, comme au TP1, enrichis si besoin.

- **SonarCloud**
  - Le projet doit être **analysé sur SonarCloud**.
  - Corriger au minimum :
    - Les **bugs majeurs** détectés.
    - Les **vulnérabilités majeures**.
    - Quelques **code smells significatifs** (exceptions, validation, duplication, complexité inutile).
  - **Quality Gate** :
    - Doit être **en succès**, ou à défaut :
      - Justification écrite dans le README :
        - Quelles règles bloquent.
        - Pourquoi.
        - Ce qui sera corrigé au TP4.
  - **Couverture de tests**
    - Objectif réaliste TP2 : **60 % minimum** de couverture.

---

### 4. README (compléments TP2)

Dans le README principal du projet, ajouter une section **Qualité (TP2)** qui mentionne :

- La **faiblesse persistante** de l’authentification (rejeu possible si une requête est capturée).
- La façon dont **SonarCloud** est configuré.
- Le résultat du **Quality Gate**.
- La **couverture approximative** et comment elle est mesurée.
- Un **plan d’améliorations futurs**.

---

### 5. Git : étapes et tags obligatoires pour le TP2

- **Étape 0 – Démarrage TP2**
  - Mettre à jour le README avec les objectifs du TP2.
  - Tag : **`v2.0-start`**

- **Étape 1 – Migration base**
  - Migrer de `password_clear` vers `password_hash`.
  - Tag : **`v2.1-db-migration`**

- **Étape 2 – Politique mot de passe**
  - Implémenter `PasswordPolicyValidator` + tests associés.
  - Tag : **`v2.2-password-policy`**

- **Étape 3 – Hashing**
  - Mettre en place `BCryptPasswordEncoder`.
  - Inscription stocke le hash, login le vérifie.
  - Tag : **`v2.3-hashing`**

- **Étape 4 – Anti brute force**
  - Implémenter `failed_attempts`, `lock_until` + tests.
  - Tag : **`v2.4-lockout`**

- **Étape 5 – UI force mot de passe**
  - Mettre en place l’indicateur visuel de force du mot de passe côté client (rouge/orange/vert).
  - Tag : **`v2.5-ui-strength`**

- **Étape 6 – SonarCloud**
  - Configurer SonarCloud, lancer l’analyse, corriger les points majeurs.
  - Ajouter une section Qualité dans le README avec capture ou résumé.
  - Tag : **`v2.6-sonarcloud`**

- **Étape 7 – Finalisation**
  - JavaDoc final, nettoyage, tous les tests verts.
  - Tag final : **`v2-tp2`**

