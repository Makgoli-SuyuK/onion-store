package com.example.onionstore.domain.product.repository;

import com.example.onionstore.domain.product.entity.ProductLike;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProductLikeRepository extends JpaRepository<ProductLike, Long> {
    Optional<ProductLike> findByUser_IdAndProduct_Id(Long userId, Long productId);
    void deleteByUser_IdAndProduct_Id(Long userId, Long productId);
}
