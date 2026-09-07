package com.example.onionstore.domain.product.repository;

import com.example.onionstore.domain.product.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
}
