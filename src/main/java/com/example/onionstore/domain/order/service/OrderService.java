package com.example.onionstore.domain.order.service;

import com.example.onionstore.domain.order.dto.*;
import com.example.onionstore.domain.order.entity.Order;
import com.example.onionstore.domain.order.entity.OrderItem;
import com.example.onionstore.domain.order.repository.OrderItemRepository;
import com.example.onionstore.domain.order.repository.OrderRepository;
import com.example.onionstore.domain.payment.dto.GetPaymentInfoResponse;
import com.example.onionstore.domain.payment.service.PaymentService;
import com.example.onionstore.global.exception.BusinessException;
import com.example.onionstore.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final PaymentService paymentService;

    // 개인회원 주문 상세 조회
    public GetOrderResponse getOne(Long userId, Long orderId) {

        Order order = orderRepository.findById(orderId).orElseThrow(
                () -> new BusinessException(ErrorCode.ORDER_NOT_FOUND)
        );

        if (!userId.equals(order.getUser().getId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN_ROLE);
        }

        GetPaymentInfoResponse paymentInfo = paymentService.getPaymentByOrderId(orderId);

        List<OrderItem> orderItems = orderItemRepository.findAllByOrderId(orderId);

        List<GetOrderItemResponse> getOrderItems = orderItems.stream()
                .map(GetOrderItemResponse::from)
                .toList();

        return GetOrderResponse.from(order, paymentInfo, getOrderItems);
    }

    // 개인회원 주문 전체 조회
    public Page<GetOrderListResponse> getAll(Long userId, Pageable pageable, OrderSearchRequest request) {
        Page<Order> orders = orderRepository.findAllByUser_IdWithKeyword(userId, pageable, request);

        // 1. 현재 페이지의 주문 ID를 한 번에 추출
        List<Long> orderIds = orders.getContent().stream()
                .map(Order::getId)
                .toList();

        // 2. 주문 상품을 한 번에 조회
        List<OrderItem> orderItems = orderItemRepository.findAllByOrder_IdIn(orderIds);
        // 3. 결제 정보를 한 번에 조회
        List<GetPaymentInfoResponse> paymentInfos = paymentService.getPaymentsByOrderIds(orderIds);

        // 4. 주문별 상품을 묶어두기
        Map<Long, List<GetOrderListItemResponse>> orderItemsMap =
                orderItems.stream()
                        .collect(Collectors.groupingBy(
                                orderItem -> orderItem.getOrder().getId(),
                                Collectors.mapping(
                                        GetOrderListItemResponse::from,
                                        Collectors.toList()
                                )
                        ));

        // 5. 주문별 결제정보를 Map으로 만들어두기
        Map<Long, GetPaymentInfoResponse> paymentInfoMap =
                paymentInfos.stream()
                        .collect(Collectors.toMap(
                                info -> info.orderId(),
                                Function.identity()
                        ));

        return orders.map(order -> {
            List<GetOrderListItemResponse> items =
                    orderItemsMap.getOrDefault(order.getId(), List.of());

            GetPaymentInfoResponse info =
                    paymentInfoMap.get(order.getId());

            return GetOrderListResponse.from(order, items, info);
        });
    }
  
    @Transactional
    public boolean markAsPaid(Long orderId) {
        Order order = findOrderForUpdate(orderId);
        return order.markAsPaid();
    }

    @Transactional
    public boolean cancelOrder(Long orderId) {
        Order order = findOrderForUpdate(orderId);
        return order.cancel();
    }

    private Order findOrderForUpdate(Long orderId) {
        return orderRepository.findByIdForUpdate(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));
    }
}
