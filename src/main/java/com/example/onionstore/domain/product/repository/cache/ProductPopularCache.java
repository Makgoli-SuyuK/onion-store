package com.example.onionstore.domain.product.repository.cache;

import com.example.onionstore.domain.product.dto.ProductSimpleResponse;

import java.util.List;

public interface ProductPopularCache {
    List<ProductSimpleResponse> get();
    void put(List<ProductSimpleResponse> list);
    void evict();
}
