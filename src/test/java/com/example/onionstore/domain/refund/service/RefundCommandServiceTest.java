package com.example.onionstore.domain.refund.service;

import com.example.onionstore.domain.category.entity.Category;
import com.example.onionstore.domain.order.entity.Order;
import com.example.onionstore.domain.order.entity.OrderItem;
import com.example.onionstore.domain.order.service.OrderService;
import com.example.onionstore.domain.payment.entity.Payment;
import com.example.onionstore.domain.payment.service.PaymentService;
import com.example.onionstore.domain.product.entity.Product;
import com.example.onionstore.domain.product.service.ProductService;
import com.example.onionstore.domain.refund.dto.common.RefundCancellationInfo;
import com.example.onionstore.domain.refund.dto.customer.request.CustomerRefundRequest;
import com.example.onionstore.domain.refund.dto.customer.request.RefundItemRequest;
import com.example.onionstore.domain.refund.dto.customer.response.CustomerRefundSummaryResponse;
import com.example.onionstore.domain.refund.entity.Refund;
import com.example.onionstore.domain.refund.entity.RefundInitiator;
import com.example.onionstore.domain.refund.entity.RefundReasonType;
import com.example.onionstore.domain.refund.entity.RefundStatus;
import com.example.onionstore.domain.user.entity.Role;
import com.example.onionstore.domain.user.entity.User;
import com.example.onionstore.domain.user.service.UserService;
import com.example.onionstore.global.exception.BusinessException;
import com.example.onionstore.global.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class RefundCommandServiceTest {

    @Mock
    private OrderService orderService;

    @Mock
    private PaymentService paymentService;

    @Mock
    private RefundService refundService;

    @Mock
    private ProductService productService;

    @Mock
    private UserService userService;

    @InjectMocks
    private RefundCommandService refundCommandService;

    private User owner;
    private Order order;
    private Payment payment;
    private OrderItem orderItem;

    @BeforeEach
    void setUp() {
        owner = new User("customer@onion.store", "password", "고객", "01012345678", Role.CUSTOMER);
        ReflectionTestUtils.setField(owner, "id", 1L);

        order = new Order(owner, 2_000L);
        ReflectionTestUtils.setField(order, "id", 10L);
        order.markAsPaid();

        payment = new Payment(order, 2_000L);
        payment.markAsSuccess();

        Product product = Product.create(new Category("양파"), "양파즙", "설명", 1_000L, 10);
        orderItem = new OrderItem(order, product, product.getName(), product.getPrice(), 2);
        ReflectionTestUtils.setField(orderItem, "id", 100L);
    }

    @Test
    void 본인_주문의_환불요청을_저장한다() {
        Refund refund = new Refund(
                payment,
                2_000L,
                RefundStatus.PENDING_APPROVAL,
                RefundInitiator.CUSTOMER,
                RefundReasonType.CUSTOMER_REQUEST,
                "상품 상태가 좋지 않습니다."
        );
        ReflectionTestUtils.setField(refund, "id", 50L);
        givenRequestPrerequisites(Map.of());
        given(refundService.createCustomerPending(eq(payment), eq(2_000L),
                eq("상품 상태가 좋지 않습니다."), anyMap())).willReturn(refund);

        CustomerRefundSummaryResponse response = refundCommandService.requestCustomerRefund(
                owner.getId(),
                order.getId(),
                request(2)
        );

        ArgumentCaptor<Map<OrderItem, Integer>> quantitiesCaptor = ArgumentCaptor.forClass(Map.class);
        verify(refundService).createCustomerPending(
                eq(payment),
                eq(2_000L),
                eq("상품 상태가 좋지 않습니다."),
                quantitiesCaptor.capture()
        );
        assertThat(quantitiesCaptor.getValue()).containsEntry(orderItem, 2);
        assertThat(response.refundId()).isEqualTo(50L);
        assertThat(response.requestedAmount()).isEqualTo(2_000L);
    }

    @Test
    void 다른_고객의_주문에는_환불을_요청할_수_없다() {
        given(orderService.findOrderForUpdate(order.getId())).willReturn(order);

        assertThatThrownBy(() -> refundCommandService.requestCustomerRefund(
                2L,
                order.getId(),
                request(1)
        )).isInstanceOfSatisfying(BusinessException.class,
                exception -> assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.ORDER_ACCESS_DENIED));

        verifyNoInteractions(paymentService, refundService);
    }

    @Test
    void 같은_주문상품을_중복으로_요청하면_거절한다() {
        givenRequestPrerequisites(Map.of());
        CustomerRefundRequest request = new CustomerRefundRequest(
                "상품 상태가 좋지 않습니다.",
                List.of(new RefundItemRequest(orderItem.getId(), 1), new RefundItemRequest(orderItem.getId(), 1))
        );

        assertThatThrownBy(() -> refundCommandService.requestCustomerRefund(owner.getId(), order.getId(), request))
                .isInstanceOfSatisfying(BusinessException.class,
                        exception -> assertThat(exception.getErrorCode())
                                .isEqualTo(ErrorCode.DUPLICATE_REFUND_ITEM_REQUEST));

        verify(refundService, never()).createCustomerPending(eq(payment), eq(2_000L),
                eq("상품 상태가 좋지 않습니다."), anyMap());
    }

    @Test
    void 이미_완료된_환불수량을_포함해_주문수량을_넘기면_거절한다() {
        givenRequestPrerequisites(Map.of(orderItem.getId(), 1));

        assertThatThrownBy(() -> refundCommandService.requestCustomerRefund(
                owner.getId(),
                order.getId(),
                request(2)
        )).isInstanceOfSatisfying(BusinessException.class,
                exception -> assertThat(exception.getErrorCode())
                        .isEqualTo(ErrorCode.EXCEEDS_REFUNDABLE_QUANTITY));

        verify(refundService, never()).createCustomerPending(eq(payment), eq(2_000L),
                eq("상품 상태가 좋지 않습니다."), anyMap());
    }

    @Test
    void 부분환불이_완료되면_요청상품_재고를_복구하고_결제를_부분취소로_변경한다() {
        Refund refund = requestedRefund(1);
        givenCompletionPrerequisites(refund, Map.of(orderItem.getId(), 1));

        var response = refundCommandService.completeRefund(refund.getId());

        assertThat(response.status()).isEqualTo(RefundStatus.COMPLETED);
        verify(productService).restoreStock(orderItem.getProduct().getId(), 1);
        verify(paymentService).applyPartialCancellation(order.getId());
        verify(orderService, never()).completeRefundCancellation(order.getId());
        verify(paymentService, never()).applyCancellation(order.getId());
    }

    @Test
    void 전액환불이_완료되면_주문과_결제를_취소한다() {
        Refund refund = requestedRefund(2);
        givenCompletionPrerequisites(refund, Map.of(orderItem.getId(), 2));

        var response = refundCommandService.completeRefund(refund.getId());

        assertThat(response.status()).isEqualTo(RefundStatus.COMPLETED);
        verify(productService).restoreStock(orderItem.getProduct().getId(), 2);
        verify(orderService).completeRefundCancellation(order.getId());
        verify(paymentService).applyCancellation(order.getId());
        verify(paymentService, never()).applyPartialCancellation(order.getId());
    }

    private void givenRequestPrerequisites(Map<Long, Integer> completedQuantities) {
        given(orderService.findOrderForUpdate(order.getId())).willReturn(order);
        given(paymentService.findPaymentForUpdate(order.getId())).willReturn(payment);
        given(orderService.findOrderItemsByOrderId(order.getId())).willReturn(List.of(orderItem));
        given(refundService.getCompletedQuantityByOrderItemId(order.getId())).willReturn(completedQuantities);
    }

    private Refund requestedRefund(int quantity) {
        Refund refund = new Refund(
                payment,
                orderItem.getProductPrice() * quantity,
                RefundStatus.REQUESTED,
                RefundInitiator.CUSTOMER,
                RefundReasonType.CUSTOMER_REQUEST,
                "상품 상태가 좋지 않습니다."
        );
        refund.addItem(orderItem, quantity);
        ReflectionTestUtils.setField(refund, "id", 50L);
        return refund;
    }

    private void givenCompletionPrerequisites(
            Refund refund,
            Map<Long, Integer> completedQuantities
    ) {
        given(refundService.getRefundCancellationInfo(refund.getId())).willReturn(
                new RefundCancellationInfo(
                        refund.getId(),
                        order.getId(),
                        "pay_123",
                        refund.getAmount(),
                        refund.getReason()
                )
        );
        given(orderService.findOrderForUpdate(order.getId())).willReturn(order);
        given(paymentService.findPaymentForUpdate(order.getId())).willReturn(payment);
        given(refundService.findRefundForUpdate(refund.getId())).willReturn(refund);
        doAnswer(invocation -> refund.complete())
                .when(refundService).completeRefund(refund);
        given(refundService.getCompletedQuantityByOrderItemId(order.getId()))
                .willReturn(completedQuantities);
        given(orderService.findOrderItemsByOrderId(order.getId())).willReturn(List.of(orderItem));
    }

    private CustomerRefundRequest request(int quantity) {
        return new CustomerRefundRequest(
                "상품 상태가 좋지 않습니다.",
                List.of(new RefundItemRequest(orderItem.getId(), quantity))
        );
    }
}
