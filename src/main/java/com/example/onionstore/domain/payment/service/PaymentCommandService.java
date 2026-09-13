package com.example.onionstore.domain.payment.service;

import com.example.onionstore.domain.order.service.OrderService;
import com.example.onionstore.domain.order.entity.OrderItem;
import com.example.onionstore.domain.payment.dto.PaymentConfirmResponse;
import com.example.onionstore.domain.payment.dto.PaymentConfirmationInfo;
import com.example.onionstore.domain.payment.dto.PaymentStateChangeResponse;
import com.example.onionstore.domain.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PaymentCommandService {

    private final PaymentService paymentService;
    private final OrderService orderService;
    private final ProductService productService;

    // 결제 성공과 주문 결제 완료 상태를 함께 확정한다.    @Transactional
    public PaymentConfirmResponse completePaymentSuccess(PaymentConfirmationInfo info) {
        orderService.findOrderForUpdate(info.orderId());
        paymentService.applySuccess(info.orderId());
        orderService.markAsPaid(info.orderId());
        return PaymentConfirmResponse.success(info.orderId(), info.portonePaymentId());
    }

    // 실패 확정된 결제만 주문 취소와 재고 복구까지 처리한다.
    @Transactional
    public void completePaymentFailure(Long orderId) {
        orderService.findOrderForUpdate(orderId);
        PaymentStateChangeResponse result = paymentService.applyFailure(orderId);

        if (!result.changed()) {
            return;
        }

        if (orderService.cancelOrder(orderId)) {restoreOrderItems(orderId);
        }
    }

    // 검증 실패로 취소된 주문의 결제 상태와 재고를 함께 되돌린다.
    @Transactional
    public void cancelPaymentForAmountMismatch(Long orderId) {
        boolean orderCancelled = orderService.cancelOrder(orderId);
        paymentService.applyCancellation(orderId);

        if (orderCancelled) {
            restoreOrderItems(orderId);
        }
    }

    // 주문 상품의 수량만큼 재고를 복구한다.
    private void restoreOrderItems(Long orderId) {
        for (OrderItem orderItem : orderService.findOrderItemsByOrderId(orderId)) {
            productService.restoreStock(
                    orderItem.getProduct().getId(),
                    orderItem.getQuantity()
            );
        }
    }
}
