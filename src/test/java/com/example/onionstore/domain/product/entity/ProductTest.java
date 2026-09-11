package com.example.onionstore.domain.product.entity;

import com.example.onionstore.domain.category.entity.Category;
import com.example.onionstore.global.exception.BusinessException;
import com.example.onionstore.global.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ProductTest {
    @Test
    @DisplayName("상품 도메인 로직 테스트 - 가격이 0이하인 경우 에러 반환")
    void 가격_변경_시_0이하인_경우_에러_발생() {
        //given
        Product product = Product.create(
                new Category("category"),
                "name",
                "description",
                1000L,
                10
        );

        //when&then
        assertThatThrownBy(() -> product.changePrice(0))
                .isInstanceOf(BusinessException.class)
                .hasMessage(ErrorCode.INVALID_PRICE.getMessage());
    }

    @Test
    @DisplayName("상품 도메인 로직 테스트 - 재고가 0인 경우 상태를 SOLD_OUT으로 변경")
    void 상품_재고가_0으로_바뀌면_상태를_SOLD_OUT_으로_변경() {
        //given
        Product product = Product.create(
                new Category("category"),
                "name",
                "description",
                1000L,
                10
        );

        //when
        product.changeStock(0);

        //then
        assertThat(product.getStock()).isEqualTo(0);
        assertThat(product.getStatus()).isEqualTo(ProductStatus.SOLD_OUT);
    }

    @Test
    @DisplayName("상품 도메인 로직 테스트 - 재고가 0보다 큰 값으로 변경되면 상태를 SELLING으로 변경")
    void 상품_재고가_0보다_큰_값이면_상태를_SELLING으로_변경한다() {
        //given
        Product product = Product.create(
                new Category("category"),
                "name",
                "description",
                1000L,
                0
        );
        product.changeStatus(ProductStatus.SOLD_OUT);

        //when
        product.changeStock(7);

        //then
        assertThat(product.getStock()).isEqualTo(7);
        assertThat(product.getStatus()).isEqualTo(ProductStatus.SELLING);
    }
}