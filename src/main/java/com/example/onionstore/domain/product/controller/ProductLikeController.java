package com.example.onionstore.domain.product.controller;

import com.example.onionstore.domain.product.facade.ProductLikeFacade;
import com.example.onionstore.global.dto.ApiResponse;
import com.example.onionstore.global.exception.BusinessException;
import com.example.onionstore.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/products/{productId}")
@RequiredArgsConstructor
public class ProductLikeController {
    private final ProductLikeFacade productLikeFacade;

    @PostMapping("/like")
    public ResponseEntity<ApiResponse<Void>> likeProduct(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long productId
    ) {
        if (jwt == null || jwt.getSubject() == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        Long userId = Long.valueOf(jwt.getSubject());

        productLikeFacade.toggleLike(userId, productId);

        return ResponseEntity.ok(ApiResponse.success("상품에 좋아요를 추가했습니다.", null));
    }
}
