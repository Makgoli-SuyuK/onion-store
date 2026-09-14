package com.example.onionstore.domain.refund.dto.admin.response;

import com.example.onionstore.domain.refund.dto.common.RefundItemDetailResponse;
import com.example.onionstore.domain.refund.dto.common.RefundOrderInfoResponse;
import com.example.onionstore.domain.refund.entity.RefundInitiator;
import com.example.onionstore.domain.refund.entity.RefundReasonType;
import com.example.onionstore.domain.refund.entity.RefundStatus;
import com.example.onionstore.domain.refund.repository.dto.AdminRefundDetailProjection;
import com.example.onionstore.domain.refund.repository.dto.OrderItemSummaryRow;

import java.time.LocalDateTime;
import java.util.List;

// 관리자 환불 상세 조회 응답
public record AdminRefundDetailResponse(
        RefundOrderInfoResponse order,
        CustomerInfo customer,
        RefundInfo refund,
        List<RefundItemDetailResponse> refundItems
) {
    public static AdminRefundDetailResponse of(
            AdminRefundDetailProjection projection,
            List<OrderItemSummaryRow> orderItemRows,
            List<RefundItemDetailResponse> refundItems
    ) {
        return new AdminRefundDetailResponse(
                RefundOrderInfoResponse.from(
                        projection.orderNumber(),
                        projection.orderAmount(),
                        orderItemRows
                ),
                CustomerInfo.from(projection),
                RefundInfo.from(projection),
                refundItems
        );
    }

    public record CustomerInfo(
            String name,
            String phoneNumber,
            String email
    ) {
        private static CustomerInfo from(AdminRefundDetailProjection projection) {
            return new CustomerInfo(
                    projection.customerName(),
                    projection.phoneNumber(),
                    projection.email()
            );
        }
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
            String rejectionReason,
            String reviewerName,
            String portoneCancellationId
    ) {
        private static RefundInfo from(AdminRefundDetailProjection projection) {
            return new RefundInfo(
                    projection.refundId(),
                    projection.status(),
                    projection.initiator(),
                    projection.reasonType(),
                    projection.reason(),
                    projection.requestedAmount(),
                    projection.requestedAt(),
                    projection.reviewedAt(),
                    projection.rejectionReason(),
                    projection.reviewerName(),
                    projection.portoneCancellationId()
            );
        }
    }
}
