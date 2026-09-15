package com.example.onionstore.domain.product.repository.cache;

public interface ProductLikeCache {
    Long get(Long productId);
    void put(Long productId, Long likeCount);
    void evict(Long productId);
}
