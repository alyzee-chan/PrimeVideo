package com.primevideo.service.payment;

import com.primevideo.dto.response.InvoiceDTO;
import com.primevideo.entity.Invoice;
import com.primevideo.entity.Payment;
import com.primevideo.repository.InvoiceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BillingService {

    private final InvoiceRepository invoiceRepository;

    @Transactional
    public Invoice generateInvoice(Payment payment) {
        log.info("Génération de la facture pour le paiement: {}", payment.getTransactionId());

        // Calcul de la taxe (ex: 20% TVA simulée)
        BigDecimal taxRate = new BigDecimal("0.20");
        BigDecimal taxAmount = payment.getAmount().multiply(taxRate).setScale(2, RoundingMode.HALF_UP);

        String year = String.valueOf(LocalDateTime.now().getYear());
        String invoiceNumber = "PV-FR-" + year + "-" + String.format("%06d", payment.getId());

        Invoice invoice = Invoice.builder()
                .invoiceNumber(invoiceNumber)
                .description(payment.getDescription() != null ? payment.getDescription() : "Abonnement Prime Video")
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .taxAmount(taxAmount)
                .issuedAt(LocalDateTime.now())
                .status(Invoice.InvoiceStatus.PAID)
                .payment(payment)
                .pdfUrl("/api/payments/invoices/download/" + invoiceNumber) // URL simulée
                .build();

        return invoiceRepository.save(invoice);
    }

    public List<InvoiceDTO> getUserInvoices(Long userId) {
        return invoiceRepository.findByPaymentUserIdOrderByIssuedAtDesc(userId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public InvoiceDTO toDTO(Invoice invoice) {
        return InvoiceDTO.builder()
                .id(invoice.getId())
                .invoiceNumber(invoice.getInvoiceNumber())
                .description(invoice.getDescription())
                .amount(invoice.getAmount())
                .currency(invoice.getCurrency())
                .taxAmount(invoice.getTaxAmount())
                .pdfUrl(invoice.getPdfUrl())
                .issuedAt(invoice.getIssuedAt())
                .status(invoice.getStatus().name())
                .build();
    }
}
