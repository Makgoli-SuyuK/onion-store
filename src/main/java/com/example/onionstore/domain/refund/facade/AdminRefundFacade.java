package com.example.onionstore.domain.refund.facade;

import com.example.onionstore.domain.payment.port.GatewayCancellationStatus;
import com.example.onionstore.domain.payment.port.PaymentCancellationResponse;
import com.example.onionstore.domain.payment.port.PaymentGateway;
import com.example.onionstore.domain.refund.dto.admin.request.AdminRefundSearchCondition;
import com.example.onionstore.domain.refund.dto.admin.request.RefundRejectRequest;
import com.example.onionstore.domain.refund.dto.admin.response.AdminRefundDetailResponse;
import com.example.onionstore.domain.refund.dto.admin.response.AdminRefundPageResponse;
import com.example.onionstore.domain.refund.dto.admin.response.RefundReviewResponse;
import com.example.onionstore.domain.refund.dto.common.RefundCancellationInfo;
import com.example.onionstore.domain.refund.dto.common.RefundItemDetailResponse;
import com.example.onionstore.domain.refund.repository.dto.AdminRefundDetailProjection;
import com.example.onionstore.domain.refund.repository.dto.OrderItemSummaryRow;
import com.example.onionstore.domain.refund.service.RefundCommandService;
import com.example.onionstore.domain.refund.service.RefundService;
import com.example.onionstore.domain.user.entity.Role;
import com.example.onionstore.domain.user.service.UserService;
import com.example.onionstore.global.exception.BusinessException;
import com.example.onionstore.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
public class AdminRefundFacade {

    private final UserService userService;
    private final RefundService refundService;
    private final RefundCommandService refundCommandService;
    private final PaymentGateway paymentGateway;

    // 관리자 권한을 확인한 뒤 검색 조건에 맞는 환불 목록을 반환한다.
    @Transactional(readOnly = true)
    public AdminRefundPageResponse getRefunds(
            Long adminId,
            AdminRefundSearchCondition condition,
            int page,
            int size
    ) {
        validateAdmin(adminId);
        return AdminRefundPageResponse.from(refundService.searchAdminRefunds(
                condition,
                PageRequest.of(page - 1, size)
        ));
    }

    // 관리자만 주문·고객 정보를 포함한 환불 상세를 조회할 수 있다.
    @Transactional(readOnly = true)
    public AdminRefundDetailResponse getRefundDetail(Long adminId, Long refundId) {
        validateAdmin(adminId);
        AdminRefundDetailProjection projection = refundService
                .getAdminRefundDetailProjection(refundId);
        List<OrderItemSummaryRow> orderItems = refundService
                .getOrderItemSummaryRows(projection.orderId());
        List<RefundItemDetailResponse> refundItems = refundService
                .getRefundItemDetails(refundId);

        return AdminRefundDetailResponse.of(projection, orderItems, refundItems);
    }

    // 승인 상태를 먼저 커밋한 뒤 PortOne 부분 취소를 요청한다.
    public RefundReviewResponse approveRefund(Long adminId, Long refundId) {
        refundCommandService.approveCustomerRefund(adminId, refundId);
        RefundCancellationInfo refundInfo = refundService.getRefundCancellationInfo(refundId);

        PaymentCancellationResponse response;
        try {
            response = paymentGateway.requestPartialCancellation(
                    refundInfo.portonePaymentId(),
                    refundInfo.refundAmount(),
                    refundInfo.reason()
            );
        } catch (BusinessException exception) {
            refundCommandService.failRefund(refundId);
            throw exception;
        }
        return applyCancellationResult(refundId, response.status());
    }

    // 거절은 외부 결제사 호출 없이 환불 상태만 변경한다.
    public RefundReviewResponse rejectRefund(
            Long adminId,
            Long refundId,
            RefundRejectRequest request
    ) {
        return refundCommandService.rejectCustomerRefund(adminId, refundId, request.reason());
    }

    private RefundReviewResponse applyCancellationResult(
            Long refundId,
            GatewayCancellationStatus status
    ) {
        if (status == GatewayCancellationStatus.SUCCEEDED) {
            return refundCommandService.completeRefund(refundId);
        }
        if (status == GatewayCancellationStatus.FAILED) {
            return refundCommandService.failRefund(refundId);
        }
        return refundService.getRefundReviewResponse(refundId);
    }

    // 환불 검토 화면은 관리자 역할에만 허용한다.
    private void validateAdmin(Long userId) {
        if (userService.findUser(userId).getRole() != Role.ADMIN) {
            throw new BusinessException(ErrorCode.FORBIDDEN_ROLE);
        }
    }
}
