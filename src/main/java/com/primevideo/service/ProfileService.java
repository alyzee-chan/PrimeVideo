package com.primevideo.service;

import com.primevideo.entity.Profile;
import com.primevideo.entity.User;
import com.primevideo.exception.BusinessException;
import com.primevideo.repository.ProfileRepository;
import com.primevideo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProfileService {

    private final ProfileRepository profileRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<Profile> getProfiles(String userEmail) {
        String email = userEmail != null ? userEmail.trim() : null;
        log.info("Récupération des profils pour l'utilisateur : {}", email);
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new BusinessException("Session invalide : utilisateur non trouvé (" + email + ")"));
        return profileRepository.findAllByUser(user);
    }

    @Transactional
    public Profile createProfile(String userEmail, Profile profile) {
        String email = userEmail != null ? userEmail.trim() : null;
        log.info("Création d'un profil pour l'utilisateur : {}", email);
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new BusinessException("Session invalide : utilisateur non trouvé (" + email + ")"));
        
        if (profileRepository.countByUser(user) >= 6) {
            throw new BusinessException("Nombre maximum de profils atteint (6)");
        }

        profile.setUser(user);
        Profile saved = profileRepository.save(profile);
        log.info("Profil créé avec succès : ID={}, Nom={}", saved.getId(), saved.getName());
        return saved;
    }

    @Transactional
    public Profile updateProfile(Long profileId, Profile updatedProfile) {
        log.info("Mise à jour du profil ID : {}", profileId);
        Profile existing = profileRepository.findById(profileId)
                .orElseThrow(() -> new BusinessException("Profil non trouvé (ID: " + profileId + ")"));

        existing.setName(updatedProfile.getName());
        existing.setAvatarUrl(updatedProfile.getAvatarUrl());
        existing.setIsKids(updatedProfile.getIsKids());
        existing.setAgeRating(updatedProfile.getAgeRating());
        existing.setLanguage(updatedProfile.getLanguage());
        existing.setPin(updatedProfile.getPin());

        return profileRepository.save(existing);
    }

    @Transactional
    public void deleteProfile(Long profileId) {
        log.info("Suppression du profil ID : {}", profileId);
        profileRepository.deleteById(profileId);
    }

    @Transactional(readOnly = true)
    public Profile getById(Long id) {
        return profileRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Profil non trouvé (ID: " + id + ")"));
    }

    @Transactional(readOnly = true)
    public boolean verifyPin(Long profileId, String pin) {
        Profile profile = profileRepository.findById(profileId)
                .orElseThrow(() -> new BusinessException("Profil non trouvé (ID: " + profileId + ")"));
        
        return pin != null && pin.equals(profile.getPin());
    }

    /**
     * Transfère un profil vers un autre compte utilisateur.
     */
    @Transactional
    public void transferProfile(Long profileId, String targetEmail) {
        Profile profile = profileRepository.findById(profileId)
                .orElseThrow(() -> new BusinessException("Profil non trouvé"));
        
        User targetUser = userRepository.findByEmailIgnoreCase(targetEmail.trim())
                .orElseThrow(() -> new BusinessException("Destinataire introuvable (" + targetEmail + ")"));

        if (profileRepository.countByUser(targetUser) >= 6) {
            throw new BusinessException("Le compte destinataire a déjà atteint le nombre maximum de profils (6)");
        }

        profile.setUser(targetUser);
        profileRepository.save(profile);
        log.info("Profil '{}' transféré avec succès vers {}", profile.getName(), targetEmail);
    }
}
