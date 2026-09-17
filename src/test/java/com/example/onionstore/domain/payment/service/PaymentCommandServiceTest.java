package com.example.onionstore.domain.payment.service;

import com.example.onionstore.domain.cart.service.CartService;
import com.example.onionstore.domain.order.entity.Order;
import com.example.onionstore.domain.order.entity.OrderItem;
import com.example.onionstore.domain.order.service.OrderService;
import com.example.onionstore.domain.user.entity.User;
import com.example.onionstore.domain.product.entity.Product;
import com.example.onionstore.domain.product.service.ProductService;
import com.example.onionstore.domain.payment.dto.*;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PaymentCommandServiceTest {
    PaymentService payments = mock(PaymentService.class);
    OrderService orders = mock(OrderService.class);
    ProductService products = mock(ProductService.class);
    CartService carts = mock(CartService.class);
    PaymentCommandService service = new PaymentCommandService(payments, orders, products, carts);

    @Test
    void 성공처리는_주문잠금후_결제와_주문을_확정한다() {
        // given
        var info = new PaymentConfirmationInfo(1L, "pay", 1000L);
        Order order = mock(Order.class);
        User user = mock(User.class);
        OrderItem item = mock(OrderItem.class);
        when(orders.findOrderForUpdate(1L)).thenReturn(order);
        when(order.getUser()).thenReturn(user);
        when(user.getId()).thenReturn(10L);
        when(payments.applySuccess(1L)).thenReturn(new PaymentStateChangeResponse(true, null));
        when(orders.markAsPaid(1L)).thenReturn(true);
        when(item.getSourceCartItemId()).thenReturn(100L);
        when(orders.findOrderItemsByOrderId(1L)).thenReturn(List.of(item));

        // when
        var result = service.completePaymentSuccess(info);

        // then
        assertEquals(PaymentConfirmResponse.success(1L, "pay"), result);
        var callOrder = inOrder(orders, payments);
        callOrder.verify(orders).findOrderForUpdate(1L);
        callOrder.verify(payments).applySuccess(1L);
        callOrder.verify(orders).markAsPaid(1L);
        verify(carts).clearCartItems(List.of(100L), 10L);
        verifyNoInteractions(products);
    }

    @Test
    void 중복_결제완료_처리는_장바구니를_다시_비우지_않는다() {
        // given
        var info = new PaymentConfirmationInfo(1L, "pay", 1000L);
        Order order = mock(Order.class);
        when(orders.findOrderForUpdate(1L)).thenReturn(order);
        when(payments.applySuccess(1L)).thenReturn(new PaymentStateChangeResponse(false, null));
        when(orders.markAsPaid(1L)).thenReturn(false);

        // when
        service.completePaymentSuccess(info);

        // then
        verifyNoInteractions(carts);
        verify(orders, never()).findOrderItemsByOrderId(anyLong());
    }

    @Test
    void 중복실패는_주문취소와_재고복구를_하지_않는다() {
        // given
        when(payments.applyFailure(1L)).thenReturn(new PaymentStateChangeResponse(false, null));

        // when
        service.completePaymentFailure(1L);

        // then
        verify(orders, never()).cancelOrder(anyLong());
        verifyNoInteractions(products);
    }

    @Test
    void 실패확정과_주문취소가_되면_상품수량만큼_복구한다() {
        // given
        when(payments.applyFailure(1L)).thenReturn(new PaymentStateChangeResponse(true, null));
        when(orders.cancelOrder(1L)).thenReturn(true);
        OrderItem item = mock(OrderItem.class);
        Product product = mock(Product.class);
        when(item.getProduct()).thenReturn(product);
        when(product.getId()).thenReturn(9L);
        when(item.getQuantity()).thenReturn(3);
        when(orders.findOrderItemsByOrderId(1L)).thenReturn(List.of(item));

        // when
        service.completePaymentFailure(1L);

        // then
        verify(products).restoreStock(9L, 3);
    }

    @Test
    void 이미취소된_주문은_재고를_중복복구하지_않는다() {
        // given
        when(payments.applyFailure(1L)).thenReturn(new PaymentStateChangeResponse(true, null));
        when(orders.cancelOrder(1L)).thenReturn(false);

        // when
        service.completePaymentFailure(1L);

        // then
        verifyNoInteractions(products);
        verify(orders, never()).findOrderItemsByOrderId(anyLong());
    }

    @Test
    void 금액불일치_중복취소는_재고를_복구하지_않는다() {
        // given
        when(orders.cancelOrder(1L)).thenReturn(false);

        // when
        service.cancelPaymentForAmountMismatch(1L);

        // then
        verify(payments).applyCancellation(1L);
        verifyNoInteractions(products);
    }
}
