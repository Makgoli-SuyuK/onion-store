package com.example.onionstore.domain.category.service;

import com.example.onionstore.domain.category.dto.CategoryCreateRequest;
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

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
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
        verify(categoryRepository) .save(any(Category.class));
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
}