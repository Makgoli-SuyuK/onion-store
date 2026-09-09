package com.example.onionstore.domain.order.service;

import com.example.onionstore.domain.category.entity.Category;
import com.example.onionstore.domain.order.dto.GetOrderItemResponse;
import com.example.onionstore.domain.order.dto.GetOrderResponse;
import com.example.onionstore.domain.order.entity.Order;
import com.example.onionstore.domain.order.entity.OrderItem;
import com.example.onionstore.domain.order.repository.OrderItemRepository;
import com.example.onionstore.domain.order.repository.OrderRepository;
import com.example.onionstore.domain.payment.dto.GetPaymentInfoResponse;
import com.example.onionstore.domain.payment.entity.Payment;
import com.example.onionstore.domain.payment.service.PaymentService;
import com.example.onionstore.domain.product.entity.Product;
import com.example.onionstore.domain.user.entity.Role;
import com.example.onionstore.domain.user.entity.User;
import com.example.onionstore.global.exception.BusinessException;
import com.example.onionstore.global.exception.ErrorCode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private PaymentService paymentService;

    @InjectMocks
    private OrderService orderService;

    @Test
    void 정상적인_주문_상세조회() {
        // given
        User user = new User("test@test.com", "1234", "테스트", "010-0000-0000", Role.CUSTOMER);
        Order order = new Order("1", user, 5000);

        Long orderId = 1L;
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        Payment payment = new Payment(order, 5000);
        GetPaymentInfoResponse paymentInfo = GetPaymentInfoResponse.from(payment);
        when(paymentService.getPaymentByOrderId(orderId)).thenReturn(paymentInfo);

        Category category = new Category("양파");
        Product product = new Product(category, "양파즙", "역대급 양파즙", 5000, 10);
        OrderItem orderItem = new OrderItem(order, product, product.getName(), product.getPrice(), 1);
        when(orderItemRepository.findAllByOrderId(orderId)).thenReturn(List.of(orderItem));

        // when
        GetOrderResponse result = orderService.getOne(orderId);

        // then
        assertEquals(order.getOrderNumber(), result.orderNumber());
        assertEquals(5000, result.totalPrice());
        assertEquals(paymentInfo.status(), result.paymentStatus());
        assertEquals(1, result.orderItems().size());

        GetOrderItemResponse resultItem = result.orderItems().get(0);
        assertEquals(orderItem.getProduct().getName(), resultItem.productName());
    }

    @Test
    void 존재하지_않는_주문조회() {
        // given
        when(orderRepository.findById(1L)).thenReturn(Optional.empty());

        // when & then
        BusinessException exception = assertThrows(BusinessException.class, () -> orderService.getOne(1L));
        assertEquals(ErrorCode.ORDER_NOT_FOUND, exception.getErrorCode());
    }

}