package com.example.onionstore.domain.refund.facade;

import com.example.onionstore.domain.refund.dto.admin.request.AdminRefundSearchCondition;
import com.example.onionstore.domain.refund.dto.admin.response.AdminRefundListResponse;
import com.example.onionstore.domain.refund.dto.admin.response.AdminRefundPageResponse;
import com.example.onionstore.domain.refund.dto.admin.response.RefundReviewResponse;
import com.example.onionstore.domain.refund.dto.common.RefundCancellationInfo;
import com.example.onionstore.domain.payment.port.GatewayCancellationStatus;
import com.example.onionstore.domain.payment.port.PaymentCancellationResponse;
import com.example.onionstore.domain.payment.port.PaymentGateway;
import com.example.onionstore.domain.refund.entity.RefundInitiator;
import com.example.onionstore.domain.refund.entity.RefundStatus;
import com.example.onionstore.domain.refund.service.RefundService;
import com.example.onionstore.domain.refund.service.RefundCommandService;
import com.example.onionstore.domain.user.entity.Role;
import com.example.onionstore.domain.user.entity.User;
import com.example.onionstore.domain.user.service.UserService;
import com.example.onionstore.global.exception.BusinessException;
import com.example.onionstore.global.exception.ErrorCode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class AdminRefundFacadeTest {

    @Mock
    private UserService userService;

    @Mock
    private RefundService refundService;

    @Mock
    private RefundCommandService refundCommandService;

    @Mock
    private PaymentGateway paymentGateway;

    @InjectMocks
    private AdminRefundFacade adminRefundFacade;

    @Test
    void 관리자는_환불목록을_조회할수있다() {
        User admin = new User("admin@onion.store", "password", "관리자", "01012345678", Role.ADMIN);
        AdminRefundListResponse refund = new AdminRefundListResponse(
                1L,
                "ORD-001",
                "고객",
                LocalDateTime.now(),
                RefundStatus.PENDING_APPROVAL,
                10_000L,
                10_000L,
                RefundInitiator.CUSTOMER
        );
        AdminRefundSearchCondition condition = new AdminRefundSearchCondition(null, null, null, null);
        given(userService.findUser(1L)).willReturn(admin);
        given(refundService.searchAdminRefunds(any(), any())).willReturn(new PageImpl<>(List.of(refund)));

        AdminRefundPageResponse response = adminRefundFacade.getRefunds(1L, condition, 1, 20);

        assertThat(response.content()).containsExactly(refund);
        assertThat(response.totalElements()).isEqualTo(1L);
    }

    @Test
    void 관리자_승인뒤_부분취소가_성공하면_환불완료를_반영한다() {
        RefundCancellationInfo cancellationInfo = new RefundCancellationInfo(
                10L, 20L, "pay_123", 3_000L, "상품 일부 환불"
        );
        RefundReviewResponse completed = new RefundReviewResponse(
                10L, RefundStatus.COMPLETED, LocalDateTime.now(), null
        );
        given(refundService.getRefundCancellationInfo(10L)).willReturn(cancellationInfo);
        given(paymentGateway.requestPartialCancellation("pay_123", 3_000L, "상품 일부 환불"))
                .willReturn(new PaymentCancellationResponse("cancel_123", GatewayCancellationStatus.SUCCEEDED));
        given(refundCommandService.completeRefund(10L)).willReturn(completed);

        RefundReviewResponse response = adminRefundFacade.approveRefund(1L, 10L);

        assertThat(response.status()).isEqualTo(RefundStatus.COMPLETED);
        verify(refundCommandService).approveCustomerRefund(1L, 10L);
        verify(paymentGateway).requestPartialCancellation("pay_123", 3_000L, "상품 일부 환불");
        verify(refundCommandService).completeRefund(10L);
    }

    @Test
    void 고객은_관리자_환불목록을_조회할수없다() {
        User customer = new User("customer@onion.store", "password", "고객", "01012345678", Role.CUSTOMER);
        given(userService.findUser(1L)).willReturn(customer);

        assertThatThrownBy(() -> adminRefundFacade.getRefunds(
                1L,
                new AdminRefundSearchCondition(null, null, null, null),
                1,
                20
        )).isInstanceOfSatisfying(BusinessException.class,
                exception -> assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.FORBIDDEN_ROLE));

        verifyNoInteractions(refundService);
    }
}
