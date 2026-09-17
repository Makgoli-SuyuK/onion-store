package com.example.onionstore.domain.payment.service;

import com.example.onionstore.domain.order.entity.Order;
import com.example.onionstore.domain.user.entity.User;
import com.example.onionstore.domain.payment.entity.Payment;
import com.example.onionstore.domain.payment.entity.PaymentStatus;
import com.example.onionstore.domain.payment.repository.PaymentRepository;
import com.example.onionstore.global.exception.BusinessException;
import com.example.onionstore.global.exception.ErrorCode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)

class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @InjectMocks
    private PaymentService paymentService;

    @Test
    void 결제정보가_없는_주문조회() {
        // given
        when(paymentRepository.findByOrderId(1L)).thenReturn(Optional.empty());

        // when
        BusinessException exception = assertThrows(BusinessException.class, () -> paymentService.getPaymentByOrderId(1L));

        // then
        assertEquals(ErrorCode.PAYMENT_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    void 다른사용자의_결제확인은_거절한다() {
        // given
        var order = mock(Order.class);
        var user = mock(User.class);
        when(order.getUser()).thenReturn(user);
        when(user.getId()).thenReturn(2L);
        var payment = new Payment(order, 1000L);
        when(paymentRepository.findByPortonePaymentIdAndOrderIdWithOrderAndUser("pay", 1L))
                .thenReturn(Optional.of(payment));

        // when
        var exception = assertThrows(BusinessException.class,
                () -> paymentService.getPaymentConfirmationInfo(3L, 1L, "pay"));

        // then
        assertEquals(ErrorCode.FORBIDDEN_ROLE, exception.getErrorCode());
    }

    @Test
    void 성공한결제에_늦은실패가_와도_상태를_유지한다() {
        // given
        var order = mock(Order.class);
        var payment = new Payment(order, 1000L);
        payment.markAsSuccess();
        when(paymentRepository.findByOrderIdForUpdate(1L)).thenReturn(Optional.of(payment));

        // when
        var result = paymentService.applyFailure(1L).changed();

        // then
        assertFalse(result);
        assertEquals(PaymentStatus.SUCCESS, payment.getStatus());
    }

    @Test
    void 실패처리_재시도는_변경없음을_반환한다() {
        // given
        var order = mock(Order.class);
        var payment = new Payment(order, 1000L);
        when(paymentRepository.findByOrderIdForUpdate(1L)).thenReturn(Optional.of(payment));

        // when
        var firstResult = paymentService.applyFailure(1L);
        var duplicateResult = paymentService.applyFailure(1L);

        // then
        assertTrue(firstResult.changed());
        assertFalse(duplicateResult.changed());
    }

    @Test
    void 웹훅의_결제ID가_없으면_조회실패한다() {
        // given
        when(paymentRepository.findByPortonePaymentIdWithOrder("missing")).thenReturn(Optional.empty());

        // when
        var exception = assertThrows(BusinessException.class,
                () -> paymentService.getPaymentConfirmationInfoByPortonePaymentId("missing"));

        // then
        assertEquals(ErrorCode.PAYMENT_NOT_FOUND, exception.getErrorCode());
    }
}