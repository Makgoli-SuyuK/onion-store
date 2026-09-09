package com.example.onionstore.domain.product.service;

import com.example.onionstore.domain.category.service.CategoryService;
import com.example.onionstore.domain.product.dto.ProductCreateRequest;
import com.example.onionstore.domain.product.entity.Product;
import com.example.onionstore.domain.product.repository.ProductRepository;
import com.example.onionstore.global.exception.BusinessException;
import com.example.onionstore.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;
    private final CategoryService categoryService;

    @Transactional
    public void createProduct(ProductCreateRequest createRequest) {
        if (createRequest.price() <= 0) {
            throw new BusinessException(ErrorCode.INVALID_PRICE);
        }

        if (createRequest.stock() <= 0) {
            throw new BusinessException(ErrorCode.INVALID_STOCK);
        }

        Product newProduct = Product.create(
                categoryService.getCategoryByName(createRequest.category()),
                createRequest.productName(),
                createRequest.description(),
                createRequest.price(),
                createRequest.stock()
        );

        productRepository.save(newProduct);
    }
}
