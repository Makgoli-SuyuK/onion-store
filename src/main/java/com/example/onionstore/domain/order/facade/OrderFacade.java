package com.example.onionstore.domain.order.facade;

import com.example.onionstore.domain.cart.entity.CartItem;
import com.example.onionstore.domain.cart.service.CartService;
import com.example.onionstore.domain.order.dto.GetOrderListItemResponse;
import com.example.onionstore.domain.order.dto.OrderCreateRequest;
import com.example.onionstore.domain.order.dto.OrderCreateResponse;
import com.example.onionstore.domain.order.entity.Order;
import com.example.onionstore.domain.order.entity.OrderItem;
import com.example.onionstore.domain.order.repository.OrderItemRepository;
import com.example.onionstore.domain.order.service.OrderService;
import com.example.onionstore.domain.payment.dto.CreatePaymentResponse;
import com.example.onionstore.domain.payment.entity.Payment;
import com.example.onionstore.domain.payment.service.PaymentService;
import com.example.onionstore.domain.product.entity.Product;
import com.example.onionstore.domain.product.service.ProductService;
import com.example.onionstore.domain.user.entity.User;
import com.example.onionstore.domain.user.service.UserService;
import com.example.onionstore.global.exception.BusinessException;
import com.example.onionstore.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderFacade {

    private final UserService userService;
    private final CartService cartService;
    private final OrderService orderService;
    private final PaymentService paymentService;
    private final OrderItemRepository orderItemRepository;
    private final ProductService productService;

    @Transactional
    public OrderCreateResponse createOrder(Long userId, OrderCreateRequest request) {
        // request가 없으면 장바구니 전체주문
        List<Long> cartItemIds = (request != null) ? request.cartItemIds() : List.of();
        User user = userService.findUser(userId);

        // 장바구니 조회
        List<CartItem> cartItems = getValidateCartItems(userId, cartItemIds);

        long totalPrice = 0;
        for (CartItem cartItem : cartItems) {
            totalPrice += cartItem.getProduct().getPrice() * cartItem.getQuantity();
        }
        Order order = orderService.createOrder(user, totalPrice);

        List<OrderItem> orderItems = new ArrayList<>();

        for (CartItem cartItem : cartItems) {
            Product product = cartItem.getProduct();
            productService.decreaseStock(product.getId(), cartItem.getQuantity());

            OrderItem orderItem = new OrderItem(order,
                    product, product.getName(),
                    product.getPrice(), cartItem.getQuantity());
            orderItems.add(orderItem);
        }
        orderItemRepository.saveAll(orderItems);

        CreatePaymentResponse payment = paymentService.createPayment(order, order.getTotalPrice());

        List<Long> orderedItemIds = cartItems.stream().map(CartItem::getId).toList();
        cartService.clearCartItems(orderedItemIds, userId);

        List<GetOrderListItemResponse> items = orderItems.stream()
                .map(orderItem -> GetOrderListItemResponse.from(orderItem))
                .toList();

        return new OrderCreateResponse(
                order.getId(), order.getOrderNumber(), order.getTotalPrice(),
                order.getCreatedAt(), order.getStatus(), payment.portonePaymentId(),
                items
        );

    }

    private List<CartItem> getValidateCartItems(Long userId, List<Long> cartItemIds) {
        // cartItems가 비어있으면 전체, 아니면 선택 조회
        List<CartItem> cartItems = cartItemIds.isEmpty() ? cartService.findCartEntities(userId)
                : cartService.findCartEntitiesByIds(userId, cartItemIds);

        if (cartItems.isEmpty()) {
            throw new BusinessException(ErrorCode.CART_EMPTY);
        }

        if (!cartItemIds.isEmpty() && cartItems.size() != cartItemIds.size()) {
            throw new BusinessException(ErrorCode.CART_ITEM_NOT_FOUND);
        }
        return cartItems;
    }
}
