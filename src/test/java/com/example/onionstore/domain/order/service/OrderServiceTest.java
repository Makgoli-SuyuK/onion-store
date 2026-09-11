package com.example.onionstore.domain.order.service;

import com.example.onionstore.domain.category.entity.Category;
import com.example.onionstore.domain.order.dto.*;
import com.example.onionstore.domain.order.entity.Order;
import com.example.onionstore.domain.order.entity.OrderItem;
import com.example.onionstore.domain.order.repository.OrderItemRepository;
import com.example.onionstore.domain.order.repository.OrderRepository;
import com.example.onionstore.domain.payment.dto.GetPaymentInfoResponse;
import com.example.onionstore.domain.payment.entity.Payment;
import com.example.onionstore.domain.payment.entity.PaymentStatus;
import com.example.onionstore.domain.payment.service.PaymentService;
import com.example.onionstore.domain.product.entity.Product;
import com.example.onionstore.domain.user.entity.User;
import com.example.onionstore.global.exception.BusinessException;
import com.example.onionstore.global.exception.ErrorCode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
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

    @Mock
    private User user;

    @Mock
    private User user2;

    @Mock
    private Order order;

    @Mock
    private Order order2;

    @InjectMocks
    private OrderService orderService;

    @Test
    void 정상적인_주문_상세조회() {
        // given
        when(user.getId()).thenReturn(1L);

        Order order = new Order(user, 5000);
        Long orderId = 1L;

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        Payment payment = new Payment(order, 5000);
        GetPaymentInfoResponse paymentInfo = GetPaymentInfoResponse.from(payment);
        when(paymentService.getPaymentByOrderId(orderId)).thenReturn(paymentInfo);

        Category category = new Category("양파");
        Product product = Product.create(category, "양파즙", "역대급 양파즙", 5000, 10);
        OrderItem orderItem = new OrderItem(order, product, product.getName(), product.getPrice(), 1);
        when(orderItemRepository.findAllByOrderId(orderId)).thenReturn(List.of(orderItem));

        // when
        GetOrderResponse result = orderService.getOne(1L, orderId);

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
        BusinessException exception = assertThrows(BusinessException.class, () -> orderService.getOne(1L, 1L));
        assertEquals(ErrorCode.ORDER_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    void 다른_사용자의_주문조회() {
        // given
        when(user.getId()).thenReturn(1L);
        when(user2.getId()).thenReturn(2L);

        Order order = new Order(user2, 5000);
        Long orderId = 1L;

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        // when & then
        BusinessException exception = assertThrows(BusinessException.class, () -> orderService.getOne(user.getId(), orderId));
        assertEquals(ErrorCode.FORBIDDEN_ROLE, exception.getErrorCode());
    }

    @Test
    void 내_주문목록_정상조회() {
        // given
        when(user.getId()).thenReturn(1L);
        Pageable pageable = PageRequest.of(0, 10);

        when(order.getId()).thenReturn(1L);
        when(order2.getId()).thenReturn(2L);

        List<Order> orders = List.of(order, order2);
        Page<Order> orderPage = new PageImpl<>(orders, pageable, 2);

        OrderSearchRequest request = new OrderSearchRequest();
        when(orderRepository.findAllByUser_IdWithKeyword(user.getId(), pageable, request)).thenReturn(orderPage);

        Category category = new Category("양파");
        Product product = Product.create(category, "양파즙", "역대급 양파즙", 5000, 10);
        OrderItem orderItem = new OrderItem(order, product, product.getName(), product.getPrice(), 1);
        OrderItem orderItem2 = new OrderItem(order2, product, product.getName(), product.getPrice(), 1);
        when(orderItemRepository.findAllByOrderId(order.getId())).thenReturn(List.of(orderItem));
        when(orderItemRepository.findAllByOrderId(order2.getId())).thenReturn(List.of(orderItem2));

        Payment payment = new Payment(order, 5000);
        GetPaymentInfoResponse paymentInfo = GetPaymentInfoResponse.from(payment);
        when(paymentService.getPaymentByOrderId(order.getId())).thenReturn(paymentInfo);

        Payment payment2 = new Payment(order2, 5000);
        GetPaymentInfoResponse paymentInfo2 = GetPaymentInfoResponse.from(payment2);
        when(paymentService.getPaymentByOrderId(order2.getId())).thenReturn(paymentInfo2);

        // when
        Page<GetOrderListResponse> result = orderService.getAll(user.getId(), pageable, request);
        GetOrderListResponse resultOrder = result.getContent().get(0);
        GetOrderListItemResponse resultItem = resultOrder.items().get(0);

        // then
        assertEquals(2, result.getContent().size());
        assertEquals(order.getOrderNumber(), resultOrder.orderNumber());
        assertEquals(order.getTotalPrice(), resultOrder.totalPrice());
        assertEquals(orderItem.getProductName(), resultItem.productName());
        assertEquals(orderItem.getProductPrice(), resultItem.productPrice());
        assertEquals(orderItem.getQuantity(), resultItem.quantity());
    }

    @Test
    void 주문목록_키워드검색_테스트() {
        when(user.getId()).thenReturn(1L);
        Pageable pageable = PageRequest.of(0, 10);

        when(order.getId()).thenReturn(1L);

        List<Order> orders = List.of(order);
        Page<Order> orderPage = new PageImpl<>(orders, pageable, 1);

        OrderSearchRequest request = new OrderSearchRequest();
        request.setKeyword("양파즙");
        when(orderRepository.findAllByUser_IdWithKeyword(user.getId(), pageable, request)).thenReturn(orderPage);

        Category category = new Category("양파");
        Product product = Product.create(category, "양파즙", "역대급 양파즙", 5000, 10);
        OrderItem orderItem = new OrderItem(order, product, product.getName(), product.getPrice(), 1);
        when(orderItemRepository.findAllByOrderId(order.getId())).thenReturn(List.of(orderItem));

        Payment payment = new Payment(order, 5000);
        GetPaymentInfoResponse paymentInfo = GetPaymentInfoResponse.from(payment);
        when(paymentService.getPaymentByOrderId(order.getId())).thenReturn(paymentInfo);

        // when
        Page<GetOrderListResponse> result = orderService.getAll(user.getId(), pageable, request);
        GetOrderListResponse resultOrder = result.getContent().get(0);
        GetOrderListItemResponse resultItem = resultOrder.items().get(0);

        // then
        assertEquals(1, result.getContent().size());
        assertEquals(orderItem.getProductName(), resultItem.productName());
    }

    @Test
    void Payment가_없는_주문이_있을때_주문전체조회() {
        // given
        when(user.getId()).thenReturn(1L);

        Pageable pageable = PageRequest.of(0, 10);

        when(order.getId()).thenReturn(1L);
        when(order2.getId()).thenReturn(2L);

        List<Order> orders = List.of(order, order2);
        Page<Order> orderPage = new PageImpl<>(orders, pageable, 2);

        OrderSearchRequest request = new OrderSearchRequest();

        when(orderRepository.findAllByUser_IdWithKeyword(user.getId(), pageable, request)).thenReturn(orderPage);

        Category category = new Category("양파");
        Product product = Product.create(category, "양파즙", "역대급 양파즙", 5000, 10);
        OrderItem orderItem = new OrderItem(order, product, product.getName(), product.getPrice(), 1);
        OrderItem orderItem2 = new OrderItem(order2, product, product.getName(), product.getPrice(), 1);

        when(orderItemRepository.findAllByOrder_IdIn(List.of(1L, 2L))).thenReturn(List.of(orderItem, orderItem2));

        GetPaymentInfoResponse paymentInfo = new GetPaymentInfoResponse(order.getId(), LocalDateTime.now(), PaymentStatus.SUCCESS);

        when(paymentService.getPaymentsByOrderIds(List.of(1L, 2L))).thenReturn(List.of(paymentInfo));

        // when
        Page<GetOrderListResponse> result = orderService.getAll(user.getId(), pageable, request);

        // then
        assertEquals(2, result.getContent().size());
        GetOrderListResponse orderResult = result.getContent().get(0);
        GetOrderListResponse order2Result = result.getContent().get(1);
        assertEquals(order.getId(), orderResult.orderId());
        assertEquals(order.getId(), paymentInfo.orderId());
        assertEquals(paymentInfo.paidAt(), orderResult.paidAt());
        assertEquals(order2.getId(), order2Result.orderId());
        assertNull(order2Result.paidAt());
    }
}