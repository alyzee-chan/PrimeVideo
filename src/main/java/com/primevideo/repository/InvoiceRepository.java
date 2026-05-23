package com.primevideo.repository;

import com.primevideo.entity.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    
    Optional<Invoice> findByInvoiceNumber(String invoiceNumber);
    
    List<Invoice> findByPaymentUserIdOrderByIssuedAtDesc(Long userId);
    
    Optional<Invoice> findByPaymentId(Long paymentId);
}
