package com.example.onionstore.domain.refund.dto.response;

import com.example.onionstore.domain.refund.entity.RefundInitiator;
import com.example.onionstore.domain.refund.entity.RefundReasonType;
import com.example.onionstore.domain.refund.entity.RefundStatus;
import com.example.onionstore.domain.refund.repository.dto.AdminRefundDetailHeader;
import com.example.onionstore.domain.refund.repository.dto.OrderItemSummaryRow;

import java.time.LocalDateTime;
import java.util.List;

public record AdminRefundDetailResponse(
        RefundOrderSummaryResponse order,
        CustomerInfo customer,
        RefundInfo refund,
        List<RefundItemDetailResponse> refundItems
) {
    public static AdminRefundDetailResponse of(
            AdminRefundDetailHeader header,
            List<OrderItemSummaryRow> orderItemRows,
            List<RefundItemDetailResponse> refundItems
    ) {
        return new AdminRefundDetailResponse(
                RefundOrderSummaryResponse.from(
                        header.orderNumber(),
                        header.orderAmount(),
                        orderItemRows
                ),
                CustomerInfo.from(header),
                RefundInfo.from(header),
                refundItems
        );
    }

    public record CustomerInfo(
            String name,
            String phoneNumber,
            String email
    ) {
        private static CustomerInfo from(AdminRefundDetailHeader header) {
            return new CustomerInfo(
                    header.customerName(),
                    header.phoneNumber(),
                    header.email()
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
        private static RefundInfo from(AdminRefundDetailHeader header) {
            return new RefundInfo(
                    header.refundId(),
                    header.status(),
                    header.initiator(),
                    header.reasonType(),
                    header.reason(),
                    header.requestedAmount(),
                    header.requestedAt(),
                    header.reviewedAt(),
                    header.rejectionReason(),
                    header.reviewerName(),
                    header.portoneCancellationId()
            );
        }
    }
}
