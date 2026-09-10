package com.example.onionstore.domain.cart.facade;

import com.example.onionstore.domain.cart.dto.request.AddCartItemRequest;
import com.example.onionstore.domain.cart.dto.response.CartItemResponse;
import com.example.onionstore.domain.cart.service.CartService;
import com.example.onionstore.domain.product.entity.Product;
import com.example.onionstore.domain.product.service.ProductService;
import com.example.onionstore.domain.user.entity.User;
import com.example.onionstore.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class CartFacade {

    private final UserService userService;
    private final CartService cartService;
    private final ProductService productService;

    @Transactional
    public CartItemResponse addItem(Long userId, AddCartItemRequest request) {
        User user = userService.findUser(userId);
        Product product = productService.getProductForCart(request.productId(), request.quantity());
        return cartService.addItem(user, product, request.quantity());
    }
}
