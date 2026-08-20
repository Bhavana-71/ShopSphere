package com.shopsphere.shopsphere.dto;

import com.shopsphere.shopsphere.entity.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderResponse {
    private Long id;
    private List<CartItemResponse> items;
    private Double totalAmount;
    private OrderStatus status;
    private LocalDateTime createdAt;
}