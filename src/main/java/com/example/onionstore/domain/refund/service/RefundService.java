package com.example.onionstore.domain.refund.service;

import com.example.onionstore.domain.order.entity.OrderItem;
import com.example.onionstore.domain.payment.entity.Payment;
import com.example.onionstore.domain.refund.dto.admin.request.AdminRefundSearchCondition;
import com.example.onionstore.domain.refund.dto.admin.response.AdminRefundListResponse;
import com.example.onionstore.domain.refund.dto.admin.response.RefundReviewResponse;
import com.example.onionstore.domain.refund.dto.common.RefundCancellationInfo;
import com.example.onionstore.domain.refund.dto.common.RefundItemDetailResponse;
import com.example.onionstore.domain.refund.dto.customer.response.CustomerRefundSummaryResponse;
import com.example.onionstore.domain.refund.entity.Refund;
import com.example.onionstore.domain.refund.entity.RefundInitiator;
import com.example.onionstore.domain.refund.entity.RefundReasonType;
import com.example.onionstore.domain.refund.entity.RefundStatus;
import com.example.onionstore.domain.refund.repository.RefundRepository;
import com.example.onionstore.domain.refund.repository.dto.AdminRefundDetailProjection;
import com.example.onionstore.domain.refund.repository.dto.CompletedRefundQuantityRow;
import com.example.onionstore.domain.refund.repository.dto.CustomerRefundDetailProjection;
import com.example.onionstore.domain.refund.repository.dto.OrderItemSummaryRow;
import com.example.onionstore.domain.user.entity.User;
import com.example.onionstore.global.exception.BusinessException;
import com.example.onionstore.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RefundService {

    private final RefundRepository refundRepository;

    // 고객 ID로 소유권이 제한된 환불 요약 목록을 조회한다.
    public List<CustomerRefundSummaryResponse> getCustomerRefunds(Long userId) {
        return refundRepository.findCustomerRefunds(userId);
    }

    // 환불 존재 여부와 주문 소유권을 분리해 상세 조회 결과를 결정한다.
    public CustomerRefundDetailProjection getCustomerRefundDetailProjection(
            Long userId,
            Long refundId
    ) {
        CustomerRefundDetailProjection projection = refundRepository
                .findCustomerRefundDetailProjection(refundId)
                .orElseThrow(() -> new BusinessException(ErrorCode.REFUND_NOT_FOUND));

        if (!projection.ownerUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }
        return projection;
    }

    // 관리자 화면 조합에 필요한 환불 상세 projection을 조회한다.
    public AdminRefundDetailProjection getAdminRefundDetailProjection(Long refundId) {
        return refundRepository.findAdminRefundDetailProjection(refundId)
                .orElseThrow(() -> new BusinessException(ErrorCode.REFUND_NOT_FOUND));
    }

    // 주문 당시 상품명과 수량을 환불 상세에 표시하기 위해 조회한다.
    public List<OrderItemSummaryRow> getOrderItemSummaryRows(Long orderId) {
        return refundRepository.findOrderItemSummaryRows(orderId);
    }

    // 해당 환불에 실제 포함된 주문 항목과 요청 수량을 조회한다.
    public List<RefundItemDetailResponse> getRefundItemDetails(Long refundId) {
        return refundRepository.findRefundItemDetails(refundId);
    }

    // 관리자 검색 조건과 페이지 정보로 환불 목록을 조회한다.
    public Page<AdminRefundListResponse> searchAdminRefunds(
            AdminRefundSearchCondition condition,
            Pageable pageable
    ) {
        return refundRepository.searchAdminRefunds(condition, pageable);
    }

    // 같은 결제의 승인 대기·취소 요청 환불을 잠금 조회한다.
    public Optional<Refund> findActiveByPaymentIdForUpdate(Long paymentId) {
        return refundRepository.findActiveByPaymentIdForUpdate(
                paymentId,
                List.of(RefundStatus.PENDING_APPROVAL, RefundStatus.REQUESTED)
        );
    }

    // 진행 중인 환불이 있으면 새 요청 생성을 막는다.
    public void validateNoActiveRefund(Long paymentId) {
        if (findActiveByPaymentIdForUpdate(paymentId).isPresent()) {
            throw new BusinessException(ErrorCode.REFUND_ALREADY_IN_PROGRESS);
        }
    }

    // 완료된 환불 수량만 집계해 다음 요청의 가능한 수량을 계산한다.
    public Map<Long, Integer> getCompletedQuantityByOrderItemId(Long orderId) {
        return refundRepository.findCompletedRefundQuantities(orderId).stream()
                .collect(Collectors.toMap(
                        CompletedRefundQuantityRow::orderItemId,
                        row -> Math.toIntExact(row.completedQuantity())
                ));
    }

    // 고객 요청을 승인 대기 환불 aggregate로 생성하고 항목을 함께 저장한다.
    @Transactional
    public Refund createCustomerPending(
            Payment payment,
            long amount,
            String reason,
            Map<OrderItem, Integer> requestedQuantities
    ) {
        Refund refund = new Refund(
                payment,
                amount,
                RefundStatus.PENDING_APPROVAL,
                RefundInitiator.CUSTOMER,
                RefundReasonType.CUSTOMER_REQUEST,
                reason
        );
        requestedQuantities.forEach(refund::addItem);
        return refundRepository.save(refund);
    }

    // 승인 대기 환불을 취소 요청 상태로 전이하고 처리 관리자를 기록한다.
    @Transactional
    public Refund approveRefund(Long refundId, User admin) {
        Refund refund = findRefundForUpdate(refundId);
        refund.approve(admin);
        return refund;
    }

    // 승인 대기 환불을 거절하고 처리 관리자와 거절 사유를 기록한다.
    @Transactional
    public Refund rejectRefund(Long refundId, User admin, String rejectionReason) {
        Refund refund = findRefundForUpdate(refundId);
        refund.reject(admin, rejectionReason);
        return refund;
    }

    // 외부 취소 호출에 필요한 환불과 결제 식별 정보를 읽는다.
    public RefundCancellationInfo getRefundCancellationInfo(Long refundId) {
        Refund refund = findRefund(refundId);
        return new RefundCancellationInfo(
                refund.getId(),
                refund.getPayment().getOrder().getId(),
                refund.getPayment().getPortonePaymentId(),
                refund.getAmount(),
                refund.getReason()
        );
    }

    // 현재 환불 상태를 관리자 승인 API 응답으로 변환한다.
    public RefundReviewResponse getRefundReviewResponse(Long refundId) {
        return RefundReviewResponse.from(findRefund(refundId));
    }

    // 이미 잠긴 환불 aggregate의 완료 전이만 처리한다.
    public boolean completeRefund(Refund refund) {
        return refund.complete();
    }

    // PortOne 취소 완료가 확정된 환불을 완료 상태로 전이한다.
    @Transactional
    public boolean completeRefund(Long refundId) {
        return completeRefund(findRefundForUpdate(refundId));
    }

    // PortOne 취소 실패가 확정된 환불을 실패 상태로 전이한다.
    @Transactional
    public boolean failRefund(Long refundId) {
        return findRefundForUpdate(refundId).fail();
    }

    // 같은 취소 ID의 중복 연결은 무시하고 다른 취소 ID 연결은 차단한다.
    @Transactional
    public boolean assignCancellationId(Long refundId, String cancellationId) {
        return findRefundForUpdate(refundId).assignCancellationId(cancellationId);
    }

    // 여러 도메인 상태를 함께 변경하는 CommandService가 마지막 순서로 호출한다.
    public Refund findRefundForUpdate(Long refundId) {
        return refundRepository.findByIdForUpdate(refundId)
                .orElseThrow(() -> new BusinessException(ErrorCode.REFUND_NOT_FOUND));
    }

    private Refund findRefund(Long refundId) {
        return refundRepository.findById(refundId)
                .orElseThrow(() -> new BusinessException(ErrorCode.REFUND_NOT_FOUND));
    }
}
