package com.primevideo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
/**
 * Point d'entrée de l'application Spring Boot — PrimeVideo Clone.
 *
 * <p>{@code @SpringBootApplication} est une annotation composite qui active :</p>
 * <ul>
 *   <li>{@code @Configuration} — cette classe peut déclarer des beans Spring</li>
 *   <li>{@code @EnableAutoConfiguration} — Spring Boot configure automatiquement
 *       les composants détectés (datasource, security, thymeleaf...)</li>
 *   <li>{@code @ComponentScan} — scanne le package {@code com.primevideo} pour trouver
 *       tous les {@code @Service}, {@code @Repository}, {@code @Controller}, etc.</li>
 * </ul>
 *
 * <p>Au démarrage, Spring Boot :</p>
 * <ol>
 *   <li>Se connecte à MySQL (WAMP) via {@code application.yml}</li>
 *   <li>Crée ou met à jour les tables via Hibernate ({@code ddl-auto: update})</li>
 *   <li>Exécute le {@link com.primevideo.config.DataInitializer} (crée le compte admin)</li>
 *   <li>Démarre le serveur Tomcat embarqué sur le port 8082</li>
 * </ol>
 */
public class PrimeVideoApplication {
    public static void main(String[] args) {
        SpringApplication.run(PrimeVideoApplication.class, args);
    }
}
