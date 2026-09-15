package com.example.onionstore.domain.refund.dto.admin.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// 관리자가 환불 요청을 거절할 때 사용하는 요청
public record RefundRejectRequest(
        @NotBlank @Size(max = 500) String reason
) {
}
