package com.example.onionstore.domain.cart;

import com.example.onionstore.domain.cart.dto.request.UpdateCartItemQuantityRequest;
import com.example.onionstore.domain.cart.entity.Cart;
import com.example.onionstore.domain.cart.entity.CartItem;
import com.example.onionstore.domain.cart.facade.CartFacade;
import com.example.onionstore.domain.cart.repository.CartItemRepository;
import com.example.onionstore.domain.cart.repository.CartRepository;
import com.example.onionstore.domain.cart.service.CartService;
import com.example.onionstore.domain.category.entity.Category;
import com.example.onionstore.domain.product.entity.Product;
import com.example.onionstore.domain.product.service.ProductService;
import com.example.onionstore.domain.user.service.UserService;
import com.example.onionstore.domain.user.entity.User;
import com.example.onionstore.domain.user.entity.Role;
import org.springframework.test.util.ReflectionTestUtils;
import com.example.onionstore.global.exception.BusinessException;
import com.example.onionstore.global.exception.ErrorCode;
import jakarta.validation.Validation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class CartQuantityUpdateTest {
    @Mock CartRepository cartRepository;
    @Mock CartItemRepository cartItemRepository;
    @Mock UserService userService;
    @Mock ProductService productService;

    private CartItem item() {
        User user = new User("cart@example.com", "hash", "고객", "01012345678", Role.CUSTOMER);
        ReflectionTestUtils.setField(user, "id", 1L);
        return new CartItem(new Cart(user),
                Product.create(new Category("양파"), "양파", "설명", 1000, 10), 2);
    }

    private CartFacade facade() {
        return new CartFacade(userService, new CartService(cartRepository, cartItemRepository), productService);
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 5})
    void replacesQuantityInsteadOfAdding(int quantity) {
        CartItem item = item();
        given(cartItemRepository.findById(10L)).willReturn(Optional.of(item));

        var response = facade().updateQuantity(1L, 10L, new UpdateCartItemQuantityRequest(quantity));

        assertThat(response.quantity()).isEqualTo(quantity);
        assertThat(item.getQuantity()).isEqualTo(quantity);
    }

    @Test
    void rejectsMissingItem() {
        given(cartItemRepository.findById(10L)).willReturn(Optional.empty());
        assertThatThrownBy(() -> facade().updateQuantity(1L, 10L, new UpdateCartItemQuantityRequest(3)))
                .isInstanceOfSatisfying(BusinessException.class,
                        e -> assertThat(e.getErrorCode()).isEqualTo(ErrorCode.CART_ITEM_NOT_FOUND));
    }

    @Test
    void rejectsOtherUsersItemWithoutChangingQuantity() {
        CartItem item = item();
        given(cartItemRepository.findById(10L)).willReturn(Optional.of(item));

        assertThatThrownBy(() -> facade().updateQuantity(2L, 10L, new UpdateCartItemQuantityRequest(3)))
                .isInstanceOfSatisfying(BusinessException.class,
                        e -> assertThat(e.getErrorCode()).isEqualTo(ErrorCode.CART_ITEM_ACCESS_DENIED));
        assertThat(item.getQuantity()).isEqualTo(2);
        verifyNoInteractions(productService);
    }

    @Test
    void keepsQuantityWhenStockValidationFails() {
        CartItem item = item();
        given(cartItemRepository.findById(10L)).willReturn(Optional.of(item));
        given(productService.getProductForCart(item.getProduct().getId(), 11))
                .willThrow(new BusinessException(ErrorCode.PRODUCT_OUT_OF_STOCK));

        assertThatThrownBy(() -> facade().updateQuantity(1L, 10L, new UpdateCartItemQuantityRequest(11)))
                .isInstanceOfSatisfying(BusinessException.class,
                        e -> assertThat(e.getErrorCode()).isEqualTo(ErrorCode.PRODUCT_OUT_OF_STOCK));
        assertThat(item.getQuantity()).isEqualTo(2);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1})
    void rejectsNonPositiveQuantityAtRequestAndEntity(int quantity) {
        try (var factory = Validation.buildDefaultValidatorFactory()) {
            assertThat(factory.getValidator().validate(new UpdateCartItemQuantityRequest(quantity))).isNotEmpty();
        }
        CartItem item = item();
        assertThatThrownBy(() -> item.updateQuantity(quantity))
                .isInstanceOfSatisfying(BusinessException.class,
                        e -> assertThat(e.getErrorCode()).isEqualTo(ErrorCode.INVALID_CART_ITEM_QUANTITY));
        assertThat(item.getQuantity()).isEqualTo(2);
    }
}
