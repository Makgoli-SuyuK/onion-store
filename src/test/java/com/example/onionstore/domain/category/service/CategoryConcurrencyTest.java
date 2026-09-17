package com.example.onionstore.domain.category.service;

import com.example.onionstore.domain.category.dto.CategoryEditRequest;
import com.example.onionstore.domain.category.entity.Category;
import com.example.onionstore.domain.category.repository.CategoryRepository;
import com.example.onionstore.domain.category.repository.cache.CategoryCache;
import com.example.onionstore.support.MysqlSupport;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Import(
        MysqlSupport.class
)
@ActiveProfiles("test")
public class CategoryConcurrencyTest {
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private CategoryRepository categoryRepository;
    @MockitoBean
    private CategoryCache categoryCache;

    Long categoryId;

    @BeforeEach
    public void setUp() {
        Category category = new Category("category");

        categoryId = categoryRepository.save(category).getId();
    }

    @AfterEach
    public void tearDown() {
        categoryRepository.deleteAll();
    }

    @Test
    @DisplayName("동시에 같은 카테고리를 수정하면 두 번째 트랜잭션은 첫 번째 트랜잭션이 끝날 때까지 대기한다.")
    void 카테고리_동시_수정_시_두_번째_트랜잭션은_첫_번째_트랜잭션이_끝날_때까지_대기한다() throws Exception {
        //given
        CategoryEditRequest editRequest = new CategoryEditRequest("edited");
        CategoryEditRequest secondEdit = new CategoryEditRequest("second");

        ExecutorService executorService = Executors.newFixedThreadPool(2);

        CountDownLatch start = new CountDownLatch(1);

        Future<?> first = executorService.submit(() -> {
            await(start);

            categoryService.editCategory(categoryId, editRequest);
        });

        Future<?> second = executorService.submit(() -> {
            await(start);

            categoryService.editCategory(categoryId, secondEdit);
        });

        //when
        start.countDown();

        first.get(5, TimeUnit.SECONDS);
        second.get(5, TimeUnit.SECONDS);

        executorService.shutdown();

        //then
        Category result = categoryRepository.findById(categoryId)
                .orElseThrow();

        assertThat(result.getName()).isIn("edited", "second");
    }

    private void await(CountDownLatch latch) {
        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }
}
