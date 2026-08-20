package com.shopsphere.shopsphere.service;

import com.shopsphere.shopsphere.dto.CartItemRequest;
import com.shopsphere.shopsphere.dto.CartItemResponse;
import com.shopsphere.shopsphere.dto.CartResponse;
import com.shopsphere.shopsphere.entity.*;
import com.shopsphere.shopsphere.repository.CartItemRepository;
import com.shopsphere.shopsphere.repository.CartRepository;
import com.shopsphere.shopsphere.repository.ProductRepository;
import com.shopsphere.shopsphere.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public Cart getOrCreateCart(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found: " + email));

        return cartRepository.findByUserId(user.getId())
                .orElseGet(() -> cartRepository.save(
                        Cart.builder().user(user).build()
                ));
    }

    public CartResponse addItem(String email, CartItemRequest request) {
        Cart cart = getOrCreateCart(email);

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found: " + request.getProductId()));

        if (product.getStockQuantity() < request.getQuantity()) {
            throw new RuntimeException("Insufficient stock for product: " + product.getName());
        }

        CartItem existingItem = cart.getItems().stream()
                .filter(item -> item.getProduct().getId().equals(product.getId()))
                .findFirst()
                .orElse(null);

        if (existingItem != null) {
            existingItem.setQuantity(existingItem.getQuantity() + request.getQuantity());
            cartItemRepository.save(existingItem);
        } else {
            CartItem newItem = CartItem.builder()
                    .cart(cart)
                    .product(product)
                    .quantity(request.getQuantity())
                    .build();
            cart.getItems().add(newItem);
            cartItemRepository.save(newItem);
        }

        return mapToResponse(cart);
    }

    public CartResponse getCart(String email) {
        Cart cart = getOrCreateCart(email);
        return mapToResponse(cart);
    }

    public CartResponse updateItemQuantity(String email, Long itemId, Integer quantity) {
        Cart cart = getOrCreateCart(email);

        CartItem item = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Cart item not found: " + itemId));

        if (!item.getCart().getId().equals(cart.getId())) {
            throw new RuntimeException("This item does not belong to your cart");
        }

        if (quantity <= 0) {
            cart.getItems().remove(item);
            cartItemRepository.delete(item);
        } else {
            item.setQuantity(quantity);
            cartItemRepository.save(item);
        }

        return mapToResponse(cart);
    }

    public CartResponse removeItem(String email, Long itemId) {
        return updateItemQuantity(email, itemId, 0);
    }

    public void clearCart(String email) {
        Cart cart = getOrCreateCart(email);
        cartItemRepository.deleteAll(cart.getItems());
        cart.getItems().clear();
        cartRepository.save(cart);
    }

    private CartResponse mapToResponse(Cart cart) {
        List<CartItemResponse> itemResponses = cart.getItems().stream()
                .map(item -> CartItemResponse.builder()
                        .id(item.getId())
                        .productId(item.getProduct().getId())
                        .productName(item.getProduct().getName())
                        .price(item.getProduct().getPrice())
                        .quantity(item.getQuantity())
                        .subtotal(item.getProduct().getPrice() * item.getQuantity())
                        .build())
                .toList();

        double total = itemResponses.stream()
                .mapToDouble(CartItemResponse::getSubtotal)
                .sum();

        return CartResponse.builder()
                .cartId(cart.getId())
                .items(itemResponses)
                .totalAmount(total)
                .build();
    }
}