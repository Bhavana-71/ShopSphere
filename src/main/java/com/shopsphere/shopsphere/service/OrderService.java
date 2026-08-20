package com.shopsphere.shopsphere.service;

import com.shopsphere.shopsphere.dto.CartItemResponse;
import com.shopsphere.shopsphere.dto.OrderResponse;
import com.shopsphere.shopsphere.entity.*;
import com.shopsphere.shopsphere.repository.CartItemRepository;
import com.shopsphere.shopsphere.repository.OrderRepository;
import com.shopsphere.shopsphere.repository.ProductRepository;
import com.shopsphere.shopsphere.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartService cartService;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    @Transactional
    public OrderResponse placeOrder(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found: " + email));

        Cart cart = cartService.getOrCreateCart(email);

        if (cart.getItems().isEmpty()) {
            throw new RuntimeException("Cannot place order: cart is empty");
        }

        double totalAmount = 0;
        Order order = Order.builder()
                .user(user)
                .status(OrderStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();

        for (CartItem cartItem : cart.getItems()) {
            Product product = cartItem.getProduct();

            if (product.getStockQuantity() < cartItem.getQuantity()) {
                throw new RuntimeException("Insufficient stock for: " + product.getName());
            }

            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .product(product)
                    .quantity(cartItem.getQuantity())
                    .priceAtPurchase(product.getPrice())
                    .build();

            order.getItems().add(orderItem);
            totalAmount += product.getPrice() * cartItem.getQuantity();

            product.setStockQuantity(product.getStockQuantity() - cartItem.getQuantity());
            product.setUnitsSold(product.getUnitsSold() + cartItem.getQuantity());
            productRepository.save(product);
        }

        order.setTotalAmount(totalAmount);
        Order savedOrder = orderRepository.save(order);

        cartItemRepository.deleteAll(cart.getItems());
        cart.getItems().clear();

        return mapToResponse(savedOrder);
    }

    public List<OrderResponse> getMyOrders(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found: " + email));

        return orderRepository.findByUserId(user.getId()).stream()
                .map(this::mapToResponse)
                .toList();
    }

    public OrderResponse getOrderById(String email, Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));

        if (!order.getUser().getEmail().equals(email)) {
            throw new RuntimeException("This order does not belong to you");
        }

        return mapToResponse(order);
    }

    @Transactional
    public OrderResponse updateOrderStatus(Long orderId, OrderStatus status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));

        order.setStatus(status);
        return mapToResponse(orderRepository.save(order));
    }

    private OrderResponse mapToResponse(Order order) {
        List<CartItemResponse> items = order.getItems().stream()
                .map(item -> CartItemResponse.builder()
                        .id(item.getId())
                        .productId(item.getProduct().getId())
                        .productName(item.getProduct().getName())
                        .price(item.getPriceAtPurchase())
                        .quantity(item.getQuantity())
                        .subtotal(item.getPriceAtPurchase() * item.getQuantity())
                        .build())
                .toList();

        return OrderResponse.builder()
                .id(order.getId())
                .items(items)
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus())
                .createdAt(order.getCreatedAt())
                .build();
    }
}