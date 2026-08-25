package com.shopsphere.shopsphere.controller;

import com.shopsphere.shopsphere.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/create/{orderId}")
    public ResponseEntity<Map<String, Object>> createPayment(@PathVariable Long orderId, @RequestParam Double amount) throws Exception {
        return ResponseEntity.ok(paymentService.createRazorpayOrder(orderId, amount));
    }

    @PostMapping("/verify")
    public ResponseEntity<String> verifyPayment(@RequestBody VerifyRequest request) throws Exception {
        paymentService.verifyAndUpdatePayment(
                request.getRazorpayOrderId(), request.getRazorpayPaymentId(), request.getRazorpaySignature());
        return ResponseEntity.ok("Payment verified successfully");
    }

    public static class VerifyRequest {
        private String razorpayOrderId;
        private String razorpayPaymentId;
        private String razorpaySignature;

        public String getRazorpayOrderId() { return razorpayOrderId; }
        public void setRazorpayOrderId(String v) { this.razorpayOrderId = v; }
        public String getRazorpayPaymentId() { return razorpayPaymentId; }
        public void setRazorpayPaymentId(String v) { this.razorpayPaymentId = v; }
        public String getRazorpaySignature() { return razorpaySignature; }
        public void setRazorpaySignature(String v) { this.razorpaySignature = v; }
    }
}