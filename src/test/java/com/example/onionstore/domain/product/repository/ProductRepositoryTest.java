package com.example.onionstore.domain.product.repository;

import com.example.onionstore.domain.category.entity.Category;
import com.example.onionstore.domain.category.repository.CategoryRepository;
import com.example.onionstore.domain.product.ProductFixture;
import com.example.onionstore.domain.product.dto.ProductSimpleResponse;
import com.example.onionstore.domain.product.entity.Product;
import com.example.onionstore.domain.product.repository.dto.ProductSearchConditions;
import com.example.onionstore.global.config.JpaConfig;
import com.example.onionstore.global.config.QuerydslConfig;
import com.example.onionstore.support.MysqlSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import({
        MysqlSupport.class,
        JpaConfig.class,
        QuerydslConfig.class
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

    @Test
    @DisplayName("조건으로 상품 정보를 검색할 수 있다.")
    void 조건으로_상품을_검색해서_정보를_반환한다() {
        //given
        Category category1 = new Category("category1");
        Category category2 = new Category("category2");
        categoryRepository.saveAllAndFlush(List.of(category1, category2));

        List<Product> products = new ArrayList<>();

        for (int i = 1; i <= 10; i++) {
            if (i % 2 == 0) {
                products.add(ProductFixture.createProduct(i, category1));
            } else {
                products.add(ProductFixture.createProduct(i, category2));
            }
        }

        for (int i = 11; i <= 15; i++) {
            Product product = ProductFixture.createProduct(i, category1);
            product.markAsDeleted();
        }
        productRepository.saveAllAndFlush(products);

        ProductSearchConditions conditions = new ProductSearchConditions(
                category1.getName(),
                "name",
                0L,
                1000000L,
                0,
                "price",
                "asc",
                1,
                10
        );

        //when
        PageRequest pageRequest = PageRequest.of(0, 10);
        Page<ProductSimpleResponse> res = productRepository.searchWithConditions(conditions, pageRequest);

        res.getContent().forEach(product -> {
            System.out.println("id: " + product.id());
            System.out.println("category: " + product.categoryName());
        });

        //then
        assertThat(res.getContent()).hasSize(5);
        assertThat(res.getTotalElements()).isEqualTo(5);
        assertThat(res.getTotalPages()).isEqualTo(1);

        assertThat(res.getContent())
                .extracting(ProductSimpleResponse::categoryName)
                .containsOnly("category1");

        assertThat(res.getContent())
                .extracting(ProductSimpleResponse::price)
                .isSorted();

        assertThat(res.getContent())
                .allSatisfy(product -> {
                    assertThat(product.name()).contains("name");
                    assertThat(product.price()).isBetween(0L, 1000000L);
                });
    }

    @Test
    @DisplayName("일부 조건으로 상품 정보를 검색할 수 있다.")
    void 일부_조건으로_상품을_검색해서_정보를_반환한다() {
        //given
        Category category1 = new Category("category1");
        Category category2 = new Category("category2");
        categoryRepository.saveAllAndFlush(List.of(category1, category2));

        List<Product> products = new ArrayList<>();

        for (int i = 1; i <= 10; i++) {
            if (i % 2 == 0) {
                products.add(ProductFixture.createProduct(i, category1));
            } else {
                products.add(ProductFixture.createProduct(i, category2));
            }
        }
        productRepository.saveAllAndFlush(products);

        ProductSearchConditions conditions = new ProductSearchConditions(
                null,
                null,
                0L,
                5000L,
                0,
                "price",
                "desc",
                1,
                10
        );

        //when
        PageRequest pageRequest = PageRequest.of(0, 10);
        Page<ProductSimpleResponse> res = productRepository.searchWithConditions(conditions, pageRequest);

        //then
        assertThat(res.getContent()).hasSize(5);
        assertThat(res.getTotalElements()).isEqualTo(5);
        assertThat(res.getTotalPages()).isEqualTo(1);

        assertThat(res.getContent())
                .extracting(ProductSimpleResponse::price)
                .isSortedAccordingTo(Comparator.reverseOrder());

        assertThat(res.getContent())
                .allSatisfy(product -> {
                    assertThat(product.price()).isBetween(0L, 5000L);
                });
    }

    @Test
    @DisplayName("좋아요 순으로 상품을 정렬해 상위 10개만 반환한다")
    void 좋아요_순으로_정렬해_상위_10개만_반환한다() {
        //given
        List<Product> products = new ArrayList<>();

        for (int i = 1; i <= 40; i++) {
            products.add(ProductFixture.createProduct(i, category));
        }
        productRepository.saveAllAndFlush(products);

        //when
        List<Product> res = productRepository.find10OrderByLikeCountDesc();

        //then
        assertThat(res.size()).isEqualTo(10);
        assertThat(res.get(0).getLikeCount()).isEqualTo(40);
        assertThat(res)
                .extracting(Product::getLikeCount)
                .isSortedAccordingTo(Comparator.reverseOrder());
    }
}