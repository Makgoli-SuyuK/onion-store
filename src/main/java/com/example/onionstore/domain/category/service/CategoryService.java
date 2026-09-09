package com.example.onionstore.domain.category.service;

import com.example.onionstore.domain.category.dto.CategoryCreateRequest;
import com.example.onionstore.domain.category.entity.Category;
import com.example.onionstore.domain.category.repository.CategoryRepository;
import com.example.onionstore.global.exception.BusinessException;
import com.example.onionstore.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {
    private final CategoryRepository categoryRepository;

    @Transactional
    public void addCategory(CategoryCreateRequest createRequest) {
        if (categoryRepository.existsByName(createRequest.name())) {
            throw new BusinessException(ErrorCode.DUPLICATE_CATEGORY);
        }

        categoryRepository.save(new Category(createRequest.name()));
    }

    @Transactional(readOnly = true)
    public List<Category> getAllCategories() {
        return categoryRepository.findAllByDeletedFalse();
    }
      
    @Transactional(readOnly = true)
    public Category getCategoryByName(String name) {
        return categoryRepository.findByName(name)
                .orElseThrow(() -> new BusinessException(ErrorCode.CATEGORY_NOT_FOUND));
    }
}
