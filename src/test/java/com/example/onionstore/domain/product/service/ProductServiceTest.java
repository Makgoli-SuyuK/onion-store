package com.example.onionstore.domain.product.service;

import com.example.onionstore.domain.category.entity.Category;
import com.example.onionstore.domain.category.service.CategoryService;
import com.example.onionstore.domain.product.dto.ProductCreateRequest;
import com.example.onionstore.domain.product.entity.Product;
import com.example.onionstore.domain.product.repository.ProductRepository;
import com.example.onionstore.global.exception.BusinessException;
import com.example.onionstore.global.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
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
}