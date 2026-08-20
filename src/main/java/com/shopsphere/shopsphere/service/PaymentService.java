package com.shopsphere.shopsphere.service;

import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.razorpay.Order;
import com.shopsphere.shopsphere.entity.Payment;
import com.shopsphere.shopsphere.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;

    @Value("${razorpay.key.id}")
    private String keyId;

    @Value("${razorpay.key.secret}")
    private String keySecret;

    public JSONObject createRazorpayOrder(Long orderId, Double amount) throws RazorpayException {
        RazorpayClient client = new RazorpayClient(keyId, keySecret);

        JSONObject orderRequest = new JSONObject();
        orderRequest.put("amount", (Object) (int) (amount * 100));
        orderRequest.put("currency", (Object) "INR");
        orderRequest.put("receipt", (Object) ("order_rcpt_" + orderId));

        Order razorpayOrder = client.orders.create(orderRequest);

        Payment payment = Payment.builder()
                .razorpayOrderId(razorpayOrder.get("id"))
                .amount(amount)
                .status("CREATED")
                .createdAt(LocalDateTime.now())
                .build();
        paymentRepository.save(payment);

        JSONObject response = new JSONObject();
        response.put("razorpayOrderId", (Object) razorpayOrder.get("id"));
        response.put("amount", (Object) amount);
        response.put("currency", (Object) "INR");
        response.put("keyId", (Object) keyId);
        return response;
    }

    public void verifyAndUpdatePayment(String razorpayOrderId, String razorpayPaymentId, String razorpaySignature) throws RazorpayException {
        JSONObject options = new JSONObject();
        options.put("razorpay_order_id", (Object) razorpayOrderId);
        options.put("razorpay_payment_id", (Object) razorpayPaymentId);
        options.put("razorpay_signature", (Object) razorpaySignature);

        boolean isValid = com.razorpay.Utils.verifyPaymentSignature(options, keySecret);

        Payment payment = paymentRepository.findByRazorpayOrderId(razorpayOrderId)
                .orElseThrow(() -> new RuntimeException("Payment record not found"));

        payment.setRazorpayPaymentId(razorpayPaymentId);
        payment.setRazorpaySignature(razorpaySignature);
        payment.setStatus(isValid ? "PAID" : "FAILED");
        paymentRepository.save(payment);

        if (!isValid) {
            throw new RuntimeException("Payment signature verification failed");
        }
    }
}