package com.example.onionstore.domain.payment.facade;

import com.example.onionstore.domain.payment.dto.PaymentConfirmResponse;
import com.example.onionstore.domain.payment.dto.PaymentConfirmationInfo;
import com.example.onionstore.domain.payment.port.PaymentGateway;
import com.example.onionstore.domain.payment.port.PaymentGatewayResponse;
import com.example.onionstore.domain.payment.service.PaymentCommandService;
import com.example.onionstore.domain.payment.service.PaymentService;
import com.example.onionstore.global.exception.BusinessException;
import com.example.onionstore.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentFacade {

    private final PaymentService paymentService;
    private final PaymentCommandService paymentCommandService;
    private final PaymentGateway paymentGateway;

    public PaymentConfirmResponse confirmPayment(
            Long userId,
            Long orderId,
            String portonePaymentId
    ) { // 결제사 결과를 검증한 뒤 결제와 주문을 확정한다.
        PaymentConfirmationInfo paymentInfo = paymentService.getPaymentConfirmationInfo(
                userId,
                orderId,
                portonePaymentId
        );
        PaymentGatewayResponse gatewayPayment = paymentGateway.getPayment(portonePaymentId);
        validatePaymentIdAndStatus(paymentInfo, gatewayPayment);
        cancelMismatchedPayment(paymentInfo, gatewayPayment);

        return paymentCommandService.completePaymentSuccess(paymentInfo);
    }

    private void validatePaymentIdAndStatus(
            PaymentConfirmationInfo paymentInfo,
            PaymentGatewayResponse gatewayPayment
    ) {
        if (!paymentInfo.portonePaymentId().equals(gatewayPayment.portonePaymentId())) {
            throw new BusinessException(ErrorCode.PAYMENT_FAILED);
        }
        if (!gatewayPayment.paid()) {
            throw new BusinessException(ErrorCode.PAYMENT_NOT_COMPLETED);
        }
    }

    private void cancelMismatchedPayment(
            PaymentConfirmationInfo paymentInfo,
            PaymentGatewayResponse gatewayPayment
    ) {
        if (gatewayPayment.totalAmount() == null
                || paymentInfo.amount() != gatewayPayment.totalAmount()) {
            paymentGateway.cancelPayment(paymentInfo.portonePaymentId(), "결제 금액 불일치 자동 취소");
            paymentCommandService.cancelPaymentForAmountMismatch(paymentInfo.orderId());
            throw new BusinessException(ErrorCode.PAYMENT_AMOUNT_MISMATCH);
        }
    }
}
