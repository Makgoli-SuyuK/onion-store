package com.example.onionstore.domain.product.controller;

import com.example.onionstore.domain.product.dto.ProductCreateRequest;
import com.example.onionstore.domain.product.service.ProductService;
import com.example.onionstore.global.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {
    private final ProductService productService;

    @PostMapping
    public ApiResponse<Void> addProduct(@Valid @RequestBody ProductCreateRequest createRequest) {
        productService.createProduct(createRequest);

        return ApiResponse.success(null);
    }
}
