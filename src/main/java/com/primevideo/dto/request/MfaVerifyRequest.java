package com.primevideo.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class MfaVerifyRequest {
    private Integer code;
    private List<String> backupCodes;
}
