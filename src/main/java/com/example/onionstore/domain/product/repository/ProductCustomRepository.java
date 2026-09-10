package com.example.onionstore.domain.product.repository;

import com.example.onionstore.domain.product.dto.ProductSimpleResponse;
import com.example.onionstore.domain.product.repository.dto.ProductSearchConditions;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductCustomRepository {
    Page<ProductSimpleResponse> searchWithConditions(ProductSearchConditions searchConditions, Pageable pageable);
}
