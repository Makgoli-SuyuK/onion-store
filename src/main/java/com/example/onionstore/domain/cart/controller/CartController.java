package com.example.onionstore.domain.cart.controller;

import com.example.onionstore.domain.cart.dto.request.AddCartItemRequest;
import com.example.onionstore.domain.cart.dto.response.CartItemResponse;
import com.example.onionstore.domain.cart.dto.response.CartResponse;
import com.example.onionstore.domain.cart.facade.CartFacade;
import com.example.onionstore.global.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/carts")
@RequiredArgsConstructor
public class CartController {

    private final CartFacade cartFacade;

    @GetMapping
    public ResponseEntity<ApiResponse<CartResponse>> getCart(
            @AuthenticationPrincipal Jwt jwt
    ) {
        Long userId = Long.valueOf(jwt.getSubject());
        CartResponse response = cartFacade.getCart(userId);
        return ResponseEntity.ok(ApiResponse.success("장바구니 조회에 성공했습니다.", response));
    }

    @PostMapping("/items")
    public ResponseEntity<ApiResponse<CartItemResponse>> addItem(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody AddCartItemRequest request
    ) {
        Long userId = Long.valueOf(jwt.getSubject());
        CartItemResponse response = cartFacade.addItem(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("장바구니에 상품을 담았습니다", response));
    }
}
