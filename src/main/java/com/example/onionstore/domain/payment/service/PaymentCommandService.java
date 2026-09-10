package com.example.onionstore.domain.payment.service;

import com.example.onionstore.domain.order.service.OrderService;
import com.example.onionstore.domain.payment.dto.PaymentConfirmResponse;
import com.example.onionstore.domain.payment.dto.PaymentConfirmationInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PaymentCommandService {

    private final PaymentService paymentService;
    private final OrderService orderService;

    // 결제 성공과 주문 유료 상태를 함께 확정한다.
    @Transactional
    public PaymentConfirmResponse completePaymentSuccess(PaymentConfirmationInfo info) {
        paymentService.applySuccess(info.orderId());
        orderService.markAsPaid(info.orderId());
        return PaymentConfirmResponse.success(info.orderId(), info.portonePaymentId());
    }
}
