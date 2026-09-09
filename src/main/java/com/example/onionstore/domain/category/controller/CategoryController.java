package com.example.onionstore.domain.category.controller;

import com.example.onionstore.domain.category.dto.CategoryCreateRequest;
import com.example.onionstore.domain.category.dto.CategoryEditRequest;
import com.example.onionstore.domain.category.entity.Category;
import com.example.onionstore.domain.category.service.CategoryService;
import com.example.onionstore.global.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {
    private final CategoryService categoryService;

    @PostMapping
    public ResponseEntity<ApiResponse<Void>> addCategory(@Valid @RequestBody CategoryCreateRequest createRequest) {
        categoryService.addCategory(createRequest);

        return ResponseEntity.ok(ApiResponse.success("카테고리 추가 성공", null));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Category>>> getAllCategories() {
        return ResponseEntity.ok(ApiResponse.success(categoryService.getAllCategories()));
    }

    @PatchMapping("/{categoryId}")
    public ResponseEntity<ApiResponse<Void>> editCategory(@PathVariable Long categoryId,
                                          @Valid @RequestBody CategoryEditRequest categoryEditRequest) {
        categoryService.editCategory(categoryId, categoryEditRequest);

        return ResponseEntity.ok(ApiResponse.success("카테고리 수정 성공", null));
    }

    @DeleteMapping("/{categoryId}")
    public ResponseEntity<ApiResponse<Void>> deleteCategory(@PathVariable Long categoryId) {
        categoryService.deleteCategory(categoryId);

        return ResponseEntity.ok(ApiResponse.success("카테고리 삭제 성공", null));
    }
}
