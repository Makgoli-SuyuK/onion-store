package com.example.onionstore.domain.product.service;

import com.example.onionstore.domain.category.entity.Category;
import com.example.onionstore.domain.product.entity.Product;
import com.example.onionstore.domain.product.entity.ProductLike;
import com.example.onionstore.domain.product.repository.ProductLikeRepository;
import com.example.onionstore.domain.user.entity.Role;
import com.example.onionstore.domain.user.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ProductLikeServiceTest {
    @Mock
    private ProductLikeRepository productLikeRepository;
    @InjectMocks
    private ProductLikeService productLikeService;

    @Test
    @DisplayName("상품 좋아요 기능 테스트")
    void 상품_정보와_사용자_정보로_상품_좋아요를_추가한다() {
        //given
        User user = new User(
                "test@email.com",
                "password",
                "name",
                "010-0000-0000",
                Role.CUSTOMER
        );

        Product product = Product.create(
                new Category("category"),
                "name",
                "description",
                10000L,
                10
        );

        //when
        productLikeService.likeProduct(user, product);

        //then
        verify(productLikeRepository).save(any(ProductLike.class));
    }
}