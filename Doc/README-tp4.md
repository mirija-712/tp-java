### TP4 – Authentification et Master Key + CI/CD

**But du TP4**

Ne plus modifier le protocole HMAC/nonce/timestamp, mais **industrialiser** :

1. La **protection des mots de passe au repos** via une **Master Key**.
2. L’**automatisation complète** de la qualité via **GitHub Actions** (CI/CD).
3. Le **blocage des merges** si la qualité est insuffisante.

Durée estimée : **10 h** – Tag Git final attendu : **`v4-tp4`**

---

### 1. Partie 1 – Chiffrement des mots de passe par Master Key

#### Contexte

- Dans le TP3, les mots de passe étaient stockés **en clair** dans MySQL → plus acceptable.
- On introduit une **Master Key** fournie par l’administrateur, utilisée pour chiffrer les mots de passe avant stockage.

#### Exigences obligatoires

- Le mot de passe ne doit **jamais** être stocké en clair.
- Le mot de passe doit être **chiffré avant insertion** en base.
- La **Master Key ne doit jamais être dans le code**.
- La Master Key doit être injectée par **variable d’environnement** :
  - Nom imposé : **`APP_MASTER_KEY`**
- Si la clé est **absente**, l’application doit **refuser de démarrer**.

#### Implémentation recommandée

- Algorithme : **AES en mode GCM**.
  - AES GCM fournit **chiffrement + intégrité**.
- Format de stockage conseillé :
  - `v1:Base64(iv):Base64(ciphertext)`

- **Structure MySQL mise à jour**
  - Table `users` :
    - `id`
    - `email`
    - `password_encrypted`
    - `created_at`
  - Le champ `password_clear` est supprimé **définitivement**.

- **Processus à l’inscription**
  - `password_plain`
  - → chiffrement AES via `APP_MASTER_KEY`
  - → stockage dans `password_encrypted`

- **Processus au login**
  - Lecture de `password_encrypted`
  - → déchiffrement via `APP_MASTER_KEY`
  - → récupération de `password_plain`
  - → recalcul HMAC (comme TP3)
  - → vérification.

- **Interdictions strictes**
  - Pas de clé codée en dur.
  - Pas d’IV fixe.
  - Pas de mode ECB.
  - Pas de log de mot de passe.

#### Tests obligatoires liés à la Master Key

- Test de démarrage **KO** si `APP_MASTER_KEY` absente.
- Test **encryption/decryption OK**.
- Test que `password_encrypted != password_plain`.
- Test de déchiffrement **KO** si le ciphertext est modifié.

---

### 2. Partie 2 – GitHub Actions CI/CD obligatoire

**Objectif :** automatiser totalement la qualité du projet.  
À partir de TP4, aucun code ne doit être validé sans passer par une pipeline automatique.

#### Exigences CI/CD

- Chaque `push` et chaque `pull_request` doit déclencher :
  - Build automatique.
  - Exécution des tests JUnit.
  - Analyse **SonarCloud**.
  - Échec si un de ces éléments échoue.

- Le repo doit contenir un workflow GitHub Actions :
  - Dossier : `.github/workflows/`
  - Fichier recommandé : `ci.yml`

- **Déclencheurs obligatoires**
  - `push` sur la branche `main`.
  - `pull_request` vers la branche `main`.

#### Pipeline minimale attendue

La pipeline doit effectuer automatiquement :

- Récupération du code (**checkout**).
- Installation de **JDK 17**.
- Build du projet (Maven ou Gradle).
- Exécution des tests.
- Analyse SonarCloud.
- Échec automatique si :
  - Un test échoue.
  - Le **Quality Gate SonarCloud** est rouge.

- Configurer le repo GitHub pour **bloquer les merges** si la CI échoue.

---

### 3. SonarCloud et gestion des tests en CI

- Le projet doit être **connecté à SonarCloud**.
- Dans GitHub → Settings → Secrets and variables → Actions :
  - Ajouter :
    - `SONAR_TOKEN`
    - `SONAR_PROJECT_KEY`
    - `SONAR_ORGANIZATION`
- La pipeline doit utiliser ces secrets.
- Le Quality Gate doit être **vert**.

- **Gestion des tests en CI**
  - Les tests doivent fonctionner **sans base MySQL externe**.
  - Exigence obligatoire :
    - Utiliser **H2 en mémoire** pour les tests.
  - Cela garantit que la pipeline fonctionne sans dépendance externe.

- **Gestion de la Master Key en CI**
  - La Master Key **réelle** ne doit jamais être exposée.
  - Pour les tests, utiliser une clé fictive injectée via :
    - Variable d’environnement.
    - Profil `application-test`.
  - Exemple :
    - `APP_MASTER_KEY=test_master_key_for_ci_only`

#### Critères de succès

- La pipeline s’exécute automatiquement.
- Les tests sont réellement exécutés.
- Le build échoue si un test échoue.
- Le Quality Gate SonarCloud est vert.
- Les secrets ne sont pas commités.

---

### 4. Exemple de workflow `ci.yml` (résumé fonctionnel)

Le fichier `.github/workflows/ci.yml` doit :

- Se déclencher sur `push` et `pull_request` vers `main`.
- Installer Java 17.
- Mettre en cache Maven.
- Injecter une Master Key fictive pour les tests.
- Exécuter tous les tests.
- Lancer SonarCloud.
- Échouer si :
  - Les tests échouent.
  - Le Quality Gate SonarCloud échoue.

---

### 5. README (compléments TP4)

Dans le README principal du projet, ajouter :

- Une section expliquant :
  - Comment la **Master Key** est gérée (variable d’environnement, profils).
  - Pourquoi elle ne doit jamais être codée en dur.
  - Comment la **CI/CD** est configurée (GitHub Actions, SonarCloud, blocage des merges).
  - Comment les tests utilisent H2 pour être autonomes.

- Un rappel que :
  - Le projet vise un niveau de qualité **industrielle**.
  - Aucune modification ne doit être mergée sans CI **verte**.

