package com.primevideo.controller;

import com.primevideo.dto.request.PaymentRequest;
import com.primevideo.dto.response.PaymentResponse;
import com.primevideo.entity.User;
import com.primevideo.repository.UserRepository;
import com.primevideo.dto.response.SubscriptionPlanDTO;
import com.primevideo.service.payment.PaymentService;
import com.primevideo.service.payment.BillingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final BillingService billingService;
    private final UserRepository userRepository;
    private final com.primevideo.service.subscription.SubscriptionService subscriptionService;

    // ── Pages Thymeleaf ───────────────────────────────────────────────────

    @GetMapping("/payment/checkout")
    public String checkoutPage(@RequestParam String plan, Model model) {
        List<SubscriptionPlanDTO> availablePlans = subscriptionService.getAvailablePlans();
        SubscriptionPlanDTO selectedPlan = availablePlans.stream()
                .filter(p -> p.getType().equals(plan))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Plan invalide"));

        PaymentRequest request = new PaymentRequest();
        request.setAmount(selectedPlan.getPrice());
        request.setCurrency(selectedPlan.getCurrency());
        request.setDescription("Abonnement " + selectedPlan.getName());

        model.addAttribute("plan", selectedPlan);
        model.addAttribute("paymentRequest", request);
        return "payment/checkout";
    }

    @PostMapping("/payment/checkout")
    public String processCheckout(@Valid @ModelAttribute("paymentRequest") PaymentRequest request,
                                  @RequestParam String planType,
                                  BindingResult result,
                                  java.security.Principal principal,
                                  Model model) {
        if (result.hasErrors()) {
            return checkoutPage(planType, model);
        }
        try {
            if (principal == null) throw new RuntimeException("Session expirée. Veuillez vous reconnecter.");
            String email = principal.getName();
            User user = userRepository.findByEmailIgnoreCase(email)
                    .orElseThrow(() -> new RuntimeException("Utilisateur introuvable (Email cherché: " + email + ")"));
            
            // 1. Traiter le paiement
            PaymentResponse response = paymentService.processPayment(request, user);
            
            // 2. Si succès, activer l'abonnement
            if (response.getStatus() == com.primevideo.entity.Payment.PaymentStatus.SUCCESS) {
                subscriptionService.subscribe(user, com.primevideo.entity.Subscription.Plan.valueOf(planType));
            }

            model.addAttribute("payment", response);
            return "payment/success";
        } catch (Exception e) {
            model.addAttribute("errorMsg", e.getMessage());
            return checkoutPage(planType, model);
        }
    }


    @GetMapping("/payment/history")
    public String paymentHistory(java.security.Principal principal, Model model) {
        if (principal == null) throw new RuntimeException("Non authentifié");
        String email = principal.getName();
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable (" + email + ")"));
        model.addAttribute("payments", paymentService.getUserPayments(user.getId()));
        return "payment/history";
    }

    @GetMapping("/payment/invoices")
    public String invoiceHistory(java.security.Principal principal, Model model) {
        if (principal == null) throw new RuntimeException("Non authentifié");
        String email = principal.getName();
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable (" + email + ")"));
        model.addAttribute("invoices", billingService.getUserInvoices(user.getId()));
        return "payment/invoices";
    }

    // ── API REST ──────────────────────────────────────────────────────────

    @PostMapping("/api/payments")
    @ResponseBody
    public ResponseEntity<PaymentResponse> apiProcessPayment(
            @Valid @RequestBody PaymentRequest request,
            java.security.Principal principal) {
        if (principal == null) throw new RuntimeException("Non authentifié");
        String email = principal.getName();
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable (" + email + ")"));
        return ResponseEntity.ok(paymentService.processPayment(request, user));
    }

    @GetMapping("/api/payments/history")
    @ResponseBody
    public ResponseEntity<List<PaymentResponse>> apiHistory(
            java.security.Principal principal) {
        if (principal == null) throw new RuntimeException("Non authentifié");
        String email = principal.getName();
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable (" + email + ")"));
        return ResponseEntity.ok(paymentService.getUserPayments(user.getId()));
    }

    @GetMapping("/api/payments/transactions")
    @ResponseBody
    public ResponseEntity<List<PaymentResponse>> apiTransactions(
            java.security.Principal principal) {
        if (principal == null) throw new RuntimeException("Non authentifié");
        String email = principal.getName();
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable (" + email + ")"));
        return ResponseEntity.ok(paymentService.getUserPayments(user.getId()));
    }

    @GetMapping("/api/payments/invoices")
    @ResponseBody
    public ResponseEntity<List<com.primevideo.dto.response.InvoiceDTO>> apiInvoices(
            java.security.Principal principal) {
        if (principal == null) throw new RuntimeException("Non authentifié");
        String email = principal.getName();
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable (" + email + ")"));
        return ResponseEntity.ok(billingService.getUserInvoices(user.getId()));
    }

    private final com.primevideo.service.ReportService reportService;
    private final com.primevideo.repository.PaymentRepository paymentRepository;

    @GetMapping("/payment/receipt/{transactionId}")
    public ResponseEntity<byte[]> getReceipt(@PathVariable String transactionId) {
        var payment = paymentRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> new RuntimeException("Transaction non trouvée"));

        byte[] pdf = reportService.generatePaymentReceipt(payment);

        return ResponseEntity.ok()
                .header("Content-Type", "application/pdf")
                .header("Content-Disposition", "attachment; filename=recu-" + transactionId + ".pdf")
                .body(pdf);
    }

    @GetMapping("/api/payments/invoices/download/{invoiceNumber}")
    @ResponseBody
    public ResponseEntity<String> downloadInvoice(@PathVariable String invoiceNumber) {
        return ResponseEntity.ok("Simulation du téléchargement de la facture " + invoiceNumber + ". En production, un fichier PDF serait généré ici.");
    }
}
