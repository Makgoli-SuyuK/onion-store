package com.example.onionstore.domain.product.service;

import com.example.onionstore.domain.category.entity.Category;
import com.example.onionstore.domain.category.service.CategoryService;
import com.example.onionstore.domain.product.ProductFixture;
import com.example.onionstore.domain.product.dto.ProductCreateRequest;
import com.example.onionstore.domain.product.dto.ProductEditRequest;
import com.example.onionstore.domain.product.dto.ProductResponse;
import com.example.onionstore.domain.product.dto.ProductSimpleResponse;
import com.example.onionstore.domain.product.entity.Product;
import com.example.onionstore.domain.product.entity.ProductStatus;
import com.example.onionstore.domain.product.repository.ProductRepository;
import com.example.onionstore.domain.product.repository.dto.ProductSearchConditions;
import com.example.onionstore.global.exception.BusinessException;
import com.example.onionstore.global.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {
    @Mock
    private ProductRepository productRepository;
    @Mock
    private CategoryService categoryService;
    @InjectMocks
    private ProductService productService;

    @Test
    @DisplayName("상품 추가 비즈니스 로직 테스트")
    void createProduct() {
        //given
        ProductCreateRequest createRequest = new ProductCreateRequest(
                "product test",
                "name1",
                "description",
                10000,
                40
        );
        Category category = new Category(createRequest.category());

        //when
        productService.createProduct(createRequest, category);

        //then
        verify(productRepository).save(any(Product.class));
    }

    @Test
    @DisplayName("상품 추가 비즈니스 로직 테스트 - 재고가 1보다 작음")
    void 재고가_1보다_작으면_에러가_발생한다() {
        //given
        ProductCreateRequest createRequest = new ProductCreateRequest(
                "product test",
                "name1",
                "description",
                10000,
                0
        );
        Category category = new Category(createRequest.category());

        //when&then
        assertThatThrownBy(() -> productService.createProduct(createRequest, category))
                .isInstanceOf(BusinessException.class)
                .hasMessage(ErrorCode.INVALID_STOCK.getMessage());
    }

    @Test
    @DisplayName("상품 추가 비즈니스 로직 테스트 - 가격이 1보다 작음")
    void 가격이_1보다_작으면_에러가_발생한다() {
        //given
        ProductCreateRequest createRequest = new ProductCreateRequest(
                "product test",
                "name1",
                "description",
                0,
                1
        );
        Category category = new Category(createRequest.category());

        //when&then
        assertThatThrownBy(() -> productService.createProduct(createRequest, category))
                .isInstanceOf(BusinessException.class)
                .hasMessage(ErrorCode.INVALID_PRICE.getMessage());
    }

    @Test
    @DisplayName("상품 단일 조회 로직 테스트")
    void 상품이_존재한다면_상품_상세_정보를_반환한다() {
        //given
        Product product = Product.create(
                new Category("category"),
                "name",
                "desc",
                10000,
                40
        );
        ReflectionTestUtils.setField(product, "id", 1L);

        given(productRepository.findByIdAndDeletedFalse(anyLong())).willReturn(Optional.of(product));

        //when
        ProductResponse res = productService.findById(1L);

        //then
        assertThat(res.categoryName()).isEqualTo(product.getCategory().getName());
        assertThat(res.name()).isEqualTo(product.getName());
        assertThat(res.price()).isEqualTo(product.getPrice());
    }

    @Test
    @DisplayName("상품 검색 비즈니스 로직 테스트")
    void 모든_조건이_있을_시_조건에_부합한_상품을_조회한다() {
        //given
        ProductSearchConditions conditions = new ProductSearchConditions(
                "category",
                "name",
                1000L,
                100000L,
                50,
                "name",
                "asc",
                0,
                10
        );
        Pageable pageable = PageRequest.of(0, 10);

        List<ProductSimpleResponse> content = List.of(
                new ProductSimpleResponse(
                        1,
                        "category",
                        "name1",
                        1500,
                        51
                ),
                new ProductSimpleResponse(
                        2,
                        "category",
                        "name2",
                        1500,
                        51
                )
        );

        given(productRepository.searchWithConditions(any(ProductSearchConditions.class), any(Pageable.class)))
                .willReturn(new PageImpl<>(
                        content,
                        pageable,
                        content.size()
                ));

        //when
        Page<ProductSimpleResponse> res = productService.searchWithConditions(conditions, 1, 10);

        //then
        assertThat(res.getTotalElements()).isEqualTo(2);
        assertThat(res.getTotalPages()).isEqualTo(1);
        assertThat(res.getContent().get(0).name()).isEqualTo("name1");
    }

    @Test
    @DisplayName("상품 삭제 비즈니스 로직 테스트")
    void 상품이_존재하면_삭제한다() {
        //given
        Product product = Product.create(
                new Category("category"),
                "name",
                "description",
                10000L,
                10
        );
        ReflectionTestUtils.setField(product, "id", 1L);
        ReflectionTestUtils.setField(product, "status", ProductStatus.SELLING);

        given(productRepository.findByIdForUpdate(anyLong())).willReturn(Optional.of(product));

        //when
        productService.deleteProduct(1L);

        //then
        assertThat(product.isDeleted()).isTrue();
        assertThat(product.getStatus()).isEqualTo(ProductStatus.HIDDEN);
    }

    @Test
    @DisplayName("상품 삭제 시 이미 삭제된 상품이라면 PRODUCT_NOT_FOUND를 던진다")
    void 상품이_이미_삭제됐으면_PRODUCT_NOT_FOUND_반환() {
        //given
        Product product = Product.create(
                new Category("category"),
                "name",
                "description",
                10000L,
                10
        );
        ReflectionTestUtils.setField(product, "id", 1L);
        ReflectionTestUtils.setField(product, "deleted", true);

        given(productRepository.findByIdForUpdate(anyLong())).willReturn(Optional.of(product));

        //when&then
        assertThatThrownBy(() -> productService.deleteProduct(1L))
                .isInstanceOf(BusinessException.class)
                .hasMessage(ErrorCode.PRODUCT_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("상품 수정 비즈니스 로직 테스트")
    void 상품_수정_성공_테스트() {
        //given
        ProductEditRequest editRequest = new ProductEditRequest(
                "name",
                "desc",
                1000L,
                20,
                ProductStatus.SELLING.name()
        );

        Product product = Product.create(
                new Category("category"),
                "name213",
                "description",
                1000,
                10
        );

        given(productRepository.findByIdForUpdate(anyLong())).willReturn(Optional.of(product));

        product.changeName(editRequest.name());
        product.changeDescription(editRequest.description());
        product.changePrice(editRequest.price());
        product.changeStock(editRequest.stock());
        product.changeStatus(ProductStatus.valueOf(editRequest.status()));
        ReflectionTestUtils.setField(product, "id", 1L);

        given(productRepository.save(any(Product.class))).willReturn(product);

        //when
        ProductResponse res = productService.editProduct(1L, editRequest);

        //then
        assertThat(res.categoryName()).isEqualTo("category");
        assertThat(res.description()).isEqualTo("desc");
        assertThat(res.price()).isEqualTo(1000);
        assertThat(res.stock()).isEqualTo(20);
    }

    @Test
    @DisplayName("좋아요 순으로 상위 10개의 상품 정보만 조회한다")
    void 좋아요_순으로_상위_10개의_상품_정보를_조회한다() {
        //given
        List<Product> list = new ArrayList<>();
        for (int i = 10; i >= 1; i--) {
            Product product = ProductFixture.createProduct(i, new Category("category"));
            ReflectionTestUtils.setField(product, "id", 1L);
            list.add(product);
        }

        given(productRepository.find10OrderByLikeCountDesc()).willReturn(list);

        //when
        List<ProductSimpleResponse> res = productService.find10OrderByLikeCountDesc();

        //then
        assertThat(res.size()).isEqualTo(10);
        assertThat(res)
                .extracting(ProductSimpleResponse::likeCount)
                .isSortedAccordingTo(Comparator.reverseOrder());
    }
}