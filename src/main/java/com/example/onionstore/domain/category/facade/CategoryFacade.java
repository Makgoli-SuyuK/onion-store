package com.example.onionstore.domain.category.facade;

import com.example.onionstore.domain.category.dto.CategoryEditRequest;
import com.example.onionstore.domain.category.service.CategoryService;
import com.example.onionstore.domain.user.entity.Role;
import com.example.onionstore.domain.user.entity.User;
import com.example.onionstore.domain.user.service.UserService;
import com.example.onionstore.global.exception.BusinessException;
import com.example.onionstore.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CategoryFacade {
    private final CategoryService categoryService;
    private final UserService userService;

    public void editCategory(Long userId, Long categoryId, CategoryEditRequest categoryEditRequest){
        checkAdmin(userId);

        categoryService.editCategory(categoryId, categoryEditRequest);
    }

    public void deleteCategory(Long userId, Long categoryId) {
        checkAdmin(userId);

        categoryService.deleteCategory(categoryId);
    }

    private void checkAdmin(Long userId) {
        User user = userService.findUser(userId);
        if (user.getRole() != Role.ADMIN) {
            throw new BusinessException(ErrorCode.FORBIDDEN_ROLE);
        }
    }
}
