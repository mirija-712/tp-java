package com.example.authentification;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Point d'entrée de l'application Spring Boot.
 * <p>
 * @SpringBootApplication combine : @Configuration, @EnableAutoConfiguration, @ComponentScan.
 * Démarre le serveur embarqué (Tomcat) et charge tous les beans du package et sous-packages.
 * </p>
 */
@SpringBootApplication
public class AuthentificationApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthentificationApplication.class, args);
    }
}
