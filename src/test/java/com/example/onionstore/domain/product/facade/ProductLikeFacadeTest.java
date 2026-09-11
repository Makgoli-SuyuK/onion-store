package com.example.onionstore.domain.product.facade;

import com.example.onionstore.domain.category.entity.Category;
import com.example.onionstore.domain.product.entity.Product;
import com.example.onionstore.domain.product.entity.ProductLike;
import com.example.onionstore.domain.product.entity.ProductStatus;
import com.example.onionstore.domain.product.service.ProductLikeService;
import com.example.onionstore.domain.product.service.ProductService;
import com.example.onionstore.domain.user.entity.Role;
import com.example.onionstore.domain.user.entity.User;
import com.example.onionstore.domain.user.service.UserService;
import com.example.onionstore.global.exception.BusinessException;
import com.example.onionstore.global.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ProductLikeFacadeTest {
    @Mock
    private ProductLikeService productLikeService;
    @Mock
    private ProductService productService;
    @Mock
    private UserService userService;

    @InjectMocks
    private ProductLikeFacade productLikeFacade;

    @Test
    @DisplayName("상품 좋아요 처리 정상 작동 테스트")
    void 사용자가_고객이고_상품이_삭제처리_되지_않았다면_좋아요를_추가할_수_있다() {
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
                100000L,
                10
        );

        given(userService.findUser(anyLong())).willReturn(user);
        given(productService.findProductForUpdate(anyLong())).willReturn(product);

        //when
        productLikeFacade.toggleLike(1L, 1L);

        //then
        verify(productLikeService).likeProduct(user, product);
        verify(productService).increaseLikeCount(product);
    }

    @Test
    @DisplayName("사용자가 관리자라면 에러를 반환한다")
    void 사용자가_관리자라면_에러를_반환한다() {
        //given
        User user = new User(
                "test@email.com",
                "password",
                "name",
                "010-0000-0000",
                Role.ADMIN
        );

        given(userService.findUser(anyLong())).willReturn(user);

        //when&then
        assertThatThrownBy(() -> productLikeFacade.toggleLike(1L, 1L))
                .isInstanceOf(BusinessException.class)
                .hasMessage(ErrorCode.LIKE_ADMIN_NOT_ALLOWED.getMessage());

    }

    @Test
    @DisplayName("상품이 삭제 처리되었다면 에러를 반환한다")
    void 상품이_삭제_처리되었다면_에러를_반환한다() {
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
        product.markAsDeleted();

        given(userService.findUser(anyLong())).willReturn(user);
        given(productService.findProductForUpdate(anyLong())).willReturn(product);

        //when&then
        assertThatThrownBy(() -> productLikeFacade.toggleLike(1L, 1L))
                .isInstanceOf(BusinessException.class)
                .hasMessage(ErrorCode.PRODUCT_LIKE_NOT_ALLOWED.getMessage());
    }

    @Test
    @DisplayName("상품 상태가 HIDDEN이라면 예외를 던진다")
    void 상품_상태가_HIDDEN이라면_예외를_던진다() {
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
        product.changeStatus(ProductStatus.HIDDEN);

        given(userService.findUser(anyLong())).willReturn(user);
        given(productService.findProductForUpdate(anyLong())).willReturn(product);

        //when&then
        assertThatThrownBy(() -> productLikeFacade.toggleLike(1L, 1L))
                .isInstanceOf(BusinessException.class)
                .hasMessage(ErrorCode.PRODUCT_LIKE_NOT_ALLOWED.getMessage());
    }

    @Test
    @DisplayName("이미 좋아요한 상품이라면 좋아요를 취소한다.")
    void 이미_좋아요했다면_좋아요_데이터를_삭제하고_좋아요_수를_감소시킨다() {
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

        ProductLike productLike = ProductLike.create(
                user,
                product
        );

        given(userService.findUser(anyLong())).willReturn(user);
        given(productService.findProductForUpdate(anyLong())).willReturn(product);
        given(productLikeService.getProductLike(anyLong(), anyLong())).willReturn(Optional.of(productLike));

        //when
        productLikeFacade.toggleLike(1L, 1L);

        //then
        verify(productLikeService).deleteProductLike(anyLong(), anyLong());
        verify(productService).decreaseLikeCount(any(Product.class));
    }
}