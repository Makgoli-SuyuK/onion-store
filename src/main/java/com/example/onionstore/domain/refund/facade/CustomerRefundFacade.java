package com.example.onionstore.domain.refund.facade;

import com.example.onionstore.domain.refund.dto.common.RefundItemDetailResponse;
import com.example.onionstore.domain.refund.dto.customer.request.CustomerRefundRequest;
import com.example.onionstore.domain.refund.dto.customer.response.CustomerRefundDetailResponse;
import com.example.onionstore.domain.refund.dto.customer.response.CustomerRefundSummaryResponse;
import com.example.onionstore.domain.refund.repository.dto.CustomerRefundDetailProjection;
import com.example.onionstore.domain.refund.repository.dto.OrderItemSummaryRow;
import com.example.onionstore.domain.refund.service.RefundCommandService;
import com.example.onionstore.domain.refund.service.RefundService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
public class CustomerRefundFacade {

    private final RefundCommandService refundCommandService;
    private final RefundService refundService;

    // 고객 환불 요청 생성 유스케이스를 CommandService에 위임한다.
    public CustomerRefundSummaryResponse requestCustomerRefund(
            Long userId,
            Long orderId,
            CustomerRefundRequest request
    ) {
        return refundCommandService.requestCustomerRefund(userId, orderId, request);
    }

    // 로그인한 고객의 환불 이력을 최신순으로 조회한다.
    @Transactional(readOnly = true)
    public List<CustomerRefundSummaryResponse> getMyRefunds(Long userId) {
        return refundService.getCustomerRefunds(userId);
    }

    // 로그인한 고객에게 소유된 환불 상세만 조합해 반환한다.
    @Transactional(readOnly = true)
    public CustomerRefundDetailResponse getMyRefundDetail(Long userId, Long refundId) {
        CustomerRefundDetailProjection projection = refundService
                .getCustomerRefundDetailProjection(userId, refundId);
        List<OrderItemSummaryRow> orderItems = refundService
                .getOrderItemSummaryRows(projection.orderId());
        List<RefundItemDetailResponse> refundItems = refundService
                .getRefundItemDetails(refundId);

        return CustomerRefundDetailResponse.of(projection, orderItems, refundItems);
    }
}