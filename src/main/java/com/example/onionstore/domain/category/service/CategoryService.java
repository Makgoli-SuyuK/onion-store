package com.example.onionstore.domain.category.service;

import com.example.onionstore.domain.category.dto.CategoryCreateRequest;
import com.example.onionstore.domain.category.dto.CategoryEditRequest;
import com.example.onionstore.domain.category.entity.Category;
import com.example.onionstore.domain.category.repository.CategoryRepository;
import com.example.onionstore.domain.category.repository.cache.CategoryCache;
import com.example.onionstore.domain.product.service.ProductService;
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
    private final ProductService productService;
    private final CategoryCache categoryCache;

    @Transactional
    public void addCategory(CategoryCreateRequest createRequest) {
        checkIfExists(createRequest.name());

        categoryRepository.save(new Category(createRequest.name()));
        categoryCache.evict();
    }

    @Transactional(readOnly = true)
    public List<Category> getAllCategories() {
        List<Category> categories = categoryCache.get();

        if (categories != null) {
            return categories;
        }

        categories = categoryRepository.findAllByDeletedFalse();
        categoryCache.put(categories);
        return categories;
    }

    @Transactional
    public void editCategory(Long id, CategoryEditRequest editRequest) {
        Category toEdit = findCategoryForUpdate(id);

        if (toEdit.isDeleted()) {
            throw new BusinessException(ErrorCode.CATEGORY_NOT_FOUND);
        }

        checkIfExists(editRequest.newName());
        toEdit.changeName(editRequest.newName());

        categoryRepository.save(toEdit);
        categoryCache.evict();
    }

    @Transactional
    public void deleteCategory(Long id) {
        Category toDelete = findCategoryForUpdate(id);

        if (toDelete.isDeleted()) {
            throw new BusinessException(ErrorCode.CATEGORY_ALREADY_DELETED);
        }

        if (productService.existsByCategoryId(id)) {
            throw new BusinessException(ErrorCode.CATEGORY_ITEM_EXISTS);
        }

        toDelete.markAsDeleted();
        categoryRepository.save(toDelete);
        categoryCache.evict();
    }

    @Transactional(readOnly = true)
    public Category getCategoryByName(String name) {
        return getAllCategories().stream()
                .filter(c -> c.getName().equals(name))
                .findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.CATEGORY_NOT_FOUND));
    }

    private Category findCategoryForUpdate(Long id) {
        return categoryRepository.findForUpdateById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.CATEGORY_NOT_FOUND));
    }

    private void checkIfExists(String name) {
        if (categoryRepository.existsByName(name)) {
            throw new BusinessException(ErrorCode.DUPLICATE_CATEGORY);
        }
    }
}
