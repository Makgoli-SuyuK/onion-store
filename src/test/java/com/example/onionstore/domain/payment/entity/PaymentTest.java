package com.example.onionstore.domain.payment.entity;

import com.example.onionstore.domain.order.entity.Order;
import com.example.onionstore.global.exception.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class PaymentTest {
    Payment payment = new Payment(mock(Order.class), 1000L);

    @Test
    void 성공중복요청은_결제시각을_바꾸지_않는다() {
        // given
        PaymentStatus initialStatus = payment.getStatus();

        // when
        boolean firstChanged = payment.markAsSuccess();
        var paidAt = payment.getPaidAt();
        boolean duplicateChanged = payment.markAsSuccess();

        // then
        assertEquals(PaymentStatus.READY, initialStatus);
        assertTrue(firstChanged);
        assertFalse(duplicateChanged);
        assertNotNull(paidAt);
        assertEquals(paidAt, payment.getPaidAt());
        assertEquals(PaymentStatus.SUCCESS, payment.getStatus());
    }

    @Test
    void 성공후_실패전이는_거절한다() {
        // given
        payment.markAsSuccess();

        // when
        var exception = assertThrows(BusinessException.class, payment::markAsFailed);

        // then
        assertEquals(ErrorCode.INVALID_PAYMENT_STATUS, exception.getErrorCode());
        assertEquals(PaymentStatus.SUCCESS, payment.getStatus());
    }

    @Test
    void 실패후_성공전이는_거절한다() {
        // given
        boolean firstChanged = payment.markAsFailed();

        // when
        boolean duplicateChanged = payment.markAsFailed();
        var exception = assertThrows(BusinessException.class, payment::markAsSuccess);

        // then
        assertTrue(firstChanged);
        assertFalse(duplicateChanged);
        assertEquals(ErrorCode.INVALID_PAYMENT_STATUS, exception.getErrorCode());
        assertEquals(PaymentStatus.FAILED, payment.getStatus());
        assertNull(payment.getPaidAt());
    }

    @Test
    void 취소후_성공전이는_거절한다() {
        // given
        boolean firstChanged = payment.markAsCancelled();

        // when
        boolean duplicateChanged = payment.markAsCancelled();
        var exception = assertThrows(BusinessException.class, payment::markAsSuccess);

        // then
        assertTrue(firstChanged);
        assertFalse(duplicateChanged);
        assertEquals(ErrorCode.INVALID_PAYMENT_STATUS, exception.getErrorCode());
        assertEquals(PaymentStatus.CANCELLED, payment.getStatus());
        assertNull(payment.getPaidAt());
    }
}
