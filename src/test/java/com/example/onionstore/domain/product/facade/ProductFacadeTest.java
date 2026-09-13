package com.example.onionstore.domain.product.facade;

import com.example.onionstore.domain.category.entity.Category;
import com.example.onionstore.domain.category.service.CategoryService;
import com.example.onionstore.domain.product.dto.ProductCreateRequest;
import com.example.onionstore.domain.product.dto.ProductDetailWithLiked;
import com.example.onionstore.domain.product.dto.ProductEditRequest;
import com.example.onionstore.domain.product.dto.ProductResponse;
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
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class ProductFacadeTest {
    @Mock
    private ProductService productService;
    @Mock
    private UserService userService;
    @Mock
    private CategoryService categoryService;
    @Mock
    private ProductLikeService productLikeService;
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

        ProductEditRequest editRequest = new ProductEditRequest(
                "name",
                "description",
                1000L,
                10,
                ProductStatus.SELLING.name()
        );

        //when&then
        assertThatThrownBy(() -> productFacade.editProduct(1L, 1L, editRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessage(ErrorCode.FORBIDDEN_ROLE.getMessage());
    }

    @Test
    @DisplayName("상품 수정 로직 테스트")
    void 관리자_계정은_상품_정보를_수정할_수_있다() {
        //given
        User user = new User(
                "email@test.com",
                "password",
                "name",
                "010-0000-0000",
                Role.ADMIN
        );

        ProductEditRequest editRequest = new ProductEditRequest(
                "name",
                "description",
                10000L,
                10,
                ProductStatus.SELLING.name()
        );

        given(userService.findUser(anyLong())).willReturn(user);
        given(productService.editProduct(anyLong(), any(ProductEditRequest.class)))
                .willReturn(new ProductResponse(
                        1L,
                        "category",
                        "name",
                        "description",
                        1000L,
                        10,
                        10,
                        ProductStatus.SELLING.name(),
                        LocalDateTime.now(),
                        LocalDateTime.now()
                ));

        //when
        ProductResponse res = productFacade.editProduct(1L, 1L, editRequest);

        //then
        assertThat(res.categoryName()).isEqualTo("category");
        assertThat(res.price()).isEqualTo(1000L);
        assertThat(res.description()).isEqualTo("description");
        assertThat(res.id()).isEqualTo(1L);
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

        given(userService.findUser(anyLong())).willReturn(user);

        //when
        productFacade.deleteProduct(1L, 1L);

        //then
        verify(productService).deleteProduct(1L);
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

    @Test
    @DisplayName("상품 추가 facade 테스트 - 관리자 계정")
    void 관리자_계정으로_상품을_추가할_수_있다() {
        //given
        User user = new User(
                "test@email.com",
                "password",
                "name",
                "010-0000-0000",
                Role.ADMIN
        );

        ProductCreateRequest createRequest = new ProductCreateRequest(
                "category",
                "name",
                "description",
                10000L,
                10
        );

        given(userService.findUser(anyLong())).willReturn(user);
        given(categoryService.getCategoryByName(anyString())).willReturn(new Category("category"));

        //when
        productFacade.addProduct(1L, createRequest);

        //then
        verify(productService).createProduct(any(ProductCreateRequest.class), any(Category.class));
    }

    @Test
    @DisplayName("상품 추가 facade 테스트 - 관리자 계정 아닌 경우")
    void 관리자_계정이_아니라면_상품을_추가할_수_없다() {
        //given
        User user = new User(
                "test@email.com",
                "password",
                "name",
                "010-0000-0000",
                Role.CUSTOMER
        );

        ProductCreateRequest createRequest = new ProductCreateRequest(
                "category",
                "name",
                "description",
                10000L,
                10
        );

        given(userService.findUser(anyLong())).willReturn(user);

        //when&then
        assertThatThrownBy(() -> productFacade.addProduct(1L, createRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessage(ErrorCode.FORBIDDEN_ROLE.getMessage());
    }

    @Test
    @DisplayName("상품 상세 조회 facade 테스트 - 일반 사용자")
    void 상품_상세_조회_시_상품_정보_및_좋아요_여부를_조회한다() {
        //given
        User user = new User(
                "test@email.com",
                "password",
                "name",
                "010-0000-0000",
                Role.CUSTOMER
        );
        ReflectionTestUtils.setField(user, "id", 1L);

        Product product = Product.create(
                new Category("category"),
                "name",
                "description",
                1000L,
                10
        );
        ReflectionTestUtils.setField(product, "id", 1L);

        given(userService.findUser(anyLong())).willReturn(user);
        given(productService.findById(anyLong())).willReturn(ProductResponse.from(product));
        given(productLikeService.getProductLike(anyLong(), anyLong())).willReturn(
                Optional.of(ProductLike.create(user, product))
        );

        //when
        ProductDetailWithLiked res = productFacade.getProductDetail(1L,1L);

        //then
        assertThat(res.productInfo().categoryName()).isEqualTo(product.getCategory().getName());
        assertThat(res.productInfo().description()).isEqualTo(product.getDescription());
        assertThat(res.productInfo().price()).isEqualTo(product.getPrice());
        assertThat(res.liked()).isTrue();
    }

    @Test
    @DisplayName("상품 상세 조회 facade 테스트 - 관리자면 isLiked가 항상 false다")
    void 상품_상세_조회_시_현재_계정이_관리자면_isLiked가_항상_false다() {
        //given
        User user = new User(
                "test@email.com",
                "password",
                "name",
                "010-0000-0000",
                Role.ADMIN
        );
        ReflectionTestUtils.setField(user, "id", 1L);

        Product product = Product.create(
                new Category("category"),
                "name",
                "description",
                1000L,
                10
        );
        ReflectionTestUtils.setField(product, "id", 1L);

        given(userService.findUser(anyLong())).willReturn(user);
        given(productService.findById(anyLong())).willReturn(ProductResponse.from(product));

        //when
        ProductDetailWithLiked res = productFacade.getProductDetail(1L,1L);

        //then
        assertThat(res.productInfo().categoryName()).isEqualTo(product.getCategory().getName());
        assertThat(res.productInfo().description()).isEqualTo(product.getDescription());
        assertThat(res.productInfo().price()).isEqualTo(product.getPrice());
        assertThat(res.liked()).isFalse();
    }

    @Test
    @DisplayName("상품 상세 조회 facade 테스트 - userId가 null이면 liked가 항상 false다")
    void 상품_상세_조회_시_userId가_null이면_liked가_항상_false다() {
        //given

        Product product = Product.create(
                new Category("category"),
                "name",
                "description",
                1000L,
                10
        );
        ReflectionTestUtils.setField(product, "id", 1L);

        given(productService.findById(anyLong())).willReturn(ProductResponse.from(product));

        //when
        ProductDetailWithLiked res = productFacade.getProductDetail(null,1L);

        //then
        assertThat(res.productInfo().categoryName()).isEqualTo(product.getCategory().getName());
        assertThat(res.productInfo().description()).isEqualTo(product.getDescription());
        assertThat(res.productInfo().price()).isEqualTo(product.getPrice());
        assertThat(res.liked()).isFalse();
    }
}
