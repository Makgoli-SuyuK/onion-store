package com.example.onionstore.domain.product.controller;

import com.example.onionstore.domain.product.dto.ProductCreateRequest;
import com.example.onionstore.domain.product.dto.ProductResponse;
import com.example.onionstore.domain.product.entity.Product;
import com.example.onionstore.domain.product.facade.ProductFacade;
import com.example.onionstore.domain.product.service.ProductService;
import com.example.onionstore.global.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {
    private final ProductService productService;
    private final ProductFacade productFacade;

    @PostMapping
    public ResponseEntity<ApiResponse<Void>> addProduct(@Valid @RequestBody ProductCreateRequest createRequest) {
        productFacade.addProduct(createRequest);

        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @GetMapping("/{productId}")
    public ResponseEntity<ApiResponse<ProductResponse>> getProduct(@PathVariable Long productId) {
        return ResponseEntity.ok(
                ApiResponse.success(productService.findById(productId))
        );
    }
}
