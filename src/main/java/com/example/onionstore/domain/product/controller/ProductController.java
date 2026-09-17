package com.example.onionstore.domain.product.controller;

import com.example.onionstore.domain.product.dto.*;
import com.example.onionstore.domain.product.facade.ProductFacade;
import com.example.onionstore.domain.product.repository.dto.ProductSearchConditions;
import com.example.onionstore.domain.product.service.ProductService;
import com.example.onionstore.global.dto.ApiResponse;
import com.example.onionstore.global.exception.BusinessException;
import com.example.onionstore.global.exception.ErrorCode;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {
    private final ProductService productService;
    private final ProductFacade productFacade;

    @PostMapping
    public ResponseEntity<ApiResponse<Void>> addProduct(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody ProductCreateRequest createRequest
    ) {
        Long userId = extractUserId(jwt);

        productFacade.addProduct(userId, createRequest);

        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @GetMapping("/{productId}")
    public ResponseEntity<ApiResponse<ProductDetailWithLiked>> getProduct(
            @AuthenticationPrincipal() Jwt jwt,
            @PathVariable Long productId
    ) {
        Long userId = (jwt == null ? null : extractUserId(jwt));

        return ResponseEntity.ok(
                ApiResponse.success(productFacade.getProductDetail(userId, productId))
        );
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<ProductSimpleResponse>>> searchProducts(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Long priceStart,
            @RequestParam(required = false) Long priceEnd,
            @RequestParam(required = false) Integer likeCount,
            @PageableDefault(sort = "created_at", direction = Sort.Direction.ASC) Pageable pageable) {
        ProductSearchConditions conditions = new ProductSearchConditions(
                category,
                name,
                priceStart,
                priceEnd,
                likeCount
        );

        return ResponseEntity.ok(
                ApiResponse.success(productService.searchWithConditions(conditions, pageable)));
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long productId
    ) {
        Long userId = extractUserId(jwt);

        productFacade.deleteProduct(userId, productId);

        return ResponseEntity.ok(ApiResponse.success("상품 삭제에 성공했습니다.", null));
    }

    @PatchMapping("/{productId}")
    public ResponseEntity<ApiResponse<ProductResponse>> editProduct(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody ProductEditRequest editRequest,
            @PathVariable Long productId) {
        Long userId = extractUserId(jwt);

        return ResponseEntity.ok(
                ApiResponse.success(productFacade.editProduct(userId, productId, editRequest))
        );
    }

    @GetMapping("/like-count-top-10")
    public ResponseEntity<ApiResponse<List<ProductSimpleResponse>>> getProductLikeCountTop10() {
        return ResponseEntity.ok(
                ApiResponse.success(productService.find10OrderByLikeCountDesc())
        );
    }

    private Long extractUserId(Jwt jwt) {
        if (jwt == null || jwt.getSubject() == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        return Long.valueOf(jwt.getSubject());
    }
}
