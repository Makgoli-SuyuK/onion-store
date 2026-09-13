package com.example.onionstore.domain.category.repository;

import com.example.onionstore.domain.category.CategoryFixture;
import com.example.onionstore.domain.category.entity.Category;
import com.example.onionstore.global.config.JpaConfig;
import com.example.onionstore.global.config.QuerydslConfig;
import com.example.onionstore.support.MysqlSupport;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import({
        JpaConfig.class,
        MysqlSupport.class,
        QuerydslConfig.class
})
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@ActiveProfiles("test")
public class CategoryRepositoryTest {
    @Autowired
    private CategoryRepository repository;
    @Autowired
    private TestEntityManager em;
    @Autowired
    private PlatformTransactionManager transactionManager;

    List<Long> categoryIds = new ArrayList<>();

    @BeforeEach
    void setup() {
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);


        transactionTemplate.executeWithoutResult(status -> {
            for (int i = 10; i > 0; i--) {
                categoryIds.add(repository.save(CategoryFixture.createCategory(i, i % 2 == 0))
                    .getId());
            }

            em.clear();
        });
    }

    @AfterEach
    void afterEach() {TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);


        transactionTemplate.executeWithoutResult(status -> {
            repository.deleteAll();
        });
    }

    @Test
    @DisplayName("deleted가 false인 데이터만 조회한다")
    void findAllByDeletedFalse_deleted가_false인_데이터만_조회한다() {
        //when
        List<Category> foundList = repository.findAllByDeletedFalse();

        //then
        foundList.forEach(category -> {
            System.out.println("Category: " + category.getName());
            System.out.println(category.isDeleted());
        });
        assertThat(foundList.size()).isEqualTo(5);
        assertThat(foundList.get(0).getName()).isEqualTo("category 1");
    }

    @Test
    @DisplayName("deleted가 false인 데이터만 존재하는지 확인한다")
    void existsByName이_deleted가_false인_데이터만_존재를_확인한다() {
        //when&then
        assertThat(repository.existsByName("category 1")).isTrue();
    }

    @Test
    @DisplayName("findForUpdateById 락 검증")
    void findForUpdateById는_락을_획득한다() throws Exception {
        long categoryId = categoryIds.get(0);

        ExecutorService executor = Executors.newFixedThreadPool(2);

        CountDownLatch firstLockAcquired = new CountDownLatch(1);
        CountDownLatch releaseFirstTransaction = new CountDownLatch(1);

        // 첫 번째 트랜잭션
        Future<?> first = executor.submit(() ->
                transactionTemplate().executeWithoutResult(status -> {
                    repository.findForUpdateById(categoryId);

                    // 여기까지 왔다는 것은 PESSIMISTIC_WRITE 락을 획득했다는 의미
                    firstLockAcquired.countDown();

                    // 두 번째 트랜잭션이 조회를 시도할 때까지 락 유지
                    await(releaseFirstTransaction);
                })
        );

        // 첫 번째 트랜잭션이 락을 획득할 때까지 대기
        assertThat(firstLockAcquired.await(5, TimeUnit.SECONDS))
                .isTrue();

        // 두 번째 트랜잭션
        CountDownLatch secondStarted = new CountDownLatch(1);
        CountDownLatch secondFinished = new CountDownLatch(1);

        Future<?> second = executor.submit(() ->
                transactionTemplate().executeWithoutResult((status -> {
                    secondStarted.countDown();

                    repository.findForUpdateById(categoryId);

                    // 첫 번째 트랜잭션이 락을 풀어준 후에 도달해야 함
                    secondFinished.countDown();
                }))
        );

        assertThat(secondStarted.await(5, TimeUnit.SECONDS))
                .isTrue();

        // 아직 첫 번째 트랜잭션이 락을 가지고 있으므로
        // 두 번째 트랜잭션은 조회를 완료하지 못해야 한다.
        assertThat(secondFinished.await(500, TimeUnit.MILLISECONDS))
                .isFalse();

        // 첫 번째 트랜잭션의 락 해제
        releaseFirstTransaction.countDown();

        // 두 번째 트랜잭션이 정상적으로 진행되는지 확인
        assertThat(secondFinished.await(5, TimeUnit.SECONDS))
                .isTrue();

        first.get(5, TimeUnit.SECONDS);
        second.get(5, TimeUnit.SECONDS);

        executor.shutdown();
    }

    private TransactionTemplate transactionTemplate() {
        return new TransactionTemplate(transactionManager);
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
