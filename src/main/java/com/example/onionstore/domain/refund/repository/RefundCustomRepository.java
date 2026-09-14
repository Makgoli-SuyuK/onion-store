package com.example.onionstore.domain.refund.repository;

import com.example.onionstore.domain.refund.dto.request.AdminRefundSearchCondition;
import com.example.onionstore.domain.refund.dto.response.AdminRefundListResponse;
import com.example.onionstore.domain.refund.dto.response.CustomerRefundSummaryResponse;
import com.example.onionstore.domain.refund.dto.response.RefundItemDetailResponse;
import com.example.onionstore.domain.refund.repository.dto.AdminRefundDetailHeader;
import com.example.onionstore.domain.refund.repository.dto.CustomerRefundDetailHeader;
import com.example.onionstore.domain.refund.repository.dto.OrderItemSummaryRow;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface RefundCustomRepository {
    List<CustomerRefundSummaryResponse> findCustomerRefunds(Long userId);

    // 환불ID와 사용자ID를 조건으로 다른사용자의 환불 상세조회 방지
    Optional<CustomerRefundDetailHeader> findCustomerRefundHeader(Long refundId, Long userId);

    List<OrderItemSummaryRow> findOrderItemSummaryRows(Long orderId);

    List<RefundItemDetailResponse> findRefundItemDetails(Long refundId);

    Page<AdminRefundListResponse> searchAdminRefunds(AdminRefundSearchCondition condition, Pageable pageable);

    Optional<AdminRefundDetailHeader> findAdminRefundDetailHeader(Long refundId);
}
