package com.example.onionstore.domain.cart;

import com.example.onionstore.domain.cart.entity.Cart;
import com.example.onionstore.domain.cart.entity.CartItem;
import com.example.onionstore.domain.cart.repository.CartItemRepository;
import com.example.onionstore.domain.cart.repository.CartRepository;
import com.example.onionstore.domain.cart.service.CartService;
import com.example.onionstore.domain.category.entity.Category;
import com.example.onionstore.domain.product.entity.Product;
import com.example.onionstore.domain.product.entity.ProductStatus;
import com.example.onionstore.domain.user.entity.Role;
import com.example.onionstore.domain.user.entity.User;
import com.example.onionstore.global.exception.BusinessException;
import com.example.onionstore.global.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import java.util.Optional;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartMutationServiceTest {
    @Mock CartRepository cartRepository;
    @Mock CartItemRepository cartItemRepository;
    @InjectMocks CartService cartService;
    User user;
    Cart cart;
    Product product;
    CartItem item;

    @BeforeEach
    void setUp() {
        user = new User("cart@example.com", "hash", "고객", "01012345678", Role.CUSTOMER);
        ReflectionTestUtils.setField(user, "id", 1L);
        cart = new Cart(user);
        ReflectionTestUtils.setField(cart, "id", 2L);
        product = Product.create(new Category("양파"), "양파", "설명", 1000, 5);
        ReflectionTestUtils.setField(product, "id", 3L);
        item = new CartItem(cart, product, 4);
    }

    @ParameterizedTest
    @EnumSource(ProductStatus.class)
    void deletesOwnItemRegardlessOfProductStatus(ProductStatus status) {
        ReflectionTestUtils.setField(product, "status", status);
        given(cartItemRepository.findByIdWithProductAndCart(10L)).willReturn(Optional.of(item));
        cartService.deleteItem(1L, 10L);
        verify(cartItemRepository).delete(item);
        verifyNoInteractions(cartRepository);
        assertThat(product.getStock()).isEqualTo(5);
    }

    @Test
    void deletesSoftDeletedProductFromCart() {
        product.markAsDeleted();
        given(cartItemRepository.findByIdWithProductAndCart(10L)).willReturn(Optional.of(item));
        cartService.deleteItem(1L, 10L);
        verify(cartItemRepository).delete(item);
        verifyNoInteractions(cartRepository);
    }

    @Test
    void rejectsOtherUsersDeletion() {
        given(cartItemRepository.findByIdWithProductAndCart(10L)).willReturn(Optional.of(item));
        assertError(() -> cartService.deleteItem(2L, 10L), ErrorCode.CART_ITEM_ACCESS_DENIED);
        verify(cartItemRepository, never()).delete(any(CartItem.class));
    }

    @Test
    void rejectsMissingItemDeletion() {
        given(cartItemRepository.findByIdWithProductAndCart(10L)).willReturn(Optional.empty());
        assertError(() -> cartService.deleteItem(1L, 10L), ErrorCode.CART_ITEM_NOT_FOUND);
        verify(cartItemRepository, never()).delete(any(CartItem.class));
    }

    private void existingItem() {
        given(cartRepository.findByUserId(1L)).willReturn(Optional.of(cart));
        given(cartItemRepository.findByCartIdAndProductId(2L, 3L)).willReturn(Optional.of(item));
    }

    @Test
    void rejectsAdditionExceedingStockInTotal() {
        existingItem();
        assertError(() -> cartService.addItem(user, product, 2), ErrorCode.PRODUCT_OUT_OF_STOCK);
        assertThat(item.getQuantity()).isEqualTo(4);
        verify(cartItemRepository, never()).save(any());
    }

    @Test
    void acceptsAdditionExactlyMatchingStock() {
        existingItem();
        assertThat(cartService.addItem(user, product, 1).quantity()).isEqualTo(5);
        assertThat(product.getStock()).isEqualTo(5);
    }

    @Test
    void rejectsOverflowWithoutChangingQuantity() {
        existingItem();
        ReflectionTestUtils.setField(product, "stock", Integer.MAX_VALUE);
        item.updateQuantity(Integer.MAX_VALUE);
        assertError(() -> cartService.addItem(user, product, 1), ErrorCode.PRODUCT_OUT_OF_STOCK);
        assertThat(item.getQuantity()).isEqualTo(Integer.MAX_VALUE);
    }

    @Test
    void acceptsUpdateExactlyMatchingStock() {
        assertThat(cartService.updateQuantity(item, product, 5).quantity()).isEqualTo(5);
        assertThat(product.getStock()).isEqualTo(5);
    }

    @Test
    void rejectsUpdateExceedingStock() {
        assertError(() -> cartService.updateQuantity(item, product, 6), ErrorCode.PRODUCT_OUT_OF_STOCK);
        assertThat(item.getQuantity()).isEqualTo(4);
    }

    @ParameterizedTest
    @EnumSource(value = ProductStatus.class, names = {"SOLD_OUT", "HIDDEN"})
    void rejectsUnavailableProductAdditionAndUpdate(ProductStatus status) {
        ReflectionTestUtils.setField(product, "status", status);
        assertError(() -> cartService.addItem(user, product, 1), ErrorCode.PRODUCT_NOT_SELLING);
        assertError(() -> cartService.updateQuantity(item, product, 1), ErrorCode.PRODUCT_NOT_SELLING);
        assertThat(item.getQuantity()).isEqualTo(4);
        verifyNoInteractions(cartRepository, cartItemRepository);
    }

    @Test
    void rejectsDeletedProductAdditionAndUpdate() {
        product.markAsDeleted();
        assertError(() -> cartService.addItem(user, product, 1), ErrorCode.PRODUCT_NOT_SELLING);
        assertError(() -> cartService.updateQuantity(item, product, 1), ErrorCode.PRODUCT_NOT_SELLING);
        assertThat(item.getQuantity()).isEqualTo(4);
    }

    private void assertError(Runnable action, ErrorCode code) {
        assertThatThrownBy(action::run).isInstanceOfSatisfying(BusinessException.class,
                e -> assertThat(e.getErrorCode()).isEqualTo(code));
    }
}
