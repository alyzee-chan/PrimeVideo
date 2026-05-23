package com.primevideo.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class MfaChallengeRequest {
    private String mfaToken;
    private Integer code; // Code TOTP à 6 chiffres
    private String deviceId;
}
