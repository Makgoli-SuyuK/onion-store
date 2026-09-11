package com.example.onionstore.domain.product.facade;

import com.example.onionstore.domain.category.entity.Category;
import com.example.onionstore.domain.category.service.CategoryService;
import com.example.onionstore.domain.product.entity.Product;
import com.example.onionstore.domain.product.entity.ProductStatus;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doAnswer;

@ExtendWith(MockitoExtension.class)
public class ProductFacadeTest {
    @Mock
    private ProductService productService;
    @Mock
    private UserService userService;
    @Mock
    private CategoryService categoryService;
    @InjectMocks
    private ProductFacade productFacade;

    @Test
    @DisplayName("수정 시도 시 현재 계정이 관리자가 아니면 FORBIDDEN_ROLE을 던진다")
    void 수정_시_관리자가_아니면_FORBIDDEN_ROLE을_던진다() {
        //given
        User user = new User(
                "test@emali.com",
                "password",
                "name",
                "010-0000-0000",
                Role.CUSTOMER
        );

        given(userService.findUser(anyLong())).willReturn(user);

        //when&then
        assertThatThrownBy(() -> productFacade.deleteProduct(1L, 1L))
                .isInstanceOf(BusinessException.class)
                .hasMessage(ErrorCode.FORBIDDEN_ROLE.getMessage());
    }

    @Test
    @DisplayName("상품 삭제 facade 테스트")
    void 상품_삭제_성공_테스트() {
        //given
        User user = new User(
                "test@email.com",
                "password",
                "name",
                "010-0000-0000",
                Role.ADMIN
        );

        Product product = Product.create(
                new Category("category"),
                "name",
                "description",
                10000L,
                10
        );

        given(userService.findUser(anyLong())).willReturn(user);

        doAnswer(invocation -> {
            product.markAsDeleted();
            return null;
        }).when(productService).deleteProduct(anyLong());

        //when
        productFacade.deleteProduct(1L, 1L);

        //then
        assertThat(product.isDeleted()).isTrue();
        assertThat(product.getStatus()).isEqualTo(ProductStatus.HIDDEN);
    }

    @Test
    @DisplayName("상품 삭제 facade 테스트 - 관리자 아님")
    void 상품_삭제_시_관리자_계정이_아니면_에러_반환() {
        //given
        User user = new User(
                "test@email.com",
                "password",
                "name",
                "010-0000-0000",
                Role.CUSTOMER
        );

        given(userService.findUser(anyLong())).willReturn(user);

        //when&then
        assertThatThrownBy(() -> productFacade.deleteProduct(1L, 1L))
                .isInstanceOf(BusinessException.class)
                .hasMessage(ErrorCode.FORBIDDEN_ROLE.getMessage());
    }
}
