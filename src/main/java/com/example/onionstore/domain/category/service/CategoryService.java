package com.example.onionstore.domain.category.service;

import com.example.onionstore.domain.category.dto.CategoryCreateRequest;
import com.example.onionstore.domain.category.dto.CategoryEditRequest;
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
        checkIfExists(createRequest.name());

        categoryRepository.save(new Category(createRequest.name()));
    }

    @Transactional(readOnly = true)
    public List<Category> getAllCategories() {
        return categoryRepository.findAllByDeletedFalse();
    }

    @Transactional
    public void editCategory(Long id, CategoryEditRequest editRequest) {
        Category toEdit = findCategory(id);

        if (toEdit.isDeleted()) {
            throw new BusinessException(ErrorCode.CATEGORY_NOT_FOUND);
        }

        checkIfExists(editRequest.newName());
        toEdit.changeName(editRequest.newName());

        categoryRepository.save(toEdit);
    }

    @Transactional(readOnly = true)
    private Category findCategory(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.CATEGORY_NOT_FOUND));
    }

    private void checkIfExists(String name) {
        if (categoryRepository.existsByName(name)) {
            throw new BusinessException(ErrorCode.DUPLICATE_CATEGORY);
        }
    }
  
    public Category getCategoryByName(String name) {
        return categoryRepository.findByName(name)
                .orElseThrow(() -> new BusinessException(ErrorCode.CATEGORY_NOT_FOUND));
    }
}
