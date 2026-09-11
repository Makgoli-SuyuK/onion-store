package com.example.onionstore.domain.product.controller;

import com.example.onionstore.domain.product.dto.ProductCreateRequest;
import com.example.onionstore.domain.product.dto.ProductEditRequest;
import com.example.onionstore.domain.product.dto.ProductResponse;
import com.example.onionstore.domain.product.dto.ProductResponse;
import com.example.onionstore.domain.product.entity.Product;
import com.example.onionstore.domain.product.dto.ProductSimpleResponse;
import com.example.onionstore.domain.product.facade.ProductFacade;
import com.example.onionstore.domain.product.repository.dto.ProductSearchConditions;
import com.example.onionstore.domain.product.service.ProductService;
import com.example.onionstore.global.dto.ApiResponse;
import com.example.onionstore.global.exception.BusinessException;
import com.example.onionstore.global.exception.ErrorCode;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
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

    @GetMapping
    public ResponseEntity<ApiResponse<Page<ProductSimpleResponse>>> searchProducts(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Long priceStart,
            @RequestParam(required = false) Long priceEnd,
            @RequestParam(required = false) Integer likeCount,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortOrder,
            @RequestParam int page,
            @RequestParam int size) {
        ProductSearchConditions conditions = new ProductSearchConditions(
                category,
                name,
                priceStart,
                priceEnd,
                likeCount,
                sortBy,
                sortOrder,
                page,
                size
        );

        return ResponseEntity.ok(
                ApiResponse.success(productService.searchWithConditions(conditions, page, size)));
    }

    @PatchMapping("/{productId}")
    public ResponseEntity<ApiResponse<ProductResponse>> editProduct(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody ProductEditRequest editRequest,
            @PathVariable Long productId) {
        if (jwt == null || jwt.getSubject() == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        Long userId = Long.valueOf(jwt.getSubject());

        return ResponseEntity.ok(
                ApiResponse.success(productFacade.editProduct(userId, productId, editRequest))
        );
    }
}
