package com.primevideo.config;

import com.primevideo.entity.Content;
import com.primevideo.entity.Profile;
import com.primevideo.entity.User;
import com.primevideo.repository.ContentRepository;
import com.primevideo.repository.ProfileRepository;
import com.primevideo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.io.File;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class DataInitializer {

    @Bean
    CommandLineRunner initDatabase(UserRepository userRepository, 
                                   ContentRepository contentRepository,
                                   ProfileRepository profileRepository,
                                   PasswordEncoder passwordEncoder) {
        return args -> {
            // Force la création ou la mise à jour de l'admin pour être sûr du mot de passe
            User admin = userRepository.findByEmail("admin@primevideo.com")
                    .orElse(User.builder()
                            .email("admin@primevideo.com")
                            .fullName("Administrateur")
                            .role(User.Role.ADMIN)
                            .isActive(true)
                            .emailVerified(true)
                            .build());
            
            admin.setPassword(passwordEncoder.encode("password"));
            User savedAdmin = userRepository.save(admin);
            log.info("👤 Compte Admin synchronisé (admin@primevideo.com / password)");

            // Création d'un profil par défaut pour l'admin s'il n'en a pas
            if (profileRepository.findAllByUser(savedAdmin).isEmpty()) {
                Profile adminProfile = Profile.builder()
                        .name("Admin")
                        .user(savedAdmin)
                        .ageRating(Profile.AgeRating.R_18)
                        .avatarUrl("https://api.dicebear.com/7.x/bottts/svg?seed=Admin")
                        .isKids(false)
                        .build();
                profileRepository.save(adminProfile);
                log.info("👤 Profil Admin par défaut créé");
            }

            if (!userRepository.existsByEmail("user@primevideo.com")) {
                User testUser = User.builder()
                        .fullName("Utilisateur Test")
                        .email("user@primevideo.com")
                        .password(passwordEncoder.encode("password"))
                        .role(User.Role.USER)
                        .isActive(true)
                        .build();
                userRepository.save(testUser);
                log.info("👤 Compte User test créé (user@primevideo.com / password)");
            }

            // Ajout de contenus diversifiés si le catalogue est encore restreint
            if (contentRepository.count() < 10) {
                // ── ANIME ───────────────────────────────────────────────────
                contentRepository.save(com.primevideo.entity.Content.builder()
                        .title("Demon Slayer")
                        .description("Le voyage de Tanjiro pour venger sa famille et guérir sa sœur transformée en démon.")
                        .type(Content.ContentType.ANIME)
                        .genre(Content.Genre.ACTION)
                        .year(2019)
                        .director("Haruo Sotozaki")
                        .posterUrl("https://fr.web.img4.acsta.net/pictures/19/08/30/17/30/2324976.jpg")
                        .ageRating(0).isPremium(true).isAvailable(true).build());

                contentRepository.save(Content.builder()
                        .title("Naruto Shippuden")
                        .description("Le jeune ninja Naruto Uzumaki cherche à devenir le leader de son village.")
                        .type(Content.ContentType.ANIME)
                        .genre(Content.Genre.ACTION)
                        .year(2007)
                        .director("Hayato Date")
                        .posterUrl("https://fr.web.img6.acsta.net/pictures/23/04/18/16/06/1897298.jpg")
                        .ageRating(0).isPremium(false).isAvailable(true).build());

                // ── K-DRAMA ─────────────────────────────────────────────────
                contentRepository.save(Content.builder()
                        .title("Crash Landing on You")
                        .description("Une héritière sud-coréenne atterrit accidentellement en Corée du Nord.")
                        .type(Content.ContentType.KDRAMA)
                        .genre(Content.Genre.ROMANCE)
                        .year(2019)
                        .director("Lee Jung-hyo")
                        .posterUrl("https://fr.web.img5.acsta.net/pictures/19/12/12/10/44/2226217.jpg")
                        .ageRating(0).isPremium(true).isAvailable(true).build());

                // ── AFRIQUE ──────────────────────────────────────────────────
                // ── AFRIQUE (AUTHENTIQUE & THÉMATIQUE) ──────────────────────
                contentRepository.save(Content.builder()
                        .title("Iwájú")
                        .description("Une aventure futuriste à Lagos où une jeune fille découvre les secrets de son monde technologique.")
                        .type(Content.ContentType.AFRICAIN)
                        .genre(Content.Genre.ANIMATION)
                        .year(2024)
                        .director("Olufikayo Ziki Adeola")
                        .posterUrl("https://lumiere-a.akamaihd.net/v1/images/p_iwaju_v1_684_a9d3a778.jpeg")
                        .ageRating(0).isPremium(true).isAvailable(true).build());

                contentRepository.save(Content.builder()
                        .title("Juju Stories")
                        .description("Trois contes sur le folklore mystique et la sorcellerie urbaine au Nigeria moderne.")
                        .type(Content.ContentType.AFRICAIN)
                        .genre(Content.Genre.THRILLER)
                        .year(2021)
                        .director("Abba Makama, C.J. Obasi")
                        .posterUrl("https://fr.web.img4.acsta.net/pictures/21/09/27/11/49/567292.jpg")
                        .ageRating(16).isPremium(true).isAvailable(true).build());

                contentRepository.save(Content.builder()
                        .title("The Wedding Party")
                        .description("Le mariage le plus luxueux de Lagos vire au chaos total. Le sommet du cinéma Nollywood.")
                        .type(Content.ContentType.AFRICAIN)
                        .genre(Content.Genre.COMEDIE)
                        .year(2016)
                        .director("Kemi Adetiba")
                        .posterUrl("https://resizing.flixster.com/eBf7p_I8_D9J9S_X0uY_Y_X_X0Y=/206x305/v2/https://flxt.tmsimg.com/assets/p13560645_p_v8_aa.jpg")
                        .ageRating(0).isPremium(false).isAvailable(true).build());

                contentRepository.save(Content.builder()
                        .title("Anikulapo")
                        .description("L'histoire mystique d'un homme qui détient le pouvoir sur la vie et la mort dans le royaume Yorouba.")
                        .type(Content.ContentType.AFRICAIN)
                        .genre(Content.Genre.DRAME)
                        .year(2022)
                        .director("Kunle Afolayan")
                        .posterUrl("https://fr.web.img3.acsta.net/pictures/22/09/23/11/04/0073286.jpg")
                        .ageRating(12).isPremium(false).isAvailable(true).build());

                log.info("🌍 Section Afrique enrichie avec Nollywood, Iwájú et Sorcellerie.");

                // ── FILMS / SERIES CULTES ────────────────────────────────────
                contentRepository.save(Content.builder()
                        .title("Interstellar")
                        .description("Un voyage interstellaire pour sauver l'humanité.")
                        .type(Content.ContentType.FILM)
                        .genre(Content.Genre.SCIENCE_FICTION)
                        .year(2014)
                        .director("Christopher Nolan")
                        .posterUrl("https://fr.web.img3.acsta.net/pictures/14/09/24/12/08/158828.jpg")
                        .ageRating(0).isPremium(false).isAvailable(true).build());

                contentRepository.save(Content.builder()
                        .title("Stranger Things")
                        .description("Des phénomènes surnaturels frappent la ville d'Hawkins.")
                        .type(Content.ContentType.SERIE)
                        .genre(Content.Genre.SCIENCE_FICTION)
                        .year(2016)
                        .director("The Duffer Brothers")
                        .videoUrl("/media/stranger_things_s1e1.mp4")
                        .posterUrl("https://fr.web.img4.acsta.net/pictures/22/05/18/14/31/5186184.jpg")
                        .ageRating(0).isPremium(true).isAvailable(true).build());

                log.info("🌍 Catalogue international enrichi (Anime, KDrama, Afrique).");

                // ── DIRECT / LIVE ───────────────────────────────────────────
                contentRepository.save(Content.builder()
                        .title("UEFA Champions League - Finale")
                        .description("Vivez la grande finale en direct. Frissons garantis !")
                        .type(Content.ContentType.LIVE)
                        .genre(Content.Genre.SPORT)
                        .year(2024)
                        .videoUrl("https://www.learningcontainer.com/wp-content/uploads/2020/05/sample-mp4-file.mp4")
                        .posterUrl("https://fr.web.img6.acsta.net/pictures/23/05/22/10/44/2226217.jpg")
                        .ageRating(0).isPremium(true).isAvailable(true).build());

                // ── CONTENU RESTREINT (18+) ─────────────────────────────────
                contentRepository.save(Content.builder()
                        .title("The Boys")
                        .description("Une vision irrévérencieuse de ce qui se passe quand les super-héros abusent de leurs pouvoirs.")
                        .type(Content.ContentType.SERIE)
                        .genre(Content.Genre.ACTION)
                        .year(2019)
                        .ageRating(18) // RÉSERVÉ AUX ADULTES
                        .director("Eric Kripke")
                        .videoUrl("/media/the_boys_s1e1.mp4")
                        .posterUrl("https://fr.web.img5.acsta.net/pictures/19/06/12/10/44/2226217.jpg")
                        .isPremium(true).isAvailable(true).build());

                log.info("📡 Section LIVE et Contenus 18+ ajoutés.");
            }

            // 🔍 DEBUG : Vérifier l'existence physique des fichiers
            String rootDir = System.getProperty("user.dir");
            File mediaFolder = new File(rootDir, "BD-Prime");
            log.info("📂 [CHECK] Dossier BD-Prime : {} (existe={})", mediaFolder.getAbsolutePath(), mediaFolder.exists());
            if (mediaFolder.exists()) {
                String[] files = mediaFolder.list();
                if (files != null) {
                    log.info("📄 [CHECK] Nombre de fichiers trouvés : {}", files.length);
                    for (String f : files) {
                        if (f.endsWith(".mp4")) log.info("   - Vidéo trouvée : {}", f);
                    }
                }
            }

            // 🛠️ CORRECTIF FINAL : S'assurer que TOUS les contenus ont un ageRating = 0 (évite les disparitions)
            // et une URL de vidéo locale valide pour garantir la lecture (contourne les URLs externes cassées)
            contentRepository.findAll().forEach(content -> {
                boolean updated = false;
                if (content.getAgeRating() == null) {
                    content.setAgeRating(0);
                    updated = true;
                }
                
                // Forcer une URL locale si l'URL est vide ou pointe vers un domaine externe instable (ex: googleapis)
                String vUrl = content.getVideoUrl();
                if (vUrl == null || vUrl.isEmpty() || vUrl.contains("googleapis.com") || vUrl.contains("bunny")) {
                    String title = content.getTitle().toLowerCase();
                    if (title.contains("boys")) {
                        content.setVideoUrl("/media/the_boys_s1e1.mp4");
                    } else if (title.contains("stranger")) {
                        content.setVideoUrl("/media/stranger_things_s1e1.mp4");
                    } else if (title.contains("banker")) {
                        content.setVideoUrl("/media/the_banker.mp4");
                    } else if (title.contains("gen v")) {
                        content.setVideoUrl("/media/gen_v_s1e1.mp4");
                    } else {
                        // Fallback par défaut sur un fichier local de test existant
                        content.setVideoUrl("/media/the_banker.mp4");
                    }
                    updated = true;
                }

                if (updated) {
                    contentRepository.save(content);
                }
            });

            log.info("✅ Correction automatique des URLs de vidéos et des restrictions effectuée.");
            log.info("✅ PrimeVideo démarré sur le réseau (port 8082)");
        };
    }
}
