package com.example.onionstore.domain.order.service;

import com.example.onionstore.domain.order.entity.Order;
import com.example.onionstore.domain.order.repository.OrderItemRepository;
import com.example.onionstore.domain.order.repository.OrderRepository;
import com.example.onionstore.domain.payment.repository.PaymentRepository;
import com.example.onionstore.domain.user.entity.Role;
import com.example.onionstore.domain.user.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @InjectMocks
    private OrderService orderService;

    @Test
    void 정상적인_주문_상세조회() {
        // given
        User user = new User("test@test.com", "1234", "테스트", "010-0000-0000", Role.CUSTOMER);
        Order order = new Order("1", user, 5000);

        dddd


        // when

        // then

    }

}