package com.example.onionstore.domain.payment.controller;

import com.example.onionstore.domain.payment.dto.PaymentConfirmRequest;
import com.example.onionstore.domain.payment.dto.PaymentConfirmResponse;
import com.example.onionstore.domain.payment.facade.PaymentFacade;
import com.example.onionstore.global.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentFacade paymentFacade;

    @PostMapping("/confirm")
    public ResponseEntity<ApiResponse<PaymentConfirmResponse>> confirmPayment(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody PaymentConfirmRequest request
    ) {
        Long userId = Long.valueOf(jwt.getSubject());
        PaymentConfirmResponse response = paymentFacade.confirmPayment(
                userId,
                request.orderId(),
                request.portonePaymentId()
        );
        return ResponseEntity.ok(ApiResponse.success("결제가 정상적으로 완료되었습니다.", response));
    }
}
