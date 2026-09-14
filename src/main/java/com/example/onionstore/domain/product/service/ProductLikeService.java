package com.example.onionstore.domain.product.service;

import com.example.onionstore.domain.product.entity.Product;
import com.example.onionstore.domain.product.entity.ProductLike;
import com.example.onionstore.domain.product.repository.ProductLikeRepository;
import com.example.onionstore.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProductLikeService {
    private final ProductLikeRepository productLikeRepository;

    @Transactional
    public void likeProduct(User user, Product product) {
        productLikeRepository.save(ProductLike.create(user, product));
    }

    @Transactional(readOnly = true)
    public Optional<ProductLike> getProductLike(Long userId, Long productId) {
        return productLikeRepository.findByUser_IdAndProduct_Id(userId, productId);
    }

    @Transactional
    public void deleteProductLike(Long userId, Long productId) {
        productLikeRepository.deleteByUser_IdAndProduct_Id(userId, productId);
    }
}
