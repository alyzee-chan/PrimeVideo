package com.primevideo.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceDTO {
    private Long id;
    private String invoiceNumber;
    private String description;
    private BigDecimal amount;
    private String currency;
    private BigDecimal taxAmount;
    private String pdfUrl;
    private LocalDateTime issuedAt;
    private String status;
}
