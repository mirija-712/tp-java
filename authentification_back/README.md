# Authentification Backend – TP1

Serveur d'authentification volontairement dangereux (non sécurisé) en Spring Boot + MySQL pour les TP2–TP4.

---

## Sommaire

1. [Prérequis](#1-prérequis)
2. [Structure du projet](#2-structure-du-projet)
3. [Configuration](#3-configuration)
4. [Modèle de données](#4-modèle-de-données)
5. [Exceptions et gestion des erreurs](#5-exceptions-et-gestion-des-erreurs)
6. [DTOs](#6-dtos)
7. [Service et contrôleurs](#7-service-et-contrôleurs)
8. [Logging](#8-logging)
9. [Tests](#9-tests)
10. [Lancement](#10-lancement)
11. [API à tester (Postman)](#11-api-à-tester-postman)
12. [Compte de test](#12-compte-de-test)
13. [Analyse de sécurité TP1](#13-analyse-de-sécurité-tp1)

---

## 1. Prérequis

- Java 17
- Maven
- MySQL (port 3307)
- Base de données : `auth_tp`

---

## 2. Structure du projet

Créer la structure de packages suivante :

```
src/main/java/com/example/authentification/
├── AuthentificationApplication.java
├── controller/
│   ├── AuthController.java
│   └── MeController.java
├── dto/
│   ├── AuthResponse.java
│   ├── LoginRequest.java
│   ├── MeResponse.java
│   └── RegisterRequest.java
├── entity/
│   └── User.java
├── exception/
│   ├── AuthenticationFailedException.java
│   ├── GlobalExceptionHandler.java
│   ├── InvalidInputException.java
│   └── ResourceConflictException.java
├── repository/
│   └── UserRepository.java
└── service/
    └── AuthService.java
```

---

## 3. Configuration

### 3.1 `pom.xml`

**Objectif :** Déclarer les dépendances Maven du projet.

**Explication :**
- `spring-boot-starter-data-jpa` : accès base de données avec JPA/Hibernate
- `spring-boot-starter-webmvc` : API REST (contrôleurs, JSON)
- `mysql-connector-j` : pilote MySQL (scope runtime)
- `h2` : base en mémoire pour les tests
- `spring-boot-starter-test` et `spring-boot-starter-webmvc-test` : JUnit, MockMvc

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>4.0.3</version>
        <relativePath/>
    </parent>
    <groupId>com.example</groupId>
    <artifactId>authentification</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <name>authentification</name>
    <properties>
        <java.version>17</java.version>
    </properties>
    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-webmvc</artifactId>
        </dependency>
        <dependency>
            <groupId>com.mysql</groupId>
            <artifactId>mysql-connector-j</artifactId>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>com.h2database</groupId>
            <artifactId>h2</artifactId>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-webmvc-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>
</project>
```

### 3.2 `src/main/resources/application.properties`

**Objectif :** Configurer l'application (port, BDD, JPA, scripts SQL).

**Explication :**
- `server.port=8080` : l'API écoute sur le port 8080
- `server.servlet.session.cookie.same-site=lax` : cookies de session pour l'auth
- `spring.datasource.*` : connexion MySQL (URL, user, mot de passe)
- `spring.jpa.hibernate.ddl-auto=none` : on utilise schema.sql, pas Hibernate pour créer les tables
- `spring.sql.init.*` : exécution de schema.sql puis data.sql au démarrage

```properties
spring.application.name=authentification
server.port=8080

server.servlet.session.cookie.same-site=lax

# MySQL - Port 3307, base auth_tp
spring.datasource.url=jdbc:mysql://localhost:3307/auth_tp?createDatabaseIfNotExist=true
spring.datasource.username=root
spring.datasource.password=VOTRE_MOT_DE_PASSE
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# JPA / Hibernate
spring.jpa.hibernate.ddl-auto=none
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect

# Initialisation SQL
spring.sql.init.mode=always
spring.sql.init.schema-locations=classpath:schema.sql
spring.sql.init.data-locations=classpath:data.sql
```

### 3.3 `src/main/resources/schema.sql`

**Objectif :** Créer la table `users` au démarrage si elle n'existe pas.

**Explication :** Chaque colonne correspond à un champ de l'entité `User`. `CREATE TABLE IF NOT EXISTS` évite les erreurs si la table existe déjà.

```sql
-- Table users minimale pour le TP1
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_clear VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### 3.4 `src/main/resources/data.sql`

**Objectif :** Insérer le compte de test obligatoire (toto@example.com / pwd1234).

**Explication :** `INSERT IGNORE` ne fait rien si l'email existe déjà (évite les erreurs en redémarrage).

```sql
-- Compte de test obligatoire : toto@example.com / pwd1234
INSERT IGNORE INTO users (email, password_clear) VALUES ('toto@example.com', 'pwd1234');
```

---

## 4. Modèle de données

### 4.1 `entity/User.java`

**Objectif :** Mapper la table `users` en objet Java (ORM JPA).

**Explication :**
- `@Entity` : classe persistée en base
- `@Table(name = "users")` : nom de la table
- `@Id` + `@GeneratedValue` : clé primaire auto-incrémentée
- `@Column` : mapping champ Java ↔ colonne SQL
- `@PrePersist` : exécuté avant chaque INSERT (remplit `createdAt`)

```java
package com.example.authentification.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entité représentant un utilisateur dans le système d'authentification.
 * Cette implémentation est volontairement dangereuse et ne doit jamais
 * être utilisée en production. Les mots de passe sont stockés en clair.
 */
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "password_clear", nullable = false)
    private String passwordClear;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public User() {}

    public User(String email, String passwordClear) {
        this.email = email;
        this.passwordClear = passwordClear;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPasswordClear() { return passwordClear; }
    public void setPasswordClear(String passwordClear) { this.passwordClear = passwordClear; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
```

### 4.2 `repository/UserRepository.java`

**Objectif :** Fournir les opérations CRUD et requêtes personnalisées sur `users`.

**Explication :** Spring Data JPA génère automatiquement l'implémentation. `findByEmail` et `existsByEmail` sont des requêtes dérivées du nom de la méthode (convention de nommage).

```java
package com.example.authentification.repository;

import com.example.authentification.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
}
```

---

## 5. Exceptions et gestion des erreurs

**Objectif global :** Intercepter les erreurs et renvoyer des réponses JSON cohérentes (400, 401, 409) au lieu de laisser Spring renvoyer des pages d'erreur HTML.

### 5.1 `exception/InvalidInputException.java`

**Objectif :** Signaler des données invalides (email vide, format incorrect, mot de passe trop court). Convertie en HTTP 400 par le `GlobalExceptionHandler`.

```java
package com.example.authentification.exception;

/**
 * Exception levée lorsque les données d'entrée sont invalides.
 * Cette implémentation est volontairement dangereuse et ne doit jamais
 * être utilisée en production.
 */
public class InvalidInputException extends RuntimeException {
    public InvalidInputException(String message) { super(message); }
    public InvalidInputException(String message, Throwable cause) { super(message, cause); }
}
```

### 5.2 `exception/AuthenticationFailedException.java`

**Objectif :** Signaler un échec de login (email inconnu ou mot de passe incorrect). Convertie en HTTP 401. Message générique pour ne pas révéler si l'email existe.

```java
package com.example.authentification.exception;

/**
 * Exception levée lorsque l'authentification échoue.
 * Cette implémentation est volontairement dangereuse et ne doit jamais
 * être utilisée en production.
 */
public class AuthenticationFailedException extends RuntimeException {
    public AuthenticationFailedException(String message) { super(message); }
    public AuthenticationFailedException(String message, Throwable cause) { super(message, cause); }
}
```

### 5.3 `exception/ResourceConflictException.java`

**Objectif :** Signaler un conflit (ex. email déjà utilisé à l'inscription). Convertie en HTTP 409.

```java
package com.example.authentification.exception;

/**
 * Exception levée lorsqu'une ressource existe déjà (ex. email déjà utilisé).
 * Cette implémentation est volontairement dangereuse et ne doit jamais
 * être utilisée en production.
 */
public class ResourceConflictException extends RuntimeException {
    public ResourceConflictException(String message) { super(message); }
    public ResourceConflictException(String message, Throwable cause) { super(message, cause); }
}
```

### 5.4 `exception/GlobalExceptionHandler.java`

**Objectif :** Intercepter toutes les exceptions levées par les contrôleurs et renvoyer un JSON `{timestamp, status, error, message, path}`. `@ExceptionHandler` associe chaque type d'exception à un code HTTP.

```java
package com.example.authentification.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.time.Instant;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private Map<String, Object> buildErrorResponse(HttpServletRequest request, HttpStatus status,
                                                   String error, String message) {
        return Map.of(
            "timestamp", Instant.now().toString(),
            "status", status.value(),
            "error", error,
            "message", message,
            "path", request.getRequestURI()
        );
    }

    @ExceptionHandler(InvalidInputException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidInput(InvalidInputException ex,
                                                                  HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(buildErrorResponse(request, HttpStatus.BAD_REQUEST, "Bad Request", ex.getMessage()));
    }

    @ExceptionHandler(AuthenticationFailedException.class)
    public ResponseEntity<Map<String, Object>> handleAuthenticationFailed(AuthenticationFailedException ex,
                                                                          HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(buildErrorResponse(request, HttpStatus.UNAUTHORIZED, "Unauthorized", ex.getMessage()));
    }

    @ExceptionHandler(ResourceConflictException.class)
    public ResponseEntity<Map<String, Object>> handleResourceConflict(ResourceConflictException ex,
                                                                      HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
            .body(buildErrorResponse(request, HttpStatus.CONFLICT, "Conflict", ex.getMessage()));
    }
}
```

**Codes HTTP :** 400 (données invalides), 401 (login échoué), 409 (email déjà existant).

---

## 6. DTOs (Data Transfer Objects)

**Objectif :** Structurer les données JSON entrantes et sortantes. Les records Java 16+ sont des classes immuables avec constructeur et getters automatiques.

### 6.1 `dto/RegisterRequest.java`

**Objectif :** Reçu par POST /api/auth/register. Contient `email` et `password`.

```java
package com.example.authentification.dto;

public record RegisterRequest(String email, String password) {}
```

### 6.2 `dto/LoginRequest.java`

**Objectif :** Reçu par POST /api/auth/login. Même structure que RegisterRequest.

```java
package com.example.authentification.dto;

public record LoginRequest(String email, String password) {}
```

### 6.3 `dto/AuthResponse.java`

**Objectif :** Réponse uniforme pour register et login : `{success: true, message: "..."}`.

```java
package com.example.authentification.dto;

public record AuthResponse(boolean success, String message) {}
```

### 6.4 `dto/MeResponse.java`

**Objectif :** Réponse de GET /api/me. Retourne uniquement `id` et `email` (jamais le mot de passe).

```java
package com.example.authentification.dto;

public record MeResponse(Long id, String email) {}
```

---

## 7. Service et contrôleurs

### 7.1 `service/AuthService.java`

**Objectif :** Couche métier. Centralise la logique d'inscription, de connexion et de validation. Les contrôleurs délèguent à ce service.

**Explication :**
- `register()` : valide email + mot de passe, vérifie unicité, sauvegarde
- `login()` : cherche l'utilisateur, compare le mot de passe en clair
- `validateEmail()` : non vide + format xxx@yyy.zzz
- `validatePassword()` : minimum 4 caractères (règle TP1)
- Logging : inscription/connexion réussie ou échouée (sans jamais logger le mot de passe)

```java
package com.example.authentification.service;

import com.example.authentification.dto.LoginRequest;
import com.example.authentification.dto.RegisterRequest;
import com.example.authentification.entity.User;
import com.example.authentification.exception.*;
import com.example.authentification.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.util.regex.Pattern;

/**
 * Service principal d'authentification.
 * Cette implémentation est volontairement dangereuse et ne doit jamais
 * être utilisée en production.
 */
@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);
    private static final int MIN_PASSWORD_LENGTH = 4;
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^@]+@[^@]+\\.[^@]+$");

    private final UserRepository userRepository;

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User register(RegisterRequest request) {
        validateEmail(request.email());
        validatePassword(request.password());

        if (userRepository.existsByEmail(request.email())) {
            log.warn("Inscription échouée : email déjà existant pour {}", request.email());
            throw new ResourceConflictException("Cet email est déjà utilisé");
        }

        User user = new User(request.email(), request.password());
        user = userRepository.save(user);
        log.info("Inscription réussie pour {}", request.email());
        return user;
    }

    public User login(LoginRequest request) {
        validateEmail(request.email());
        validatePassword(request.password());

        User user = userRepository.findByEmail(request.email())
            .orElseThrow(() -> {
                log.warn("Connexion échouée : email inconnu {}", request.email());
                return new AuthenticationFailedException("Email ou mot de passe incorrect");
            });

        if (!user.getPasswordClear().equals(request.password())) {
            log.warn("Connexion échouée : mot de passe incorrect pour {}", request.email());
            throw new AuthenticationFailedException("Email ou mot de passe incorrect");
        }

        log.info("Connexion réussie pour {}", request.email());
        return user;
    }

    private void validateEmail(String email) {
        if (email == null || email.isBlank())
            throw new InvalidInputException("L'email est requis");
        if (!EMAIL_PATTERN.matcher(email.trim()).matches())
            throw new InvalidInputException("Format d'email invalide");
    }

    private void validatePassword(String password) {
        if (password == null || password.length() < MIN_PASSWORD_LENGTH)
            throw new InvalidInputException("Le mot de passe doit contenir au minimum " + MIN_PASSWORD_LENGTH + " caractères");
    }
}
```

### 7.2 `controller/AuthController.java`

**Objectif :** Exposer POST /api/auth/register et POST /api/auth/login. Reçoit le JSON, appelle le service, retourne la réponse. Après un login réussi, stocke l'utilisateur en session (cookie JSESSIONID) pour que GET /api/me fonctionne.

```java
package com.example.authentification.controller;

import com.example.authentification.dto.*;
import com.example.authentification.entity.User;
import com.example.authentification.service.AuthService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Contrôleur REST pour l'authentification.
 * Cette implémentation est volontairement dangereuse et ne doit jamais
 * être utilisée en production.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final String SESSION_USER = "authUser";
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest request) {
        authService.register(request);
        return ResponseEntity.ok(new AuthResponse(true, "Inscription réussie"));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request, HttpSession session) {
        User user = authService.login(request);
        session.setAttribute(SESSION_USER, user);
        return ResponseEntity.ok(new AuthResponse(true, "Connexion réussie"));
    }
}
```

### 7.3 `controller/MeController.java`

**Objectif :** Exposer GET /api/me (route protégée). Vérifie si un utilisateur est en session. Si oui, retourne `{id, email}`. Si non, retourne 401.

```java
package com.example.authentification.controller;

import com.example.authentification.dto.MeResponse;
import com.example.authentification.entity.User;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Contrôleur pour la route protégée /api/me.
 * Cette implémentation est volontairement dangereuse et ne doit jamais
 * être utilisée en production.
 */
@RestController
@RequestMapping("/api")
public class MeController {

    private static final String SESSION_USER = "authUser";

    @GetMapping("/me")
    public ResponseEntity<MeResponse> me(HttpSession session) {
        User user = (User) session.getAttribute(SESSION_USER);
        if (user == null)
            return ResponseEntity.status(401).build();
        return ResponseEntity.ok(new MeResponse(user.getId(), user.getEmail()));
    }
}
```

### 7.4 `AuthentificationApplication.java`

**Objectif :** Point d'entrée. `@SpringBootApplication` active l'auto-configuration et le scan des composants. Démarre le serveur Tomcat sur le port 8080.

```java
package com.example.authentification;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class AuthentificationApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthentificationApplication.class, args);
    }
}
```

---

## 8. Logging

**Objectif :** Écrire les logs en console ET dans `logs/auth-server.log`. Pattern : date | thread | level | logger | message. **Important :** ne jamais logger le mot de passe.

### 8.1 `src/main/resources/logback-spring.xml`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<configuration>
    <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n</pattern>
            <charset>UTF-8</charset>
        </encoder>
    </appender>
    <appender name="FILE" class="ch.qos.logback.core.FileAppender">
        <file>logs/auth-server.log</file>
        <encoder>
            <pattern>%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n</pattern>
            <charset>UTF-8</charset>
        </encoder>
    </appender>
    <root level="INFO">
        <appender-ref ref="CONSOLE"/>
        <appender-ref ref="FILE"/>
    </root>
</configuration>
```

**Important :** ne jamais logger le mot de passe.

---

## 9. Tests

**Objectif :** Tests d'intégration avec H2 en mémoire (pas de MySQL nécessaire). MockMvc simule les requêtes HTTP. Le profil `test` charge `application-test.properties`.

### 9.1 `src/test/resources/application-test.properties`

**Objectif :** Utiliser H2 au lieu de MySQL pour les tests. `create-drop` recrée le schéma à chaque test.

```properties
spring.datasource.url=jdbc:h2:mem:testdb
spring.datasource.driver-class-name=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=

spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
spring.sql.init.mode=never
```

### 9.2 `src/test/java/.../AuthControllerTest.java`

**Objectif :** Tester chaque endpoint avec cas valides et invalides. Pour /api/me après login, réutiliser la session via `loginResult.getRequest().getSession()`.

Créer un test d’intégration avec `@SpringBootTest`, `@AutoConfigureMockMvc` et `@ActiveProfiles("test")` pour couvrir au minimum :

- Validation email (vide, format incorrect)
- Validation mot de passe (&lt; 4 caractères)
- Inscription OK
- Inscription refusée si email déjà existant (409)
- Login OK
- Login KO si mot de passe incorrect (401)
- Login KO si email inconnu (401)
- Accès `/api/me` : refus sans authentification, OK après login

Utiliser `MockMvc` et réutiliser la session après le login pour tester `/api/me`.

---

## 10. Lancement

### MySQL

1. Démarrer MySQL sur le port **3307**.
2. Créer la base `auth_tp` (ou la laisser être créée via `createDatabaseIfNotExist=true`).
3. Adapter `application.properties` (user, mot de passe).

### API Spring Boot

```bash
cd authentification_back
.\mvnw.cmd spring-boot:run
```

Ou via l’IDE : exécuter `AuthentificationApplication.java`.

L’API est disponible sur **http://localhost:8080**.

---

## 11. API à tester (Postman)

| Méthode | URL | Description |
|---------|-----|-------------|
| `POST` | `http://localhost:8080/api/auth/register` | Inscription |
| `POST` | `http://localhost:8080/api/auth/login` | Connexion |
| `GET` | `http://localhost:8080/api/me` | Route protégée (après login) |

### Exemples

**Inscription :**
```json
POST /api/auth/register
Content-Type: application/json

{"email":"user@example.com","password":"pwd1234"}
```

**Login :**
```json
POST /api/auth/login
Content-Type: application/json

{"email":"toto@example.com","password":"pwd1234"}
```

**Route protégée `/api/me` :**  
Activer l’envoi des cookies dans Postman, puis appeler `GET /api/me` après un login réussi. Réponse attendue : `{"id":1,"email":"toto@example.com"}`.

---

## 12. Compte de test

- **Email :** `toto@example.com`
- **Mot de passe :** `pwd1234`

Ce compte est créé automatiquement au démarrage via `data.sql`.

---

## 13. Analyse de sécurité TP1

Cette version est volontairement non sécurisée. Principaux risques :

1. **Mots de passe en clair** – Stockés sans hash (BCrypt, Argon2, etc.).
2. **Règles de mot de passe trop faibles** – Minimum 4 caractères uniquement.
3. **Session HTTP basique** – Pas de gestion sécurisée des tokens (JWT, etc.).
4. **Absence de protection contre le brute force** – Pas de limite de tentatives ni de blocage.
5. **Pas de HTTPS** – Communications en clair, vulnérables aux attaques man-in-the-middle.

Ces points seront abordés et corrigés dans les TP2–TP4.
