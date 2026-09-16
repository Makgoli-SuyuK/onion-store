package com.example.onionstore.domain.product;

import com.example.onionstore.domain.category.entity.Category;
import com.example.onionstore.domain.category.repository.CategoryRepository;
import com.example.onionstore.domain.product.entity.ProductStatus;
import com.example.onionstore.domain.product.repository.ProductRepository;
import com.example.onionstore.domain.product.repository.dto.ProductSearchConditions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@SpringBootTest
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ProductIndexEfficiencyTest {
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private ProductRepository productRepository;

    private static final int MEASURE_COUNT = 100;

//    @BeforeAll
//    void setup() {
//        System.out.println("데이터 세팅 중");
//        insertProductData();
//        System.out.println("데이터 세팅 완료");
//    }

    @Test
    @DisplayName("가격 인덱스 시 가격 범위 검색 성능 테스트")
    void 가격_범위_검색_성능_테스트() {
        ProductSearchConditions conditions = new ProductSearchConditions(
                null, null, 100_000L, 500_000L, null
        );
        PageRequest pageable = PageRequest.of(0, 20, Sort.by(Sort.Direction.ASC, "price"));

        List<Double> withoutIndex = new ArrayList<>();
        List<Double> withIndex = new ArrayList<>();

        // 1. 인덱스 제거 후 측정
        dropIndex("idx_product_price");
        warmup(conditions, pageable);
        for (int i = 0; i < MEASURE_COUNT; i++) {
            withoutIndex.add(measureOnce(conditions, pageable));
        }

        // 2. 인덱스 생성 후 측정
        createIndex("CREATE INDEX idx_product_price ON products(price)");
        warmup(conditions, pageable);
        for (int i = 0; i < MEASURE_COUNT; i++) {
            withIndex.add(measureOnce(conditions, pageable));
        }

        printStatistics("WITHOUT INDEX", withoutIndex);
        printStatistics("WITH INDEX", withIndex);

        dropIndex("idx_product_price");
    }

    @Test
    @DisplayName("좋아요 수 인덱스 좋아요 수 검색 성능 테스트")
    void likeCountSearch() {

        ProductSearchConditions conditions =
                new ProductSearchConditions(
                        null,
                        null,
                        null,
                        null,
                        5_000
                );

        PageRequest pageable =
                PageRequest.of(
                        0,
                        20,
                        Sort.by(
                                Sort.Direction.DESC,
                                "likeCount"
                        )
                );

        List<Double> withoutIndex = new ArrayList<>();
        List<Double> withIndex = new ArrayList<>();

        // 1. 인덱스 제거 후 측정
        dropIndex("idx_product_like_count");
        warmup(conditions, pageable);
        for (int i = 0; i < MEASURE_COUNT; i++) {
            withoutIndex.add(measureOnce(conditions, pageable));
        }

        // 2. 인덱스 생성 후 측정
        createIndex("CREATE INDEX idx_product_like_count ON products(like_count)");
        warmup(conditions, pageable);
        for (int i = 0; i < MEASURE_COUNT; i++) {
            withIndex.add(measureOnce(conditions, pageable));
        }

        printStatistics("WITHOUT INDEX", withoutIndex);
        printStatistics("WITH INDEX", withIndex);

        dropIndex("idx_product_like_count");
    }

    @Test
    @DisplayName("카테고리 id 및 가격 인덱싱 성능 테스트")
    void categoryAndPriceSearch() {

        ProductSearchConditions conditions =
                new ProductSearchConditions(
                        "category 5",
                        null,
                        100_000L,
                        500_000L,
                        null
                );

        PageRequest pageable =
                PageRequest.of(
                        0,
                        20,
                        Sort.by(
                                Sort.Direction.ASC,
                                "price"
                        )
                );

        List<Double> withoutIndex = new ArrayList<>();
        List<Double> withIndex = new ArrayList<>();

        // 1. 인덱스 제거 후 측정
        dropIndex("idx_product_category_id_and_price");
        warmup(conditions, pageable);
        for (int i = 0; i < MEASURE_COUNT; i++) {
            withoutIndex.add(measureOnce(conditions, pageable));
        }

        // 2. 인덱스 생성 후 측정
        createIndex("CREATE INDEX idx_product_category_id_and_price ON products(category_id, price)");
        warmup(conditions, pageable);
        for (int i = 0; i < MEASURE_COUNT; i++) {
            withIndex.add(measureOnce(conditions, pageable));
        }

        printStatistics("WITHOUT INDEX", withoutIndex);
        printStatistics("WITH INDEX", withIndex);

        dropIndex("idx_product_category_id_and_price");
    }

    @Test
    @DisplayName("가격 및 카테고리 id 인덱싱 성능 테스트")
    void priceAndCategorySearch() {

        ProductSearchConditions conditions =
                new ProductSearchConditions(
                        "category 5",
                        null,
                        100_000L,
                        500_000L,
                        null
                );

        PageRequest pageable =
                PageRequest.of(
                        0,
                        20,
                        Sort.by(
                                Sort.Direction.ASC,
                                "price"
                        )
                );

        List<Double> withoutIndex = new ArrayList<>();
        List<Double> withIndex = new ArrayList<>();

        // 1. 인덱스 제거 후 측정
        dropIndex("idx_product_price_and_category_id");
        warmup(conditions, pageable);
        for (int i = 0; i < MEASURE_COUNT; i++) {
            withoutIndex.add(measureOnce(conditions, pageable));
        }

        // 2. 인덱스 생성 후 측정
        createIndex("CREATE INDEX idx_product_price_and_category_id ON products(price, category_id)");
        warmup(conditions, pageable);
        for (int i = 0; i < MEASURE_COUNT; i++) {
            withIndex.add(measureOnce(conditions, pageable));
        }

        printStatistics("WITHOUT INDEX", withoutIndex);
        printStatistics("WITH INDEX", withIndex);

        dropIndex("idx_product_price_and_category_id");
    }

    @Test
    @DisplayName("이름 인덱싱 성능 테스트")
    void nameContainsSearch() {

        ProductSearchConditions conditions =
                new ProductSearchConditions(
                        null,
                        "product 500",
                        null,
                        null,
                        null
                );

        PageRequest pageable =
                PageRequest.of(
                        0,
                        20
                );

        List<Double> withoutIndex = new ArrayList<>();
        List<Double> withIndex = new ArrayList<>();

        // 1. 인덱스 제거 후 측정
        dropIndex("idx_product_name");
        warmup(conditions, pageable);
        for (int i = 0; i < MEASURE_COUNT; i++) {
            withoutIndex.add(measureOnce(conditions, pageable));
        }

        // 2. 인덱스 생성 후 측정
        createIndex("CREATE INDEX idx_product_name ON products(name)");
        warmup(conditions, pageable);
        for (int i = 0; i < MEASURE_COUNT; i++) {
            withIndex.add(measureOnce(conditions, pageable));
        }

        printStatistics("WITHOUT INDEX", withoutIndex);
        printStatistics("WITH INDEX", withIndex);

        dropIndex("idx_product_name");
    }


    private void insertProductData() {
        int size = 500000;
        int batchSize = 10000;

        List<Category> categories = List.of(
                new Category("category1"),
                new Category("category2"),
                new Category("category3"),
                new Category("category4"),
                new Category("category5")
        );

        // 저장된 카테고리의 실제 ID 목록 추출
        List<Long> categoryIds = categoryRepository.saveAllAndFlush(categories)
                .stream()
                .map(Category::getId)
                .toList();

        String productQuery = """
            INSERT INTO products(
                category_id,
                name,
                description,
                price,
                stock,
                like_count,
                status,
                deleted,
                created_at,
                updated_at
            )
            VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

        LocalDateTime baseTime = LocalDateTime.now();

        for (int start = 0; start < size; start += batchSize) {
            int end = Math.min(start + batchSize, size);
            List<Object[]> batch = new ArrayList<>(end - start);

            for (int i = start; i < end; i++) {
                // 실제 저장된 Category ID를 순환 할당
                Long categoryId = categoryIds.get(i % categoryIds.size());

                String name = "product " + i;
                String description = "product " + i;
                long price = 10000L + (long) (Math.random() * 900000);
                int stock = 100;
                int likeCount = i % 10000;
                String status = ProductStatus.SELLING.name();
                boolean deleted = false;

                Timestamp now = Timestamp.valueOf(baseTime.minusDays(i % 365));

                batch.add(new Object[]{categoryId, name, description, price, stock, likeCount, status, deleted, now, now});
            }

            jdbcTemplate.batchUpdate(productQuery, batch);
            System.out.println(end + " inserted");
        }
    }

    private void warmup(ProductSearchConditions conditions, Pageable pageable) {
        for (int i = 0; i <= 10; i++) {
            productRepository.searchWithConditions(conditions, pageable);
        }
    }

    private double measureOnce(ProductSearchConditions conditions, Pageable pageable) {
        long start = System.nanoTime();

        productRepository.searchWithConditions(conditions, pageable);

        long end = System.nanoTime();

        return (end - start) /  1_000_000.0;
    }

    private void printStatistics(String name, List<Double> values) {
        List<Double> sorted =
                values.stream()
                        .sorted()
                        .toList();

        double average =
                values.stream()
                        .mapToDouble(Double::doubleValue)
                        .average()
                        .orElse(0);

        double min =
                sorted.get(0);

        double max =
                sorted.get(sorted.size() - 1);

        double p50 =
                percentile(sorted, 0.50);

        double p95 =
                percentile(sorted, 0.95);

        double p99 =
                percentile(sorted, 0.99);

        System.out.println();
        System.out.println("[" + name + "]");

        System.out.printf(
                "min : %.3f ms%n",
                min
        );

        System.out.printf(
                "avg : %.3f ms%n",
                average
        );

        System.out.printf(
                "p50 : %.3f ms%n",
                p50
        );

        System.out.printf(
                "p95 : %.3f ms%n",
                p95
        );

        System.out.printf(
                "p99 : %.3f ms%n",
                p99
        );

        System.out.printf(
                "max : %.3f ms%n",
                max
        );
    }

    private void createIndex(String sql) {
        jdbcTemplate.execute(sql);
    }

    private void dropIndex(String indexName) {
        try {
            jdbcTemplate.execute("DROP INDEX " + indexName + " ON products");
        } catch (Exception ex) {

        }
    }

    private double percentile(List<Double> sorted, double percentile) {
        int index = (int) Math.ceil(percentile * sorted.size()) - 1;

        return sorted.get(
                Math.max(0, index)
        );
    }
}
