package com.example.onionstore.domain.product.repository;

import com.example.onionstore.domain.product.entity.Product;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long>, ProductCustomRepository {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Product p where p.id = :productId")
    Optional<Product> findByIdForUpdate(@Param("productId") Long productId);

    @Query("SELECT p FROM Product p JOIN FETCH p.category WHERE p.id = :id AND p.deleted = false")
    Optional<Product> findByIdAndDeletedFalse(@Param("id") Long id);

    @Query("SELECT p FROM Product p JOIN FETCH p.category WHERE p.deleted = false ORDER BY p.likeCount DESC LIMIT 10")
    List<Product> find10OrderByLikeCountDesc();
}
