package com.example.onionstore.domain.refund.entity;

import com.example.onionstore.domain.payment.entity.Payment;
import com.example.onionstore.domain.user.entity.Role;
import com.example.onionstore.domain.user.entity.User;
import com.example.onionstore.global.exception.BusinessException;
import com.example.onionstore.global.exception.ErrorCode;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

class RefundTest {

    @Test
    void 승인하면_환불이_취소요청_상태로_변경된다() {
        Refund refund = refund(RefundStatus.PENDING_APPROVAL);
        User admin = new User("admin@onion.store", "password", "관리자", "01012345678", Role.ADMIN);

        refund.approve(admin);

        assertThat(refund.getStatus()).isEqualTo(RefundStatus.REQUESTED);
        assertThat(refund.getReviewedBy()).isEqualTo(admin);
        assertThat(refund.getReviewedAt()).isNotNull();
    }

    @Test
    void 취소실패가_확정되면_환불이_실패상태로_변경된다() {
        Refund refund = refund(RefundStatus.REQUESTED);

        boolean changed = refund.fail();

        assertThat(changed).isTrue();
        assertThat(refund.getStatus()).isEqualTo(RefundStatus.FAILED);
    }

    @Test
    void 이미_완료된_환불을_다시_완료해도_상태를_변경하지_않는다() {
        Refund refund = refund(RefundStatus.COMPLETED);

        boolean changed = refund.complete();

        assertThat(changed).isFalse();
        assertThat(refund.getStatus()).isEqualTo(RefundStatus.COMPLETED);
    }

    @Test
    void 같은_포트원_취소ID를_다시_연결해도_성공으로_처리한다() {
        Refund refund = refund(RefundStatus.REQUESTED);

        assertThat(refund.assignCancellationId("cancel_123")).isTrue();
        assertThat(refund.assignCancellationId("cancel_123")).isFalse();
    }

    @Test
    void 다른_포트원_취소ID를_연결하면_예외가_발생한다() {
        Refund refund = refund(RefundStatus.REQUESTED);
        refund.assignCancellationId("cancel_123");

        assertThatThrownBy(() -> refund.assignCancellationId("cancel_456"))
                .isInstanceOfSatisfying(BusinessException.class,
                        exception -> assertThat(exception.getErrorCode())
                                .isEqualTo(ErrorCode.CANCELLATION_ALREADY_LINKED));
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
