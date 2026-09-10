package com.example.onionstore.domain.cart.service;

import com.example.onionstore.domain.cart.dto.response.CartItemResponse;
import com.example.onionstore.domain.cart.entity.Cart;
import com.example.onionstore.domain.cart.entity.CartItem;
import com.example.onionstore.domain.cart.repository.CartItemRepository;
import com.example.onionstore.domain.cart.repository.CartRepository;
import com.example.onionstore.domain.product.entity.Product;
import com.example.onionstore.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;

    @Transactional
    public CartItemResponse addItem(User user, Product product, int quantity) {
        Cart cart = findOrCreateCart(user);
        CartItem cartItem = cartItemRepository.findByCartIdAndProductId(cart.getId(), product.getId())
                .map(exisitingItem -> {
                    exisitingItem.addQuantity(quantity);
                    return exisitingItem;
                })
                .orElseGet(() -> cartItemRepository.save(new CartItem(cart, product, quantity)));
        return CartItemResponse.from(cartItem);
    }

    private Cart findOrCreateCart(User user) { // 첫 상품 추가 요청이면 사용자 장바구니를 함께 만든다.
        return cartRepository.findByUserId(user.getId())
                .orElseGet(() -> cartRepository.save(new Cart(user)));
    }

}
