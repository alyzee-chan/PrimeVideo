package com.primevideo.dto.response;

import com.primevideo.entity.Payment;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class PaymentResponse {
    private Long id;
    private BigDecimal amount;
    private String currency;
    private Payment.PaymentMethod paymentMethod;
    private Payment.PaymentStatus status;
    private String transactionId;
    private String cardLastFour;
    private String cardBrand;
    private String phoneNumber;
    private String paypalEmail;
    private String description;
    private String failureReason;
    private LocalDateTime paidAt;
    private LocalDateTime createdAt;
    // Message lisible pour l'utilisateur
    private String message;
}
