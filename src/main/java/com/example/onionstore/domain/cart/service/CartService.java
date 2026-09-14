package com.example.onionstore.domain.cart.service;

import com.example.onionstore.domain.cart.dto.response.CartItemResponse;
import com.example.onionstore.domain.cart.dto.response.CartResponse;
import com.example.onionstore.domain.cart.entity.Cart;
import com.example.onionstore.domain.cart.entity.CartItem;
import com.example.onionstore.domain.cart.repository.CartItemRepository;
import com.example.onionstore.domain.cart.repository.CartRepository;
import com.example.onionstore.domain.product.entity.Product;
import com.example.onionstore.domain.product.entity.ProductStatus;
import com.example.onionstore.domain.user.entity.User;
import com.example.onionstore.global.exception.BusinessException;
import com.example.onionstore.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;

    public List<CartItem> findCartEntities(Long userId) {
        return cartItemRepository.findByCart_UserId(userId);
    }

    @Transactional
    public CartItemResponse addItem(User user, Product product, int quantity) {
        validateProductAndQuantity(product, quantity);
        Cart cart = findOrCreateCart(user);
        CartItem cartItem = cartItemRepository.findByCartIdAndProductId(cart.getId(), product.getId())
                .map(existingItem -> {
                    // long으로 합산해 int 범위를 넘는 요청도 재고 검증에서 거부한다.
                    long finalQuantity = (long) existingItem.getQuantity() + quantity;
                    validateProductAndQuantity(product, finalQuantity);
                    existingItem.updateQuantity((int) finalQuantity);
                    return existingItem;
                })
                .orElseGet(() -> cartItemRepository.save(new CartItem(cart, product, quantity)));
        return CartItemResponse.from(cartItem);
    }

    @Transactional(readOnly = true)
    public CartResponse getCart(User user) {
        Cart cart = cartRepository.findByUserId(user.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.CART_NOT_FOUND));
        var items = cartItemRepository.findAllByCartIdOrderByIdAsc(cart.getId()).stream()
                .map(CartItemResponse::from)
                .toList();
        return CartResponse.from(cart, items);
    }

    public List<CartItem> findCartEntitiesByIds(Long userId, List<Long> cartItemIds) {
        return cartItemRepository.findByIdInAndCart_User_IdWithProduct(cartItemIds, userId);
    }

    @Transactional(readOnly = true)
    public CartItem findCartItem(Long userId, Long cartItemId) {
        CartItem cartItem = cartItemRepository.findByIdWithProductAndCart(cartItemId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CART_ITEM_NOT_FOUND));
        if (!cartItem.getCart().getUser().getId().equals(userId)) {
            throw new BusinessException(ErrorCode.CART_ITEM_ACCESS_DENIED);
        }
        return cartItem;
    }

    @Transactional
    public CartItemResponse updateQuantity(CartItem cartItem, Product product, int quantity) {
        validateProductAndQuantity(product, quantity);
        cartItem.updateQuantity(quantity);
        return CartItemResponse.from(cartItem);
    }

    @Transactional
    public void deleteItem(Long userId, Long cartItemId) {
        CartItem cartItem = findCartItem(userId, cartItemId);
        cartItemRepository.delete(cartItem);
    }

    private void validateProductAndQuantity(Product product, long quantity) {
        if (quantity < 1) {
            throw new BusinessException(ErrorCode.INVALID_CART_ITEM_QUANTITY);
        }
        if (product.isDeleted() || product.getStatus() != ProductStatus.SELLING) {
            throw new BusinessException(ErrorCode.PRODUCT_NOT_SELLING);
        }
        if (quantity > product.getStock()) {
            throw new BusinessException(ErrorCode.PRODUCT_OUT_OF_STOCK);
        }
    }

    private Cart findOrCreateCart(User user) { // 첫 상품 추가 요청이면 사용자 장바구니를 함께 만든다.
        return cartRepository.findByUserId(user.getId())
                .orElseGet(() -> cartRepository.save(new Cart(user)));
    }

    public void clearCartItems(List<Long> orderedItemIds, Long userId) {
        int deleted = cartItemRepository.deleteAllByIdInAndCart_User_Id(orderedItemIds, userId);
        if (deleted != orderedItemIds.size()) {
            log.warn("장바구니 삭제 불일치: expected={}, actual={}, userId={}",
                    orderedItemIds.size(), deleted, userId);
        }
    }
}
