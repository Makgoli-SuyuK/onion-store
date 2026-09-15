package com.example.onionstore.domain.category.service;

import com.example.onionstore.domain.category.dto.CategoryCreateRequest;
import com.example.onionstore.domain.category.dto.CategoryEditRequest;
import com.example.onionstore.domain.category.entity.Category;
import com.example.onionstore.domain.category.repository.CategoryRepository;
import com.example.onionstore.domain.category.repository.cache.CategoryCache;
import com.example.onionstore.domain.product.service.ProductService;
import com.example.onionstore.global.config.CacheConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@Import(CacheConfig.class)
public class CategoryCacheTest {
    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private ProductService productService;
    @Mock
    private CategoryCache categoryCache;
    @InjectMocks
    private CategoryService categoryService;

    private Category category;

    @BeforeEach
    public void setUp() {
        category = new Category("category");
    }

    @Test
    @DisplayName("캐시가 존재하면 repository를 조회하지 않는다.")
    void 캐시가_존재하면_DB를_조회하지_않는다() {
        //given
        List<Category> cached = List.of(category);

        given(categoryCache.get()).willReturn(cached);

        //when
        List<Category> result = categoryService.getAllCategories();

        //then
        verify(categoryRepository, never()).findAllByDeletedFalse();
        verify(categoryCache).get();
        verify(categoryCache, never()).put(any());
        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("캐시에 없다면 repository에서 조회한다.")
    void 캐시에_없다면_repository에서_조회한다() {
        //given
        List<Category> list = List.of(category);

        given(categoryCache.get()).willReturn(null);
        given(categoryRepository.findAllByDeletedFalse()).willReturn(list);

        //when
        List<Category> result = categoryService.getAllCategories();

        //then
        verify(categoryRepository).findAllByDeletedFalse();
        verify(categoryCache).get();
        verify(categoryCache).put(any());
        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("카테고리를 추가하면 캐시를 삭제한다")
    void 카테고리를_추가하면_캐시를_삭제한다() {
        //given
        CategoryCreateRequest createRequest = new CategoryCreateRequest("category");

        given(categoryRepository.existsByName("category")).willReturn(false);

        //when
        categoryService.addCategory(createRequest);

        //then
        verify(categoryRepository).save(any(Category.class));
        verify(categoryCache).evict();
    }

    @Test
    @DisplayName("카테고리를 수정하면 캐시를 삭제한다")
    void 카테고리를_수정하면_캐시를_삭제한다() {
        //given
        CategoryEditRequest editRequest = new CategoryEditRequest("edit");

        given(categoryRepository.findForUpdateById(anyLong())).willReturn(Optional.of(category));
        given(categoryRepository.existsByName("edit")).willReturn(false);

        //when
        categoryService.editCategory(1L, editRequest);

        //then
        verify(categoryRepository).existsByName(anyString());
        verify(categoryRepository).save(any(Category.class));
        verify(categoryCache).evict();
    }

    @Test
    @DisplayName("카테고리를 삭제하면 캐시를 삭제한다")
    void 카테고리를_삭제하면_캐시를_삭제한다() {
        //given
        given(categoryRepository.findForUpdateById(anyLong())).willReturn(Optional.of(category));

        //when
        categoryService.deleteCategory(1L);

        //then
        verify(categoryCache).evict();
    }

    @Test
    @DisplayName("카테고리 이름으로 조회할 때 캐시를 사용한다")
    void 카테고리_이름으로_조회할_때_캐시를_사용한다() {
        //given
        List<Category> list = List.of(category);

        given(categoryCache.get()).willReturn(list);

        //when
        Category result = categoryService.getCategoryByName("category");

        //then
        assertThat(result).isEqualTo(category);

        verify(categoryCache).get();
        verify(categoryRepository, never()).findAllByDeletedFalse();
    }
}
