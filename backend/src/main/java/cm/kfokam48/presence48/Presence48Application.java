package cm.kfokam48.presence48;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Presence48 — presences, depot d'exercices et relecture entre pairs.
 *
 * <p>Epreuve finale KFOKAM48. Le contrat d'API que cette application respecte
 * se trouve dans {@code api/contrat.yaml}, a la racine du depot.
 */
@SpringBootApplication
public class Presence48Application {

    public static void main(String[] args) {
        SpringApplication.run(Presence48Application.class, args);
    }
}
