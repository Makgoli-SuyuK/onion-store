package com.example.onionstore.domain.refund.repository;

import com.example.onionstore.domain.refund.dto.admin.request.AdminRefundSearchCondition;
import com.example.onionstore.domain.refund.dto.admin.response.AdminRefundListResponse;
import com.example.onionstore.domain.refund.dto.common.RefundItemDetailResponse;
import com.example.onionstore.domain.refund.dto.customer.response.CustomerRefundSummaryResponse;
import com.example.onionstore.domain.refund.repository.dto.AdminRefundDetailProjection;
import com.example.onionstore.domain.refund.repository.dto.CustomerRefundDetailProjection;
import com.example.onionstore.domain.refund.repository.dto.OrderItemSummaryRow;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface RefundCustomRepository {
    List<CustomerRefundSummaryResponse> findCustomerRefunds(Long userId);

    Optional<CustomerRefundDetailProjection> findCustomerRefundDetailProjection(Long refundId, Long userId);

    List<OrderItemSummaryRow> findOrderItemSummaryRows(Long orderId);

    List<RefundItemDetailResponse> findRefundItemDetails(Long refundId);

    Page<AdminRefundListResponse> searchAdminRefunds(AdminRefundSearchCondition condition, Pageable pageable);

    Optional<AdminRefundDetailProjection> findAdminRefundDetailProjection(Long refundId);
}
