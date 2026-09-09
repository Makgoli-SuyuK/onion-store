package com.example.onionstore.domain.category.service;

import com.example.onionstore.domain.category.dto.CategoryCreateRequest;
import com.example.onionstore.domain.category.dto.CategoryEditRequest;
import com.example.onionstore.domain.category.entity.Category;
import com.example.onionstore.domain.category.repository.CategoryRepository;
import com.example.onionstore.global.exception.BusinessException;
import com.example.onionstore.global.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {
    @Mock
    private CategoryRepository categoryRepository;
    @InjectMocks
    private CategoryService categoryService;

    @Test
    @DisplayName("카테고리 추가 테스트")
    void 카테고리_추가_성공() {
        //given
        CategoryCreateRequest createRequest = new CategoryCreateRequest(
                "new category"
        );

        given(categoryRepository.save(any(Category.class)))
                .willReturn(new Category(createRequest.name()));

        //when
        categoryService.addCategory(createRequest);

        //then
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    @DisplayName("이미 존재하는 카테고리 이름을 추가 시도하면 에러 발생")
    void 이미_존재하는_카테고리_이름을_추가하면_에러가_발생한다() {
        //given
        CategoryCreateRequest createRequest = new CategoryCreateRequest(
                "exists category"
        );

        given(categoryRepository.existsByName(createRequest.name()))
                .willReturn(true);

        //when&then
        assertThatThrownBy(() -> categoryService.addCategory(createRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessage(ErrorCode.DUPLICATE_CATEGORY.getMessage());
    }

    @Test
    @DisplayName("카테고리 전체 조회 테스트")
    void 카테고리_전체_데이터를_조회한다() {
        //given
        List<Category> categories = new ArrayList<>();
        categories.add(new Category("new category1"));
        categories.add(new Category("new category2"));

        given(categoryRepository.findAllByDeletedFalse()).willReturn(categories);

        //when
        List<Category> list = categoryService.getAllCategories();

        //then
        assertEquals(list.size(), categories.size());
        assertEquals(list.get(0), categories.get(0));
        assertEquals(list.get(1), categories.get(1));
    }

    @Test
    @DisplayName("카테고리 수정 테스트")
    void 카테고리_수정_테스트() {
        //given
        Category category = new Category("new category");
        ReflectionTestUtils.setField(category, "id", 1L);

        CategoryEditRequest editRequest = new CategoryEditRequest("edited");

        given(categoryRepository.findById(1L)).willReturn(Optional.of(category));
        given(categoryRepository.save(any())).willReturn(category);

        //when
        categoryService.editCategory(1L, editRequest);

        //then
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    @DisplayName("카테고리 수정 테스트 - 이미 존재하는 이름으로 수정 시도 시 에러 발생")
    void 카테고리_수정_시_이미_존재하는_이름이면_에러가_발생한다() {
        //given
        Category category = new Category("new category");
        ReflectionTestUtils.setField(category, "id", 1L);

        CategoryEditRequest editRequest = new CategoryEditRequest("exists");

        given(categoryRepository.findById(1L)).willReturn(Optional.of(category));
        given(categoryRepository.existsByName(anyString())).willReturn(Boolean.TRUE);

        //when&then
        assertThatThrownBy(() -> categoryService.editCategory(1L, editRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessage(ErrorCode.DUPLICATE_CATEGORY.getMessage());
    }
}