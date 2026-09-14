package com.example.onionstore.domain.order.facade;

import com.example.onionstore.domain.cart.entity.Cart;
import com.example.onionstore.domain.cart.entity.CartItem;
import com.example.onionstore.domain.cart.service.CartService;
import com.example.onionstore.domain.category.entity.Category;
import com.example.onionstore.domain.order.dto.CancelOrderResponse;
import com.example.onionstore.domain.order.dto.OrderCreateRequest;
import com.example.onionstore.domain.order.dto.OrderCreateResponse;
import com.example.onionstore.domain.order.entity.Order;
import com.example.onionstore.domain.order.entity.OrderItem;
import com.example.onionstore.domain.order.entity.OrderStatus;
import com.example.onionstore.domain.order.repository.OrderItemRepository;
import com.example.onionstore.domain.order.service.OrderService;
import com.example.onionstore.domain.payment.dto.CreatePaymentResponse;
import com.example.onionstore.domain.payment.dto.GetPaymentInfoResponse;
import com.example.onionstore.domain.payment.dto.PaymentStateChangeResponse;
import com.example.onionstore.domain.payment.entity.Payment;
import com.example.onionstore.domain.payment.service.PaymentService;
import com.example.onionstore.domain.product.entity.Product;
import com.example.onionstore.domain.product.service.ProductService;
import com.example.onionstore.domain.user.entity.Role;
import com.example.onionstore.domain.user.entity.User;
import com.example.onionstore.domain.user.service.UserService;
import com.example.onionstore.global.exception.BusinessException;
import com.example.onionstore.global.exception.ErrorCode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderFacadeTest {

    @Mock UserService userService;
    @Mock OrderService orderService;
    @Mock CartService cartService;
    @Mock ProductService productService;
    @Mock OrderItemRepository orderItemRepository;
    @Mock PaymentService paymentService;
    @Mock CartItem cartItem;

    @InjectMocks OrderFacade orderFacade;

    @Test
    void 정상_주문생성() {
        // given
        User user = new User("test@test.com", "1234", "테스트", "010-0000-0000", Role.CUSTOMER);
        Long userId = 1L;
        when(userService.findUser(userId)).thenReturn(user);

        Cart cart = new Cart(user);
        Category category = new Category("양파");
        Product product = Product.create(category, "양파1kg", "까도까도 맛있는 양파", 1000, 5);

        when(cartItem.getId()).thenReturn(1L);
        int quantity = 1;
        when(cartItem.getQuantity()).thenReturn(quantity);
        when(cartItem.getProduct()).thenReturn(product);
        List<Long> cartItemIds = List.of(cartItem.getId());
        when(cartService.findCartEntitiesByIds(userId, cartItemIds)).thenReturn(List.of(cartItem));

        Order order = new Order(user, 1000);
        when(orderService.createOrder(user, order.getTotalPrice())).thenReturn(order);
        doNothing().when(productService).decreaseStock(product.getId(), quantity);
        OrderItem orderItem = new OrderItem(order, product, product.getName(), product.getPrice(), quantity);
        when(orderItemRepository.saveAll(any(List.class))).thenReturn(List.of(orderItem));

        Payment payment = new Payment(order, order.getTotalPrice());
        CreatePaymentResponse response = new CreatePaymentResponse(payment.getId(), payment.getPortonePaymentId(), payment.getAmount(), payment.getStatus());
        when(paymentService.createPayment(order, order.getTotalPrice())).thenReturn(response);

        doNothing().when(cartService).clearCartItems(cartItemIds, userId);

        // when
        OrderCreateRequest request = new OrderCreateRequest(cartItemIds);
        OrderCreateResponse result = orderFacade.createOrder(userId, request);

        // then
        assertEquals(order.getTotalPrice(), result.totalPrice());
        assertEquals(order.getStatus(), result.status());
        assertEquals(payment.getPortonePaymentId(), result.portonePaymentId());
        assertEquals(1, result.items().size());
        var resultItem = result.items().get(0);
        assertEquals(product.getName(), resultItem.productName());
        assertEquals(product.getPrice(), resultItem.productPrice());
        assertEquals(quantity, resultItem.quantity());
    }

    @Test
    void 정상_주문취소() {
        // given
        Long userId = 1L;
        Long orderId = 1L;
        User user = new User("test@test.com", "1234", "테스트", "010-0000-0000", Role.CUSTOMER);
        ReflectionTestUtils.setField(user, "id", userId);
        Order order = new Order(user, 1000);
        ReflectionTestUtils.setField(order, "id", orderId);

        when(orderService.findById(orderId)).thenReturn(order);
        Payment payment = new Payment(order, 1000);
        when(paymentService.findByOrderId(orderId)).thenReturn(payment);
        Category category = new Category("양파");
        Product product = Product.create(category, "양파1kg", "까도까도 맛있는 양파", 1000, 5);
        OrderItem orderItem = new OrderItem(order, product, product.getName(), product.getPrice(), 1);
        when(orderService.findOrderItemsByOrderId(orderId)).thenReturn(List.of(orderItem));
        doNothing().when(productService).restoreStock(product.getId(), orderItem.getQuantity());
        PaymentStateChangeResponse paymentResponse = new PaymentStateChangeResponse(true, GetPaymentInfoResponse.from(payment));
        when(paymentService.applyCancellation(orderId)).thenReturn(paymentResponse);

        // when
        CancelOrderResponse result = orderFacade.cancelOrder(userId, orderId);

        // then
        assertEquals(orderId, result.orderId());
        assertEquals(OrderStatus.CANCELLED, result.status());
        assertNotNull(result.cancelledAt());
        verify(paymentService).applyCancellation(orderId);
        verify(productService).restoreStock(product.getId(), orderItem.getQuantity());
    }

    @Test
    void 이미_취소된_주문은_다시_취소할_수_없다() {
        // given
        Long userId = 1L;
        Long orderId = 1L;

        User user = new User("test@test.com", "1234", "테스트", "010-0000-0000", Role.CUSTOMER);
        ReflectionTestUtils.setField(user, "id", userId);

        Order order = new Order(user, 1000);
        ReflectionTestUtils.setField(order, "id", orderId);

        // 주문을 이미 취소된 상태로 만든다.
        ReflectionTestUtils.setField(order, "status", OrderStatus.CANCELLED);
        when(orderService.findById(orderId)).thenReturn(order);

        // when
        BusinessException exception = assertThrows(BusinessException.class, () -> orderFacade.cancelOrder(userId, orderId));

        // then
        assertEquals(ErrorCode.ORDER_ALREADY_CANCELED, exception.getErrorCode());
        verify(orderService).findById(orderId);
        verify(paymentService).findByOrderId(orderId);
        verify(paymentService, never()).applyCancellation(orderId);
        verify(productService, never()).restoreStock(anyLong(), anyInt());
    }

    @Test
    void 결제_완료된_주문은_취소할_수_없다() {
        // given
        Long userId = 1L;
        Long orderId = 1L;

        User user = new User("test@test.com", "1234", "테스트", "010-0000-0000", Role.CUSTOMER);
        ReflectionTestUtils.setField(user, "id", userId);

        Order order = new Order(user, 1000);
        ReflectionTestUtils.setField(order, "id", orderId);

        Payment payment = new Payment(order, 1000);

        // 결제를 성공 상태로 변경
        payment.markAsSuccess();

        when(orderService.findById(orderId)).thenReturn(order);
        when(paymentService.findByOrderId(orderId)).thenReturn(payment);

        // when
        BusinessException exception = assertThrows(BusinessException.class, () -> orderFacade.cancelOrder(userId, orderId));

        // then
        assertEquals(ErrorCode.CANNOT_CANCEL_ORDER, exception.getErrorCode());
        verify(orderService).findById(orderId);
        verify(paymentService).findByOrderId(orderId);
        verify(paymentService, never()).applyCancellation(orderId);
        verify(orderService, never()).findOrderItemsByOrderId(orderId);
        verify(productService, never()).restoreStock(anyLong(), anyInt());
    }
}
