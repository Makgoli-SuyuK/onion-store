package com.example.onionstore.domain.product.facade;

import com.example.onionstore.domain.product.entity.Product;
import com.example.onionstore.domain.product.service.ProductLikeService;
import com.example.onionstore.domain.product.service.ProductService;
import com.example.onionstore.domain.user.entity.Role;
import com.example.onionstore.domain.user.entity.User;
import com.example.onionstore.domain.user.service.UserService;
import com.example.onionstore.global.exception.BusinessException;
import com.example.onionstore.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class ProductLikeFacade {
    private final ProductLikeService productLikeService;
    private final ProductService productService;
    private final UserService userService;

    @Transactional
    public void likeProduct(Long userId, Long productId) {
        User user = userService.findUser(userId);
        if (user.getRole() == Role.ADMIN) {
            throw new BusinessException(ErrorCode.LIKE_ADMIN_NOT_ALLOWED);
        }

        Product product = productService.getProductById(productId);
        if (product.isDeleted() || product.isHidden()) {
            throw new BusinessException(ErrorCode.PRODUCT_LIKE_NOT_ALLOWED);
        }

        if (productLikeService.existsLikeProduct(userId, productId)) {
            throw new BusinessException(ErrorCode.ALREADY_LIKED_PRODUCT);
        }

        productLikeService.likeProduct(user, product);
        productService.increaseLikeCount(product);
    }
}
