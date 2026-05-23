package com.primevideo.service;

import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;
import com.warrenstrange.googleauth.GoogleAuthenticatorQRGenerator;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class MfaService {

    private final GoogleAuthenticator gAuth = new GoogleAuthenticator();

    /**
     * Génère un nouveau secret TOTP pour un utilisateur.
     */
    public String generateSecret() {
        final GoogleAuthenticatorKey key = gAuth.createCredentials();
        return key.getKey();
    }

    /**
     * Génère l'URL du QR Code pour scanner dans une application (Google Authenticator, Authy).
     */
    public String getQrCodeUrl(String secret, String email) {
        return GoogleAuthenticatorQRGenerator.getOtpAuthURL("Prime Video", email, 
                new GoogleAuthenticatorKey.Builder(secret).build());
    }

    /**
     * Vérifie si le code fourni est valide pour le secret donné.
     */
    public boolean verifyCode(String secret, int code) {
        return gAuth.authorize(secret, code);
    }

    /**
     * Génère une liste de codes de secours à usage unique.
     */
    public List<String> generateBackupCodes() {
        List<String> codes = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            codes.add(UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        }
        return codes;
    }
}
