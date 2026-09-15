package com.example.onionstore.domain.category.repository.cache;

import com.example.onionstore.domain.category.entity.Category;

import java.util.List;

public interface CategoryCache {
    List<Category> get();
    void put(List<Category> list);
    void evict();
}
