package com.example.onionstore.domain.cart;

import com.example.onionstore.domain.cart.dto.response.CartResponse;
import com.example.onionstore.domain.cart.entity.Cart;
import com.example.onionstore.domain.cart.entity.CartItem;
import com.example.onionstore.domain.cart.repository.CartItemRepository;
import com.example.onionstore.domain.cart.repository.CartRepository;
import com.example.onionstore.domain.cart.service.CartService;
import com.example.onionstore.domain.category.entity.Category;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class CartQueryServiceTest {
    @Mock CartRepository cartRepository;
    @Mock CartItemRepository cartItemRepository;
    @InjectMocks CartService cartService;

    @Test
    void returnsCartItemsAndTotalPrice() {
        User user = new User("cart@example.com", "hash", "고객", "01012345678", Role.CUSTOMER);
        Cart cart = new Cart(user);
        Product product = Product.create(new Category("양파"), "햇양파", "설명", 15000, 50);
        CartItem item = new CartItem(cart, product, 2);
        given(cartRepository.findByUserId(user.getId())).willReturn(Optional.of(cart));
        given(cartItemRepository.findAllByCartIdOrderByIdAsc(cart.getId())).willReturn(List.of(item));

        CartResponse response = cartService.getCart(user);

        assertThat(response.items()).hasSize(1);
        assertThat(response.items().get(0).price()).isEqualTo(15000);
        assertThat(response.items().get(0).quantity()).isEqualTo(2);
        assertThat(response.totalPrice()).isEqualTo(30000);
    }

    @Test
    void returnsZeroTotalForEmptyCart() {
        User user = new User("empty@example.com", "hash", "고객", "01012345678", Role.CUSTOMER);
        Cart cart = new Cart(user);
        given(cartRepository.findByUserId(user.getId())).willReturn(Optional.of(cart));
        given(cartItemRepository.findAllByCartIdOrderByIdAsc(cart.getId())).willReturn(List.of());

        CartResponse response = cartService.getCart(user);

        assertThat(response.items()).isEmpty();
        assertThat(response.totalPrice()).isZero();
    }

    @Test
    void throwsWhenCartDoesNotExist() {
        User user = new User("missing@example.com", "hash", "고객", "01012345678", Role.CUSTOMER);
        given(cartRepository.findByUserId(user.getId())).willReturn(Optional.empty());

        assertThatThrownBy(() -> cartService.getCart(user))
                .isInstanceOfSatisfying(BusinessException.class,
                        e -> assertThat(e.getErrorCode()).isEqualTo(ErrorCode.CART_NOT_FOUND));
    }
}
