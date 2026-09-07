package com.example.onionstore.domain.cart.repository;

import com.example.onionstore.domain.cart.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
}
