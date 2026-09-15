package com.example.onionstore.domain.refund.service;

import com.example.onionstore.domain.payment.entity.Payment;
import com.example.onionstore.domain.refund.entity.Refund;
import com.example.onionstore.domain.refund.entity.RefundInitiator;
import com.example.onionstore.domain.refund.entity.RefundReasonType;
import com.example.onionstore.domain.refund.entity.RefundStatus;
import com.example.onionstore.domain.refund.repository.RefundRepository;
import com.example.onionstore.domain.refund.repository.dto.CustomerRefundDetailProjection;
import com.example.onionstore.global.exception.BusinessException;
import com.example.onionstore.global.exception.ErrorCode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RefundServiceTest {

    @Mock
    private RefundRepository refundRepository;

    @InjectMocks
    private RefundService refundService;

    @Test
    void 본인_환불상세를_조회한다() {
        CustomerRefundDetailProjection projection = projection(1L);
        given(refundRepository.findCustomerRefundDetailProjection(10L)).willReturn(Optional.of(projection));

        CustomerRefundDetailProjection result = refundService.getCustomerRefundDetailProjection(1L, 10L);

        assertThat(result).isSameAs(projection);
    }

    @Test
    void 존재하지_않는_환불상세는_찾을수없음으로_처리한다() {
        given(refundRepository.findCustomerRefundDetailProjection(10L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> refundService.getCustomerRefundDetailProjection(1L, 10L))
                .isInstanceOfSatisfying(BusinessException.class,
                        exception -> assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.REFUND_NOT_FOUND));
    }

    @Test
    void 다른_고객의_환불상세는_접근을_거부한다() {
        given(refundRepository.findCustomerRefundDetailProjection(10L))
                .willReturn(Optional.of(projection(2L)));

        assertThatThrownBy(() -> refundService.getCustomerRefundDetailProjection(1L, 10L))
                .isInstanceOfSatisfying(BusinessException.class,
                        exception -> assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.ACCESS_DENIED));
    }

    @Test
    void 진행중인_환불이_없으면_새_환불요청을_허용한다() {
        given(refundRepository.findActiveByPaymentIdForUpdate(eq(20L), any()))
                .willReturn(Optional.empty());

        refundService.validateNoActiveRefund(20L);

        verify(refundRepository).findActiveByPaymentIdForUpdate(
                eq(20L),
                eq(List.of(RefundStatus.PENDING_APPROVAL, RefundStatus.REQUESTED))
        );
    }

    @Test
    void 완료된_포트원_취소는_환불완료로_한번만_반영한다() {
        Refund refund = refund(RefundStatus.REQUESTED);
        given(refundRepository.findByIdForUpdate(30L)).willReturn(Optional.of(refund));

        assertThat(refundService.completeRefund(30L)).isTrue();
        assertThat(refundService.completeRefund(30L)).isFalse();
        assertThat(refund.getStatus()).isEqualTo(RefundStatus.COMPLETED);
    }

    private CustomerRefundDetailProjection projection(Long ownerUserId) {
        return new CustomerRefundDetailProjection(
                ownerUserId,
                1L,
                "ORD-001",
                10_000L,
                10L,
                RefundStatus.PENDING_APPROVAL,
                RefundInitiator.CUSTOMER,
                RefundReasonType.CUSTOMER_REQUEST,
                "단순 변심",
                10_000L,
                null,
                null,
                null
        );
    }

    private Refund refund(RefundStatus status) {
        return new Refund(
                mock(Payment.class),
                10_000L,
                status,
                RefundInitiator.CUSTOMER,
                RefundReasonType.CUSTOMER_REQUEST,
                "단순 변심"
        );
    }
}
