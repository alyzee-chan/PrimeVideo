package com.primevideo.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSubscriptionDTO {
    private Long id;
    private SubscriptionPlanDTO plan;
    private String status;
    private LocalDateTime startedAt;
    private LocalDateTime currentPeriodEnd;
    private LocalDateTime cancelledAt;
    private Boolean isAutoRenew;
}
