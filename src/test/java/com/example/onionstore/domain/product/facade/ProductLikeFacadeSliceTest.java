package com.example.onionstore.domain.product.facade;

import com.example.onionstore.domain.category.entity.Category;
import com.example.onionstore.domain.category.repository.CategoryRepository;
import com.example.onionstore.domain.product.entity.Product;
import com.example.onionstore.domain.product.repository.ProductLikeRepository;
import com.example.onionstore.domain.product.repository.ProductRepository;
import com.example.onionstore.domain.product.service.ProductLikeService;
import com.example.onionstore.domain.product.service.ProductService;
import com.example.onionstore.domain.user.entity.Role;
import com.example.onionstore.domain.user.entity.User;
import com.example.onionstore.domain.user.repository.UserRepository;
import com.example.onionstore.domain.user.service.UserService;
import com.example.onionstore.global.config.JpaConfig;
import com.example.onionstore.global.config.PasswordEncoderConfig;
import com.example.onionstore.global.config.QuerydslConfig;
import com.example.onionstore.support.MysqlSupport;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ContextConfiguration(classes={
        ProductLikeFacade.class,
        ProductLikeService.class,
        UserService.class,
        ProductService.class,
})
@EnableJpaRepositories(basePackages= "com.example.onionstore")
@EntityScan(basePackages = "com.example.onionstore")
@Import({
        MysqlSupport.class,
        QuerydslConfig.class,
        PasswordEncoderConfig.class,
        BCryptPasswordEncoder.class,
        JpaConfig.class
})
@ActiveProfiles("test")
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class ProductLikeFacadeSliceTest {
    @Autowired
    private ProductLikeFacade productLikeFacade;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private ProductLikeRepository productLikeRepository;

    private Long productId;
    private List<Long> userIds = new ArrayList<>();

    @BeforeEach
    void setUp() {
        Category category = new Category("category");
        categoryRepository.saveAndFlush(category);

        productId = productRepository.save(Product.create(
                category,
                "name",
                "description",
                10000L,
                10
        )).getId();

        for (int i = 0; i < 10; i++) {
            User user = userRepository.save(
                    new User(
                            "test" + i + "@email.com",
                            "password",
                            "name" + i,
                            "010-0000-0000",
                            Role.CUSTOMER
                    )
            );
            userRepository.saveAndFlush(user);

            userIds.add(user.getId());
        }
    }

    @AfterEach
    void tearDown() {
        productLikeRepository.deleteAllInBatch();
        productRepository.deleteAllInBatch();
        categoryRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();
    }

    @Test
    @DisplayName("Facade 슬라이스 환경에서 락 동작 및 동시성 검증")
    void facade_슬라이스_동시성_및_락_검증() throws Exception {
        //given
        int threadCount = 10;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch countDownLatch = new CountDownLatch(threadCount);

        //when
        for (int i = 0; i < threadCount; i++) {
            System.out.println("USERID: " + userIds.get(i));
            Long userId = userIds.get(i);

            executorService.execute(() -> {
                try {
                    productLikeFacade.toggleLike(userId, productId);
                } finally {
                    countDownLatch.countDown();
                }
            });
        }

        countDownLatch.await();

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new Exception("Product not found"));

        //then
        assertThat(product.getLikeCount()).isEqualTo(10);
    }
}
