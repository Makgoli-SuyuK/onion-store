package com.example.onionstore.domain.product.facade;

import com.example.onionstore.domain.category.entity.Category;
import com.example.onionstore.domain.category.service.CategoryService;
import com.example.onionstore.domain.product.dto.ProductCreateRequest;
import com.example.onionstore.domain.product.dto.ProductDetailWithLiked;
import com.example.onionstore.domain.product.dto.ProductEditRequest;
import com.example.onionstore.domain.product.dto.ProductResponse;
import com.example.onionstore.domain.product.service.ProductLikeService;
import com.example.onionstore.domain.product.service.ProductService;
import com.example.onionstore.domain.user.entity.Role;
import com.example.onionstore.domain.user.entity.User;
import com.example.onionstore.domain.user.service.UserService;
import com.example.onionstore.global.exception.BusinessException;
import com.example.onionstore.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProductFacade {
    private final ProductService productService;
    private final CategoryService categoryService;
    private final UserService userService;
    private final ProductLikeService productLikeService;

    public void addProduct(Long userId, ProductCreateRequest createRequest) {
        if (userService.findUser(userId).getRole() != Role.ADMIN) {
            throw new BusinessException(ErrorCode.FORBIDDEN_ROLE);
        }

        Category category = categoryService.getCategoryByName(createRequest.category());

        productService.createProduct(createRequest, category);
    }

    public void deleteProduct(Long userId, Long productId) {
        if (userService.findUser(userId).getRole() != Role.ADMIN) {
            throw new BusinessException(ErrorCode.FORBIDDEN_ROLE);
        }

        productService.deleteProduct(productId);
    }

    public ProductResponse editProduct(Long userId, Long productId, ProductEditRequest editRequest) {
        User user = userService.findUser(userId);

        if (user.getRole() != Role.ADMIN) {
            throw new BusinessException(ErrorCode.FORBIDDEN_ROLE);
        }

        return productService.editProduct(productId, editRequest);
    }

    public ProductDetailWithLiked getProductDetail(Long userId, Long productId) {
        boolean isLiked = false;

        ProductResponse productInfo = productService.findById(productId);


        if (userId != null){
            User user = userService.findUser(userId);

            if (!user.getRole().equals(Role.ADMIN) && productLikeService.getProductLike(userId, productId).isPresent()) {
                isLiked = true;
            }
        }

        return ProductDetailWithLiked.from(productInfo, isLiked);
    }
}
