package com.example.onionstore.domain.order.service;

import com.example.onionstore.domain.order.dto.GetOrderItemResponse;
import com.example.onionstore.domain.order.dto.GetOrderResponse;
import com.example.onionstore.domain.order.entity.Order;
import com.example.onionstore.domain.order.entity.OrderItem;
import com.example.onionstore.domain.order.repository.OrderItemRepository;
import com.example.onionstore.domain.order.repository.OrderRepository;
import com.example.onionstore.domain.payment.dto.GetPaymentInfoResponse;
import com.example.onionstore.domain.payment.service.PaymentService;
import com.example.onionstore.global.exception.BusinessException;
import com.example.onionstore.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final PaymentService paymentService;

    // 개인회원 주문 상세 조회
    // TODO: 인증 구현 완료 후 현재 로그인 사용자의 주문인지 검증
    public GetOrderResponse getOne(Long orderId) {
        Order order = orderRepository.findById(orderId).orElseThrow(
                () -> new BusinessException(ErrorCode.ORDER_NOT_FOUND)
        );

        GetPaymentInfoResponse paymentInfo = paymentService.getPaymentByOrderId(orderId);

        List<OrderItem> orderItems = orderItemRepository.findAllByOrderId(orderId);

        List<GetOrderItemResponse> getOrderItems = orderItems.stream()
                .map(GetOrderItemResponse::from)
                .toList();

        return GetOrderResponse.from(order, paymentInfo, getOrderItems);
    }
}
