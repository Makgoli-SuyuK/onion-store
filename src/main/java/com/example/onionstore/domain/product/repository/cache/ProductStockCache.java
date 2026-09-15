package com.example.onionstore.domain.product.repository.cache;

public interface ProductStockCache {
    Integer get(Long productId);
    void put(Long productId, Integer stock);
    void evict(Long productId);
}
