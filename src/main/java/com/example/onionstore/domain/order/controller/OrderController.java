package com.example.onionstore.domain.order.controller;

import com.example.onionstore.domain.order.dto.*;
import com.example.onionstore.domain.order.facade.OrderFacade;
import com.example.onionstore.domain.order.service.OrderService;
import com.example.onionstore.global.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;
    private final OrderFacade orderFacade;

    // 개인회원 주문 상세 조회
    @GetMapping("/{orderId}")
    public ResponseEntity<ApiResponse<GetOrderResponse>> getOne(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long orderId) {
        return ResponseEntity.ok(ApiResponse.success("주문 상세 조회에 성공했습니다.", orderService.getOne(Long.valueOf(jwt.getSubject()), orderId)));
    }

    // 개인회원 주문 전체 조회 (페이징, 조건검색)
    @GetMapping
    public ResponseEntity<ApiResponse<Page<GetOrderListResponse>>> getAll(
            @AuthenticationPrincipal Jwt jwt,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            @ModelAttribute OrderSearchRequest request
            ) {

        return ResponseEntity.ok(ApiResponse.success(orderService.getAll(Long.valueOf(jwt.getSubject()), pageable, request)));
    }

    // 주문 생성
    @PostMapping
    public ResponseEntity<ApiResponse<OrderCreateResponse>>  createOrder(
            @AuthenticationPrincipal Jwt jwt,
            // request가 없으면 장바구니 전체주문
            @RequestBody(required = false) OrderCreateRequest request
    ) {
        Long userId = Long.valueOf(jwt.getSubject());
        OrderCreateResponse response = orderFacade.createOrder(userId, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 주문 취소
    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<ApiResponse<CancelOrderResponse>> cancelOrder(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long orderId
    ) {
        Long userId = Long.valueOf(jwt.getSubject());
        return ResponseEntity.ok(ApiResponse.success(orderFacade.cancelOrder(userId, orderId)));
    }
}
