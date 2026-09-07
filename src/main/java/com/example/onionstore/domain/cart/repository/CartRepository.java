package com.example.onionstore.domain.cart.repository;

import com.example.onionstore.domain.cart.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartRepository extends JpaRepository<Cart, Long> {
}
