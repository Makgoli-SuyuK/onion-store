package com.example.onionstore.domain.refund.dto.response;

import com.example.onionstore.domain.refund.entity.RefundInitiator;
import com.example.onionstore.domain.refund.entity.RefundReasonType;
import com.example.onionstore.domain.refund.entity.RefundStatus;
import com.example.onionstore.domain.refund.repository.dto.CustomerRefundDetailHeader;
import com.example.onionstore.domain.refund.repository.dto.OrderItemSummaryRow;

import java.time.LocalDateTime;
import java.util.List;

public record CustomerRefundDetailResponse(
        RefundOrderSummaryResponse order,
        RefundInfo refund,
        List<RefundItemDetailResponse> refundItems
) {
    public static CustomerRefundDetailResponse of(
            CustomerRefundDetailHeader header,
            List<OrderItemSummaryRow> orderItemRows,
            List<RefundItemDetailResponse> refundItems
    ) {
        return new CustomerRefundDetailResponse(
                RefundOrderSummaryResponse.from(
                        header.orderNumber(),
                        header.orderAmount(),
                        orderItemRows
                ),
                RefundInfo.from(header),
                refundItems
        );
    }

    public record RefundInfo(
            Long refundId,
            RefundStatus status,
            RefundInitiator initiator,
            RefundReasonType reasonType,
            String reason,
            long requestedAmount,
            LocalDateTime requestedAt,
            LocalDateTime reviewedAt,
            String rejectionReason
    ) {
        private static RefundInfo from(CustomerRefundDetailHeader header) {
            return new RefundInfo(
                    header.refundId(), header.status(), header.initiator(),
                    header.reasonType(), header.reason(), header.requestedAmount(),
                    header.requestedAt(), header.reviewedAt(), header.rejectionReason()
            );
        }
    }
}
