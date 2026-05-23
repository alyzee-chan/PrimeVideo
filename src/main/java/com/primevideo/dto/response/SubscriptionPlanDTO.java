package com.primevideo.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionPlanDTO {
    private String id;
    private String name;
    private String type; // Enum in YAML: PRIME_MONTHLY, etc.
    private BigDecimal price;
    private String currency;
    private String billingPeriod; // MONTHLY, ANNUAL
    private Integer trialDays;
    private List<String> features;
    private Integer maxSimultaneousStreams;
    private Integer maxDownloads;
}
