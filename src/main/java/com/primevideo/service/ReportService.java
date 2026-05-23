package com.primevideo.service;

import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.pdf.BaseFont;
import com.primevideo.entity.Payment;
import com.primevideo.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final PaymentRepository paymentRepository;

    @Value("${app.signature.secret:primevideo-secret}")
    private String signatureSecret;

    public byte[] generateMonthlySalesReport() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfMonth = now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        LocalDateTime endOfMonth = now.withDayOfMonth(now.toLocalDate().lengthOfMonth()).withHour(23).withMinute(59).withSecond(59);

        List<Payment> payments = paymentRepository.findByPaidAtBetweenAndStatus(startOfMonth, endOfMonth, Payment.PaymentStatus.SUCCESS);

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, out);

            document.open();

            // Fonts
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 22, Color.DARK_GRAY);
            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, Color.WHITE);
            Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.BLACK);
            Font smallFont = FontFactory.getFont(FontFactory.HELVETICA, 8, Color.GRAY);

            // Title
            Paragraph title = new Paragraph("Rapport Mensuel des Ventes", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(20);
            document.add(title);

            // Period
            Paragraph period = new Paragraph("Période : " + startOfMonth.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + 
                                            " au " + now.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")), normalFont);
            period.setSpacingAfter(30);
            document.add(period);

            // Table
            PdfPTable table = new PdfPTable(5);
            table.setWidthPercentage(100);
            table.setSpacingBefore(10);
            table.setWidths(new float[]{3f, 2f, 2f, 2f, 2f});

            // Headers
            String[] headers = {"Utilisateur", "Méthode", "Montant", "Date", "Transaction ID"};
            for (String header : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(header, headerFont));
                cell.setBackgroundColor(new Color(0, 168, 225)); // Prime Video Blue
                cell.setPadding(8);
                table.addCell(cell);
            }

            BigDecimal totalAmount = BigDecimal.ZERO;

            for (Payment payment : payments) {
                table.addCell(new Phrase(payment.getUser().getFullName(), normalFont));
                table.addCell(new Phrase(payment.getPaymentMethod().toString(), normalFont));
                table.addCell(new Phrase(payment.getAmount() + " " + payment.getCurrency(), normalFont));
                table.addCell(new Phrase(payment.getPaidAt().format(DateTimeFormatter.ofPattern("dd/MM HH:mm")), normalFont));
                table.addCell(new Phrase(payment.getTransactionId(), smallFont));
                totalAmount = totalAmount.add(payment.getAmount());
            }

            document.add(table);

            // Summary
            Paragraph summary = new Paragraph("\nTotal des ventes : " + totalAmount + " " + (payments.isEmpty() ? "XAF" : payments.get(0).getCurrency()), 
                                              FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, Color.BLACK));
            summary.setAlignment(Element.ALIGN_RIGHT);
            summary.setSpacingBefore(20);
            document.add(summary);

            // --- Digital Signature Section ---
            document.add(new Paragraph("\n\n"));
            
            PdfPTable signTable = new PdfPTable(1);
            signTable.setWidthPercentage(100);
            
            String reportHash = UUID.randomUUID().toString().toUpperCase(); // Simulated hash
            String certificateId = "CERT-" + now.format(DateTimeFormatter.ofPattern("yyyyMM")) + "-" + reportHash.substring(0, 8);
            
            PdfPCell signCell = new PdfPCell();
            signCell.setBorder(com.lowagie.text.Rectangle.BOX);
            signCell.setBorderWidth(1f);
            signCell.setPadding(15);
            signCell.setBackgroundColor(new Color(245, 245, 245));
            
            signCell.addElement(new Phrase("CERTIFICAT D'AUTHENTICITÉ NUMÉRIQUE", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.BLACK)));
            signCell.addElement(new Phrase("\nCe document est généré officiellement par le système Prime Video.", smallFont));
            signCell.addElement(new Phrase("\nID Certificat : " + certificateId, smallFont));
            signCell.addElement(new Phrase("\nSignature numérique : " + java.util.Base64.getEncoder().encodeToString((certificateId + signatureSecret).getBytes()), smallFont));
            signCell.addElement(new Phrase("\nDate de certification : " + now.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")), smallFont));
            
            signTable.addCell(signCell);
            document.add(signTable);

            document.close();
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la génération du PDF", e);
        }
    }

    public byte[] generatePaymentReceipt(Payment payment) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4);
            PdfWriter writer = PdfWriter.getInstance(document, out);

            document.open();

            // Fonts
            Font brandFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 26, new Color(0, 168, 225));
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, Color.BLACK);
            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, Color.WHITE);
            Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 11, Color.BLACK);
            Font boldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, Color.BLACK);
            Font smallFont = FontFactory.getFont(FontFactory.HELVETICA, 9, Color.GRAY);

            // 1. Header (Logo + Company Info)
            PdfPTable headerTable = new PdfPTable(2);
            headerTable.setWidthPercentage(100);
            
            PdfPCell logoCell = new PdfPCell(new Phrase("prime video", brandFont));
            logoCell.setBorder(com.lowagie.text.Rectangle.NO_BORDER);
            headerTable.addCell(logoCell);
            
            PdfPCell infoCell = new PdfPCell();
            infoCell.setBorder(com.lowagie.text.Rectangle.NO_BORDER);
            infoCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            infoCell.addElement(new Paragraph("PRIME VIDEO SERVICES", boldFont));
            infoCell.addElement(new Paragraph("Amazon Digital UK Ltd.\n38 Avenue John F. Kennedy\nL-1855 Luxembourg", smallFont));
            headerTable.addCell(infoCell);
            
            document.add(headerTable);
            document.add(new Paragraph("\n"));

            // 2. Receipt Title & Date
            Paragraph receiptTitle = new Paragraph("REÇU DE PAIEMENT", titleFont);
            receiptTitle.setSpacingAfter(10);
            document.add(receiptTitle);
            
            String dateFormatted = payment.getPaidAt() != null 
                    ? payment.getPaidAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) 
                    : LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
            document.add(new Paragraph("Date d'émission : " + dateFormatted, normalFont));
            document.add(new Paragraph("Numéro de transaction : " + payment.getTransactionId(), normalFont));
            document.add(new Paragraph("\n"));

            // 3. Billing Info
            PdfPTable billingTable = new PdfPTable(1);
            billingTable.setWidthPercentage(100);
            PdfPCell billingCell = new PdfPCell();
            billingCell.setPadding(10);
            billingCell.setBackgroundColor(new Color(245, 245, 245));
            billingCell.addElement(new Phrase("FACTURE À :", boldFont));
            billingCell.addElement(new Phrase("\n" + payment.getUser().getFullName(), normalFont));
            billingCell.addElement(new Phrase("\n" + payment.getUser().getEmail(), normalFont));
            billingTable.addCell(billingCell);
            document.add(billingTable);
            document.add(new Paragraph("\n"));

            // 4. Items Table
            PdfPTable table = new PdfPTable(2);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{4f, 2f});

            // Headers
            String[] headers = {"Description", "Montant"};
            for (String header : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(header, headerFont));
                cell.setBackgroundColor(new Color(0, 168, 225));
                cell.setPadding(8);
                table.addCell(cell);
            }

            table.addCell(new Phrase("Abonnement Prime Video - " + payment.getPaymentMethod(), normalFont));
            table.addCell(new Phrase(payment.getAmount() + " " + payment.getCurrency(), boldFont));

            document.add(table);

            // 5. Total
            Paragraph total = new Paragraph("\nTOTAL PAYÉ : " + payment.getAmount() + " " + payment.getCurrency(), 
                                           FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, new Color(0, 168, 225)));
            total.setAlignment(Element.ALIGN_RIGHT);
            document.add(total);

            // 6. Digital Signature & QR
            document.add(new Paragraph("\n\n\n"));
            
            PdfPTable footerTable = new PdfPTable(2);
            footerTable.setWidthPercentage(100);
            
            PdfPCell certCell = new PdfPCell();
            certCell.setBorder(com.lowagie.text.Rectangle.BOX);
            certCell.setPadding(10);
            certCell.addElement(new Phrase("CERTIFICATION NUMÉRIQUE", boldFont));
            String signature = java.util.Base64.getEncoder().encodeToString((payment.getTransactionId() + signatureSecret).getBytes()).substring(0, 32);
            certCell.addElement(new Phrase("\nID : " + UUID.randomUUID().toString().substring(0, 8).toUpperCase(), smallFont));
            certCell.addElement(new Phrase("\nSignature : " + signature, smallFont));
            footerTable.addCell(certCell);
            
            // 7. --- RED STAMP (TAMPON ROUGE) ---
            PdfPCell stampCell = new PdfPCell();
            stampCell.setBorder(com.lowagie.text.Rectangle.NO_BORDER);
            stampCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            stampCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            footerTable.addCell(stampCell);
            
            document.add(footerTable);

            // Drawing the stamp manually with PdfContentByte
            com.lowagie.text.pdf.PdfContentByte cb = writer.getDirectContent();
            cb.saveState();
            cb.setRGBColorStroke(220, 20, 60); // Crimson Red
            cb.setLineWidth(2f);
            cb.setLineDash(3, 3);
            
            // Position for stamp at bottom right
            float x = 450;
            float y = 100;
            
            // Circle
            cb.circle(x, y, 40);
            cb.stroke();
            
            // Text inside
            BaseFont bfBold = BaseFont.createFont(BaseFont.HELVETICA_BOLD, BaseFont.CP1252, BaseFont.NOT_EMBEDDED);
            BaseFont bfNormal = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.CP1252, BaseFont.NOT_EMBEDDED);
            
            cb.beginText();
            cb.setFontAndSize(bfBold, 14);
            cb.setRGBColorFill(220, 20, 60);
            cb.showTextAligned(Element.ALIGN_CENTER, "PAYÉ", x, y + 5, 25); // 25 degree rotation
            cb.setFontAndSize(bfNormal, 8);
            String stampDate = payment.getPaidAt() != null 
                    ? payment.getPaidAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                    : LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            cb.showTextAligned(Element.ALIGN_CENTER, stampDate, x, y - 15, 25);
            cb.endText();
            cb.restoreState();

            document.close();
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la génération du reçu PDF", e);
        }
    }
}
