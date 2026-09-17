package com.example.onionstore.domain.refund.controller;

import com.example.onionstore.domain.refund.dto.customer.request.CustomerRefundRequest;
import com.example.onionstore.domain.refund.dto.customer.response.CustomerRefundDetailResponse;
import com.example.onionstore.domain.refund.dto.customer.response.CustomerRefundSummaryResponse;
import com.example.onionstore.domain.refund.facade.CustomerRefundFacade;
import com.example.onionstore.global.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class CustomerRefundController {

    private final CustomerRefundFacade customerRefundFacade;

    @PostMapping("/orders/{orderId}/refunds")
    public ResponseEntity<ApiResponse<CustomerRefundSummaryResponse>> requestRefund(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long orderId,
            @Valid @RequestBody CustomerRefundRequest request
    ) {
        Long userId = Long.valueOf(jwt.getSubject());

        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(
                "환불 요청을 접수했습니다.", customerRefundFacade.requestCustomerRefund(userId, orderId, request)
        ));
    }

    @GetMapping("/refunds")
    public ResponseEntity<ApiResponse<List<CustomerRefundSummaryResponse>>> getMyRefunds(
            @AuthenticationPrincipal Jwt jwt
    ) {
        Long userId = Long.valueOf(jwt.getSubject());

        return ResponseEntity.ok(ApiResponse.success("내 환불 이력을 조회했습니다.", customerRefundFacade.getMyRefunds(userId)
        ));
    }

    @GetMapping("/refunds/{refundId}")
    public ResponseEntity<ApiResponse<CustomerRefundDetailResponse>> getMyRefundDetail(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long refundId
    ) {
        Long userId = Long.valueOf(jwt.getSubject());
        return ResponseEntity.ok(ApiResponse.success("환불 상세를 조회했습니다.", customerRefundFacade.getMyRefundDetail(userId, refundId)
        ));
    }
}