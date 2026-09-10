package com.example.onionstore.domain.cart.service;

import com.example.onionstore.domain.cart.entity.CartItem;
import com.example.onionstore.domain.cart.repository.CartItemRepository;
import com.example.onionstore.domain.cart.repository.CartRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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

    public List<CartItem> findCartEntitiesByIds(Long userId, List<Long> cartItemIds) {
        return cartItemRepository.findByIdInAndCart_User_IdWithProduct(cartItemIds, userId);
    }

    public void clearCartItems(List<Long> orderedItemIds, Long userId) {
        int deleted = cartItemRepository.deleteAllByIdInAndCart_User_Id(orderedItemIds, userId);
        if (deleted != orderedItemIds.size()) {
            log.warn("장바구니 삭제 불일치: expected={}, actual={}, userId={}",
                    orderedItemIds.size(), deleted, userId);
        }
    }
}
