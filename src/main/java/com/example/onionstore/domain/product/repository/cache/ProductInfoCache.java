package com.example.onionstore.domain.product.repository.cache;

import com.example.onionstore.domain.product.dto.ProductDto;

public interface ProductInfoCache {
    ProductDto get(Long productId);
    void put(Long productId, ProductDto productDto);
    void evict(Long productId);
}
