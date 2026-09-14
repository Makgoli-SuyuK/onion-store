package com.example.onionstore.domain.payment.facade;

import com.example.onionstore.domain.payment.dto.*;
import com.example.onionstore.domain.payment.port.*;
import com.example.onionstore.domain.payment.service.*;
import com.example.onionstore.global.exception.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PaymentFacadeTest {
    PaymentService payments = mock(PaymentService.class);
    PaymentCommandService commands = mock(PaymentCommandService.class);
    PaymentGateway gateway = mock(PaymentGateway.class);
    PaymentFacade facade = new PaymentFacade(payments, commands, gateway);
    PaymentConfirmationInfo info = new PaymentConfirmationInfo(1L, "pay-1", 1000L);

    @Test
    void 정상결제는_검증후_확정한다() {
        // given
        when(payments.getPaymentConfirmationInfo(2L, 1L, "pay-1")).thenReturn(info);
        when(gateway.getPayment("pay-1")).thenReturn(new PaymentGatewayResponse("pay-1", GatewayPaymentStatus.PAID, 1000L));
        PaymentConfirmResponse expected = PaymentConfirmResponse.success(1L, "pay-1");
        when(commands.completePaymentSuccess(info)).thenReturn(expected);

        // when
        var result = facade.confirmPayment(2L, 1L, "pay-1");

        // then
        assertEquals(expected, result);
        verify(gateway, never()).cancelPayment(anyString(), anyString());
    }

    @Test
    void 결제조회가_거절되면_PG를_호출하지_않는다() {
        // given
        when(payments.getPaymentConfirmationInfo(2L, 1L, "pay-1"))
                .thenThrow(new BusinessException(ErrorCode.FORBIDDEN_ROLE));

        // when
        var exception = assertThrows(BusinessException.class,
                () -> facade.confirmPayment(2L, 1L, "pay-1"));

        // then
        assertEquals(ErrorCode.FORBIDDEN_ROLE, exception.getErrorCode());
        verifyNoInteractions(gateway, commands);
    }

    @Test
    void 금액불일치면_PG취소후_내부취소한다() {
        // given
        when(payments.getPaymentConfirmationInfoByPortonePaymentId("pay-1")).thenReturn(info);
        when(gateway.getPayment("pay-1")).thenReturn(new PaymentGatewayResponse("pay-1", GatewayPaymentStatus.PAID, 999L));

        // when
        var exception = assertThrows(BusinessException.class,
                () -> facade.confirmPaymentFromWebhook("pay-1"));

        // then
        assertEquals(ErrorCode.PAYMENT_AMOUNT_MISMATCH, exception.getErrorCode());
        var order = inOrder(gateway, commands);
        order.verify(gateway).cancelPayment("pay-1", "결제 금액 불일치 자동 취소");
        order.verify(commands).cancelPaymentForAmountMismatch(1L);
        verify(commands, never()).completePaymentSuccess(any());
    }

    @Test
    void PG취소실패면_내부상태를_취소하지_않는다() {
        // given
        when(payments.getPaymentConfirmationInfoByPortonePaymentId("pay-1")).thenReturn(info);
        when(gateway.getPayment("pay-1")).thenReturn(new PaymentGatewayResponse("pay-1", GatewayPaymentStatus.PAID, null));
        doThrow(new BusinessException(ErrorCode.PAYMENT_CANCELLATION_FAILED)).when(gateway).cancelPayment(anyString(), anyString());

        // when
        var exception = assertThrows(BusinessException.class,
                () -> facade.confirmPaymentFromWebhook("pay-1"));

        // then
        assertEquals(ErrorCode.PAYMENT_CANCELLATION_FAILED, exception.getErrorCode());
        verifyNoInteractions(commands);
    }

    @Test
    void 다른결제ID는_확정하지_않는다() {
        // given
        when(payments.getPaymentConfirmationInfoByPortonePaymentId("pay-1")).thenReturn(info);
        when(gateway.getPayment("pay-1")).thenReturn(new PaymentGatewayResponse("other", GatewayPaymentStatus.PAID, 1000L));

        // when
        var exception = assertThrows(BusinessException.class,
                () -> facade.confirmPaymentFromWebhook("pay-1"));

        // then
        assertEquals(ErrorCode.PAYMENT_FAILED, exception.getErrorCode());
        verifyNoInteractions(commands);
    }

    @ParameterizedTest
    @EnumSource(value = GatewayPaymentStatus.class, names = "PAID", mode = EnumSource.Mode.EXCLUDE)
    void 미완료결제는_성공처리하지_않는다(GatewayPaymentStatus status) {
        // given
        when(payments.getPaymentConfirmationInfoByPortonePaymentId("pay-1")).thenReturn(info);
        when(gateway.getPayment("pay-1")).thenReturn(new PaymentGatewayResponse("pay-1", status, 1000L));

        // when
        var exception = assertThrows(BusinessException.class,
                () -> facade.confirmPaymentFromWebhook("pay-1"));

        // then
        assertEquals(ErrorCode.PAYMENT_NOT_COMPLETED, exception.getErrorCode());
        verifyNoInteractions(commands);
    }

    @ParameterizedTest
    @EnumSource(value = GatewayPaymentStatus.class, names = {"UNKNOWN", "NOT_FOUND"})
    void 실패웹훅의_PG상태를_확인할수_없으면_오류를_반환한다(GatewayPaymentStatus status) {
        // given
        when(payments.getPaymentConfirmationInfoByPortonePaymentId("pay-1")).thenReturn(info);
        when(gateway.getPayment("pay-1"))
                .thenReturn(new PaymentGatewayResponse("pay-1", status, null));

        // when
        var exception = assertThrows(BusinessException.class,
                () -> facade.synchronizePaymentFailureFromWebhook("pay-1"));

        // then
        assertEquals(ErrorCode.PAYMENT_GATEWAY_ERROR, exception.getErrorCode());
        verifyNoInteractions(commands);
    }

    @ParameterizedTest
    @EnumSource(value = GatewayPaymentStatus.class,
            names = {"UNKNOWN", "NOT_FOUND"}, mode = EnumSource.Mode.EXCLUDE)
    void 실패웹훅은_PG실제상태로_판단한다(GatewayPaymentStatus status) {
        // given
        when(payments.getPaymentConfirmationInfoByPortonePaymentId("pay-1")).thenReturn(info);
        when(gateway.getPayment("pay-1"))
                .thenReturn(new PaymentGatewayResponse("pay-1", status, null));

        // when
        boolean changed = facade.synchronizePaymentFailureFromWebhook("pay-1");

        // then
        assertEquals(status == GatewayPaymentStatus.FAILED, changed);
        if (status == GatewayPaymentStatus.FAILED) {
            verify(commands).completePaymentFailure(1L);
        } else {
            verifyNoInteractions(commands);
        }
    }
}
