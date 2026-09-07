package com.example.onionstore.domain.product.repository;

import com.example.onionstore.domain.product.entity.ProductLike;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductLikeRepository extends JpaRepository<ProductLike, Long> {
}
