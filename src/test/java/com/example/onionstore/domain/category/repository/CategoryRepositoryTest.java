package com.example.onionstore.domain.category.repository;

import com.example.onionstore.domain.category.CategoryFixture;
import com.example.onionstore.domain.category.entity.Category;
import com.example.onionstore.global.config.JpaConfig;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import({
        JpaConfig.class
})
public class CategoryRepositoryTest {
    @Autowired
    private CategoryRepository repository;

    @BeforeEach
    void setup() {
        for (int i = 10; i > 0; i--) {
            repository.save(CategoryFixture.createCategory(i, i % 2 == 0));
        }
    }

    @AfterEach
    void afterEach() {
        repository.deleteAll();
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
}
