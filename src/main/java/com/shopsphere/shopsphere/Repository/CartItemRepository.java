package com.shopsphere.shopsphere.repository;

import com.shopsphere.shopsphere.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
}