package com.example.onionstore.domain.payment.facade;

import com.example.onionstore.domain.payment.dto.PaymentConfirmResponse;
import com.example.onionstore.domain.payment.dto.PaymentConfirmationInfo;
import com.example.onionstore.domain.payment.port.GatewayPaymentStatus;
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
            Long userId, Long orderId, String portonePaymentId) {
        PaymentConfirmationInfo paymentInfo = paymentService.getPaymentConfirmationInfo(
                userId, orderId, portonePaymentId
        );

        return confirmPaidPayment(paymentInfo);
    }

    public void confirmPaymentFromWebhook(String portonePaymentId) {
        PaymentConfirmationInfo info =
                paymentService.getPaymentConfirmationInfoByPortonePaymentId(portonePaymentId);
        confirmPaidPayment(info);
    }
    public boolean synchronizePaymentFailureFromWebhook(String portonePaymentId) {
        PaymentConfirmationInfo info =
                paymentService.getPaymentConfirmationInfoByPortonePaymentId(portonePaymentId);

        PaymentGatewayResponse gatewayPayment = paymentGateway.getPayment(portonePaymentId);

        if (!info.portonePaymentId().equals(gatewayPayment.portonePaymentId())) {
            throw new BusinessException(ErrorCode.PAYMENT_FAILED);
        }

        if (gatewayPayment.status() == GatewayPaymentStatus.NOT_FOUND ||
                gatewayPayment.status() == GatewayPaymentStatus.UNKNOWN) {
            throw new BusinessException(ErrorCode.PAYMENT_GATEWAY_ERROR);
        }

        if (gatewayPayment.status() != GatewayPaymentStatus.FAILED) {
            return false;
        }

        paymentCommandService.completePaymentFailure(info.orderId());
        return true;
    }


    private PaymentConfirmResponse confirmPaidPayment(PaymentConfirmationInfo paymentInfo) {
        PaymentGatewayResponse gatewayResponse = paymentGateway.getPayment(paymentInfo.portonePaymentId());

        validatePortonePaymentIdAndStatus(paymentInfo, gatewayResponse);
        cancelMismatchedPayment(paymentInfo,gatewayResponse);

        return paymentCommandService.completePaymentSuccess(paymentInfo);
    }

    private void validatePortonePaymentIdAndStatus(PaymentConfirmationInfo paymentInfo, PaymentGatewayResponse gatewayPayment) {
        if (!paymentInfo.portonePaymentId().equals(gatewayPayment.portonePaymentId())) {
            throw new BusinessException(ErrorCode.PAYMENT_FAILED);
        }
        if (!gatewayPayment.isPaid()) {
            throw new BusinessException(ErrorCode.PAYMENT_NOT_COMPLETED);
        }
    }

    private void cancelMismatchedPayment(PaymentConfirmationInfo paymentInfo, PaymentGatewayResponse gatewayPayment) {
        if (gatewayPayment.totalAmount() == null || paymentInfo.amount() != gatewayPayment.totalAmount()) {
            paymentGateway.cancelPayment(paymentInfo.portonePaymentId(), "결제 금액 불일치 자동 취소");
            paymentCommandService.cancelPaymentForAmountMismatch(paymentInfo.orderId());
            throw new BusinessException(ErrorCode.PAYMENT_AMOUNT_MISMATCH);
        }
    }

}
