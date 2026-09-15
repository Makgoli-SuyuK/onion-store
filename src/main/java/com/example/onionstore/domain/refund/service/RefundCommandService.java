package com.example.onionstore.domain.refund.service;

import com.example.onionstore.domain.order.entity.Order;
import com.example.onionstore.domain.order.entity.OrderItem;
import com.example.onionstore.domain.order.entity.OrderStatus;
import com.example.onionstore.domain.order.service.OrderService;
import com.example.onionstore.domain.payment.entity.Payment;
import com.example.onionstore.domain.payment.entity.PaymentStatus;
import com.example.onionstore.domain.payment.service.PaymentService;
import com.example.onionstore.domain.refund.dto.customer.request.CustomerRefundRequest;
import com.example.onionstore.domain.refund.dto.customer.request.RefundItemRequest;
import com.example.onionstore.domain.refund.dto.customer.response.CustomerRefundSummaryResponse;
import com.example.onionstore.domain.refund.entity.Refund;
import com.example.onionstore.global.exception.BusinessException;
import com.example.onionstore.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RefundCommandService {

    private final OrderService orderService;
    private final PaymentService paymentService;
    private final RefundService refundService;

    // 주문·결제·환불을 잠근 뒤 고객 환불 요청을 승인 대기 상태로 저장한다.
    @Transactional
    public CustomerRefundSummaryResponse requestCustomerRefund(
            Long userId,
            Long orderId,
            CustomerRefundRequest request
    ) {
        Order order = orderService.findOrderForUpdate(orderId);
        validateOwner(order, userId);
        validateRefundableOrder(order);

        Payment payment = paymentService.findPaymentForUpdate(orderId);
        validateRefundablePayment(payment);
        refundService.validateNoActiveRefund(payment.getId());

        List<OrderItem> orderItems = orderService.findOrderItemsByOrderId(orderId);
        Map<Long, Integer> completedQuantity = refundService
                .getCompletedQuantityByOrderItemId(orderId);
        Map<OrderItem, Integer> requestedQuantities = validateAndMapRequestedQuantities(
                orderItems,
                completedQuantity,
                request.items()
        );

        Refund refund = refundService.createCustomerPending(
                payment,
                calculateRequestedAmount(requestedQuantities),
                request.reason(),
                requestedQuantities
        );
        return new CustomerRefundSummaryResponse(
                refund.getId(),
                refund.getStatus(),
                refund.getAmount(),
                refund.getCreatedAt(),
                refund.getReviewedAt(),
                refund.getRejectionReason()
        );
    }

    // 주문 소유자가 아닌 사용자의 환불 요청을 차단한다.
    private void validateOwner(Order order, Long userId) {
        if (!order.getUser().getId().equals(userId)) {
            throw new BusinessException(ErrorCode.ORDER_ACCESS_DENIED);
        }
    }

    // 결제가 완료된 주문만 고객 환불 요청을 허용한다.
    private void validateRefundableOrder(Order order) {
        if (order.getStatus() != OrderStatus.PAID) {
            throw new BusinessException(ErrorCode.REFUND_NOT_ALLOWED);
        }
    }

    // 전체 또는 부분 환불 이후에도 남은 결제 금액이 있는 결제만 허용한다.
    private void validateRefundablePayment(Payment payment) {
        if (payment.getStatus() != PaymentStatus.SUCCESS
                && payment.getStatus() != PaymentStatus.PARTIALLY_CANCELLED) {
            throw new BusinessException(ErrorCode.REFUND_NOT_ALLOWED);
        }
    }

    // 요청 항목의 중복·소속·누적 환불 수량을 확인해 저장할 수량으로 변환한다.
    private Map<OrderItem, Integer> validateAndMapRequestedQuantities(
            List<OrderItem> orderItems,
            Map<Long, Integer> completedQuantityByOrderItemId,
            List<RefundItemRequest> requests
    ) {
        Map<Long, OrderItem> itemsById = orderItems.stream()
                .collect(Collectors.toMap(OrderItem::getId, Function.identity()));
        Map<OrderItem, Integer> requestedQuantities = new LinkedHashMap<>();
        Set<Long> requestedOrderItemIds = new HashSet<>();

        for (RefundItemRequest request : requests) {
            if (!requestedOrderItemIds.add(request.orderItemId())) {
                throw new BusinessException(ErrorCode.DUPLICATE_REFUND_ITEM_REQUEST);
            }

            OrderItem orderItem = Optional.ofNullable(itemsById.get(request.orderItemId()))
                    .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_ITEM_NOT_FOUND));
            int completedQuantity = completedQuantityByOrderItemId
                    .getOrDefault(orderItem.getId(), 0);

            if (completedQuantity + request.quantity() > orderItem.getQuantity()) {
                throw new BusinessException(ErrorCode.EXCEEDS_REFUNDABLE_QUANTITY);
            }
            requestedQuantities.put(orderItem, request.quantity());
        }
        return requestedQuantities;
    }

    // 주문 시점 상품 가격과 요청 수량으로 환불 금액을 계산한다.
    private long calculateRequestedAmount(Map<OrderItem, Integer> requestedQuantities) {
        return requestedQuantities.entrySet().stream()
                .mapToLong(entry -> entry.getKey().getProductPrice() * entry.getValue())
                .sum();
    }


}