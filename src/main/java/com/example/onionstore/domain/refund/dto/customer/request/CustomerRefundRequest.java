package com.example.onionstore.domain.refund.dto.customer.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

// 고객 환불 생성 요청
public record CustomerRefundRequest(
        @NotBlank @Size(max = 500) String reason,
        @NotEmpty @Valid List<RefundItemRequest> items
) {
}