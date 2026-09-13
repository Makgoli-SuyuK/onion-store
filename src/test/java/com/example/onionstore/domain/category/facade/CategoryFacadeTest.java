package com.example.onionstore.domain.category.facade;

import com.example.onionstore.domain.category.dto.CategoryEditRequest;
import com.example.onionstore.domain.category.service.CategoryService;
import com.example.onionstore.domain.user.entity.Role;
import com.example.onionstore.domain.user.entity.User;
import com.example.onionstore.domain.user.service.UserService;
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
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CategoryFacadeTest {
    @Mock
    private CategoryService categoryService;
    @Mock
    private UserService userService;
    @InjectMocks
    private CategoryFacade categoryFacade;

    @Test
    @DisplayName("관리자 계정은 카테고리를 수정할 수 있다.")
    void 관리자_계정은_카테고리를_수정할_수_있다() {
        //given
        User user = new User(
                "test@email.com",
                "password",
                "name",
                "010-0000-0000",
                Role.ADMIN
        );

        CategoryEditRequest editRequest = new CategoryEditRequest(
                "newName"
        );

        given(userService.findUser(anyLong())).willReturn(user);

        //when
        categoryFacade.editCategory(1L, 1L, editRequest);

        //then
        verify(categoryService).editCategory(anyLong(), any(CategoryEditRequest.class));
    }

    @Test
    @DisplayName("관리자 계정이 아니라면 카테고리 수정 시도 시 예외를 던진다")
    void 관리자_계정이_아니라면_카테고리_수정_시_예외를_던진다() {
        //given
        User user = new User(
                "test@email.com",
                "password",
                "name",
                "010-0000-0000",
                Role.CUSTOMER
        );

        given(userService.findUser(anyLong())).willReturn(user);

        //when&then
        assertThatThrownBy(() -> categoryFacade.editCategory(1L, 1L, new CategoryEditRequest("newName")))
                .isInstanceOf(BusinessException.class)
                .hasMessage(ErrorCode.FORBIDDEN_ROLE.getMessage());
    }

    @Test
    @DisplayName("관리자 계정은 카테고리를 삭제할 수 있다")
    void 관리자_계정은_카테고리를_삭제할_수_있다() {
        //given
        User user = new User(
                "test@email.com",
                "password",
                "name",
                "010-0000-0000",
                Role.ADMIN
        );

        given(userService.findUser(anyLong())).willReturn(user);

        //when
        categoryFacade.deleteCategory(1L, 1L);

        //then
        verify(categoryService).deleteCategory(anyLong());
    }

    @Test
    @DisplayName("관리자 계정이 아니라면_카테고리를_삭제할_수_없다")
    void 관리자_계정이_아니라면_카테고리를_삭제할_수_없다() {
        //given
        User user = new User(
                "test@email.com",
                "password",
                "name",
                "010-0000-0000",
                Role.CUSTOMER
        );

        given(userService.findUser(anyLong())).willReturn(user);

        //when&then
        assertThatThrownBy(() -> categoryFacade.deleteCategory(1L, 1L))
                .isInstanceOf(BusinessException.class)
                .hasMessage(ErrorCode.FORBIDDEN_ROLE.getMessage());
    }
}