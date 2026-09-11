package com.example.onionstore.domain.payment.service;

import com.example.onionstore.domain.order.service.OrderService;
import com.example.onionstore.domain.order.entity.OrderItem;
import com.example.onionstore.domain.order.repository.OrderItemRepository;
import com.example.onionstore.domain.payment.dto.PaymentConfirmResponse;
import com.example.onionstore.domain.payment.dto.PaymentConfirmationInfo;
import com.example.onionstore.domain.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PaymentCommandService {

    private final PaymentService paymentService;
    private final OrderService orderService;
    private final OrderItemRepository orderItemRepository;
    private final ProductService productService;

    // 결제 성공과 주문 유료 상태를 함께 확정한다.
    @Transactional
    public PaymentConfirmResponse completePaymentSuccess(PaymentConfirmationInfo info) {
        paymentService.applySuccess(info.orderId());
        orderService.markAsPaid(info.orderId());
        return PaymentConfirmResponse.success(info.orderId(), info.portonePaymentId());
    }

    // 검증 실패로 취소된 주문의 결제 상태와 재고를 함께 되돌린다.
    @Transactional
    public void cancelPaymentForAmountMismatch(Long orderId) {
        boolean orderCancelled = orderService.cancelOrder(orderId);
        paymentService.applyCancellation(orderId);

        if (orderCancelled) {
            for (OrderItem orderItem : orderItemRepository.findAllByOrderId(orderId)) {
                productService.restoreStock(orderItem.getProduct().getId(), orderItem.getQuantity());
            }
        }
    }
}
