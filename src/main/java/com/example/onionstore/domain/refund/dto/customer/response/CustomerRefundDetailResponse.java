package com.example.onionstore.domain.refund.dto.customer.response;

import com.example.onionstore.domain.refund.dto.common.RefundItemDetailResponse;
import com.example.onionstore.domain.refund.dto.common.RefundOrderInfoResponse;
import com.example.onionstore.domain.refund.entity.RefundInitiator;
import com.example.onionstore.domain.refund.entity.RefundReasonType;
import com.example.onionstore.domain.refund.entity.RefundStatus;
import com.example.onionstore.domain.refund.repository.dto.CustomerRefundDetailProjection;
import com.example.onionstore.domain.refund.repository.dto.OrderItemSummaryRow;

import java.time.LocalDateTime;
import java.util.List;

// 고객 환불 상세 조회 응답
public record CustomerRefundDetailResponse(
        RefundOrderInfoResponse order,
        RefundInfo refund,
        List<RefundItemDetailResponse> refundItems
) {
    public static CustomerRefundDetailResponse of(
            CustomerRefundDetailProjection projection,
            List<OrderItemSummaryRow> orderItemRows,
            List<RefundItemDetailResponse> refundItems
    ) {
        return new CustomerRefundDetailResponse(
                RefundOrderInfoResponse.from(
                        projection.orderNumber(),
                        projection.orderAmount(),
                        orderItemRows
                ),
                RefundInfo.from(projection),
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
        private static RefundInfo from(CustomerRefundDetailProjection projection) {
            return new RefundInfo(
                    projection.refundId(), projection.status(), projection.initiator(),
                    projection.reasonType(), projection.reason(), projection.requestedAmount(),
                    projection.requestedAt(), projection.reviewedAt(), projection.rejectionReason()
            );
        }
    }
}
