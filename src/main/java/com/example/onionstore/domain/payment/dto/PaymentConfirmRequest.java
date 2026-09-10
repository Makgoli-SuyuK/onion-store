package com.example.onionstore.domain.payment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PaymentConfirmRequest(
        @NotNull(message = "주문 ID는 필수입니다.")
        Long orderId,
        @NotBlank(message = "포트원 결제 ID는 필수입니다.")
        String portonePaymentId
) {}
