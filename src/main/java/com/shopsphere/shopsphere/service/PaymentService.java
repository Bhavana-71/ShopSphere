package com.shopsphere.shopsphere.service;

import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.razorpay.Order;
import com.shopsphere.shopsphere.entity.OrderStatus;
import com.shopsphere.shopsphere.entity.Payment;
import com.shopsphere.shopsphere.repository.OrderRepository;
import com.shopsphere.shopsphere.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;

    @Value("${razorpay.key.id}")
    private String keyId;

    @Value("${razorpay.key.secret}")
    private String keySecret;

    public Map<String, Object> createRazorpayOrder(Long orderId, Double amount) throws RazorpayException {
        com.shopsphere.shopsphere.entity.Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));

        RazorpayClient client = new RazorpayClient(keyId, keySecret);

        JSONObject orderRequest = new JSONObject();
        orderRequest.put("amount", (Object) (int) (amount * 100));
        orderRequest.put("currency", (Object) "INR");
        orderRequest.put("receipt", (Object) ("order_rcpt_" + orderId));

        Order razorpayOrder = client.orders.create(orderRequest);

        Payment payment = Payment.builder()
                .order(order)
                .razorpayOrderId(razorpayOrder.get("id"))
                .amount(amount)
                .status("CREATED")
                .createdAt(LocalDateTime.now())
                .build();
        paymentRepository.save(payment);

        Map<String, Object> response = new HashMap<>();
        response.put("razorpayOrderId", razorpayOrder.get("id"));
        response.put("amount", amount);
        response.put("currency", "INR");
        response.put("keyId", keyId);
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

        if (isValid) {
            com.shopsphere.shopsphere.entity.Order order = payment.getOrder();
            order.setStatus(OrderStatus.CONFIRMED);
            orderRepository.save(order);
        } else {
            throw new RuntimeException("Payment signature verification failed");
        }
    }
}