package com.primevideo.service;

import com.primevideo.entity.User;
import com.primevideo.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service utilitaire pour nettoyer les données corrompues ou mal formatées en base.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DataCleanupService {

    private final UserRepository userRepository;

    /**
     * Nettoie les emails des utilisateurs en supprimant les espaces invisibles.
     * S'exécute une seule fois au démarrage de l'application.
     */
    @PostConstruct
    @Transactional
    public void cleanupUserEmails() {
        log.info("Démarrage du nettoyage des emails utilisateurs...");
        List<User> users = userRepository.findAll();
        int count = 0;

        for (User user : users) {
            String originalEmail = user.getEmail();
            if (originalEmail != null) {
                String trimmedEmail = originalEmail.trim();
                if (!originalEmail.equals(trimmedEmail)) {
                    log.warn("Nettoyage de l'email pour l'utilisateur ID {}: '{}' -> '{}'", 
                            user.getId(), originalEmail, trimmedEmail);
                    user.setEmail(trimmedEmail);
                    userRepository.save(user);
                    count++;
                }
            }
        }

        if (count > 0) {
            log.info("Nettoyage terminé : {} emails ont été corrigés.", count);
        } else {
            log.info("Aucun email mal formaté trouvé. La base de données est saine.");
        }
    }
}
