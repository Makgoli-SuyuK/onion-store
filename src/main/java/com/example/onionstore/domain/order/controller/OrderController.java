package com.example.onionstore.domain.order.controller;

import com.example.onionstore.domain.order.dto.GetOrderResponse;
import com.example.onionstore.domain.order.dto.GetOrderListResponse;
import com.example.onionstore.domain.order.dto.OrderSearchRequest;
import com.example.onionstore.domain.order.service.OrderService;
import com.example.onionstore.global.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

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
}
