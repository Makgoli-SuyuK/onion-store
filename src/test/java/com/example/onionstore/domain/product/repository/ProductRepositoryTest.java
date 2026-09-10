package com.example.onionstore.domain.product.repository;

import com.example.onionstore.domain.category.entity.Category;
import com.example.onionstore.domain.category.repository.CategoryRepository;
import com.example.onionstore.domain.product.entity.Product;
import com.example.onionstore.global.config.JpaConfig;
import com.example.onionstore.support.MysqlSupport;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Import({
        MysqlSupport.class,
        JpaConfig.class
})
@ActiveProfiles("test")
class ProductRepositoryTest {
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private CategoryRepository categoryRepository;

    private Category category;

    @BeforeEach
    void setUp() {
        category = new Category("test");
        categoryRepository.saveAndFlush(category);
    }

    @Test
    @DisplayName("삭제 처리된 상품 정보는 제외하고 상품 정보를 단일 조회한다.")
    void findByIdAndDeletedFalse() {
        //given
        Product product = Product.create(
                category,
                "name",
                "desc",
                10000,
                30
        );
        productRepository.saveAndFlush(product);

        //when
        Product found = productRepository.findByIdAndDeletedFalse(product.getId())
                .orElseThrow(RuntimeException::new);

        //then
        assertThat(found.isDeleted()).isFalse();
        assertThat(found.getName()).isEqualTo("name");
        assertThat(found.getDescription()).isEqualTo("desc");
        assertThat(found.getPrice()).isEqualTo(10000);
    }
}