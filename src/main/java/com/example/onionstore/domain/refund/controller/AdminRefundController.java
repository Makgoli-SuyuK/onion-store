package com.example.onionstore.domain.refund.controller;

import com.example.onionstore.domain.refund.dto.admin.request.AdminRefundSearchCondition;
import com.example.onionstore.domain.refund.dto.admin.request.RefundRejectRequest;
import com.example.onionstore.domain.refund.dto.admin.response.AdminRefundDetailResponse;
import com.example.onionstore.domain.refund.dto.admin.response.AdminRefundPageResponse;
import com.example.onionstore.domain.refund.dto.admin.response.RefundReviewResponse;
import com.example.onionstore.domain.refund.entity.RefundStatus;
import com.example.onionstore.domain.refund.facade.AdminRefundFacade;
import com.example.onionstore.global.dto.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("/api/admin/refunds")
public class AdminRefundController {

    private final AdminRefundFacade adminRefundFacade;

    @GetMapping
    public ResponseEntity<ApiResponse<AdminRefundPageResponse>> getRefunds(
            @AuthenticationPrincipal Jwt jwt,
            @ModelAttribute AdminRefundSearchCondition condition,
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size
    ) {
        Long adminId = Long.valueOf(jwt.getSubject());
        return ResponseEntity.ok(ApiResponse.success(
                "환불 목록을 조회했습니다.",
                adminRefundFacade.getRefunds(adminId, condition, page, size)
        ));
    }

    @GetMapping("/{refundId}")
    public ResponseEntity<ApiResponse<AdminRefundDetailResponse>> getRefundDetail(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long refundId
    ) {
        Long adminId = Long.valueOf(jwt.getSubject());
        return ResponseEntity.ok(ApiResponse.success(
                "환불 상세를 조회했습니다.",
                adminRefundFacade.getRefundDetail(adminId, refundId)
        ));
    }

    @PostMapping("/{refundId}/approve")
    public ResponseEntity<ApiResponse<RefundReviewResponse>> approveRefund(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long refundId
    ) {
        Long adminId = Long.valueOf(jwt.getSubject());
        RefundReviewResponse response = adminRefundFacade.approveRefund(adminId, refundId);

        if (response.status() == RefundStatus.REQUESTED) {
            return ResponseEntity.accepted().body(ApiResponse.success(
                    "환불 취소 요청을 처리하고 있습니다.", response
            ));
        }
        return ResponseEntity.ok(ApiResponse.success(
                "환불 요청을 처리했습니다.", response
        ));
    }

    @PostMapping("/{refundId}/reject")
    public ResponseEntity<ApiResponse<RefundReviewResponse>> rejectRefund(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long refundId,
            @Valid @RequestBody RefundRejectRequest request
    ) {
        Long adminId = Long.valueOf(jwt.getSubject());
        return ResponseEntity.ok(ApiResponse.success(
                "환불 요청을 거절했습니다.",
                adminRefundFacade.rejectRefund(adminId, refundId, request)
        ));
    }
}
