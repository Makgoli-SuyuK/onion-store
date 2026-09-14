package com.example.onionstore.domain.category.controller;

import com.example.onionstore.domain.category.dto.CategoryCreateRequest;
import com.example.onionstore.domain.category.dto.CategoryEditRequest;
import com.example.onionstore.domain.category.entity.Category;
import com.example.onionstore.domain.category.facade.CategoryFacade;
import com.example.onionstore.domain.category.service.CategoryService;
import com.example.onionstore.global.dto.ApiResponse;
import com.example.onionstore.global.exception.BusinessException;
import com.example.onionstore.global.exception.ErrorCode;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {
    private final CategoryService categoryService;
    private final CategoryFacade categoryFacade;

    @PostMapping
    public ResponseEntity<ApiResponse<Void>> addCategory(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody CategoryCreateRequest createRequest
    ) {
        Long userId = extractUserId(jwt);

        categoryFacade.addCategory(userId, createRequest);

        return ResponseEntity.ok(ApiResponse.success("카테고리 추가 성공", null));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Category>>> getAllCategories() {
        return ResponseEntity.ok(ApiResponse.success(categoryService.getAllCategories()));
    }

    @PatchMapping("/{categoryId}")
    public ResponseEntity<ApiResponse<Void>> editCategory(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long categoryId,
            @Valid @RequestBody CategoryEditRequest categoryEditRequest) {
        categoryFacade.editCategory(extractUserId(jwt), categoryId, categoryEditRequest);

        return ResponseEntity.ok(ApiResponse.success("카테고리 수정 성공", null));
    }

    @DeleteMapping("/{categoryId}")
    public ResponseEntity<ApiResponse<Void>> deleteCategory(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long categoryId) {
        categoryFacade.deleteCategory(extractUserId(jwt), categoryId);

        return ResponseEntity.ok(ApiResponse.success("카테고리 삭제 성공", null));
    }

    private Long extractUserId(Jwt jwt) {
        if (jwt == null || jwt.getSubject() == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        return Long.valueOf(jwt.getSubject());
    }
}
