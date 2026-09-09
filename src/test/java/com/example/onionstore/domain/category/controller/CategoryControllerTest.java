package com.example.onionstore.domain.category.controller;

import com.example.onionstore.domain.category.dto.CategoryCreateRequest;
import com.example.onionstore.domain.category.dto.CategoryEditRequest;
import com.example.onionstore.domain.category.entity.Category;
import com.example.onionstore.domain.category.service.CategoryService;
import com.example.onionstore.global.exception.BusinessException;
import com.example.onionstore.global.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.willThrow;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CategoryController.class)
class CategoryControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private CategoryService categoryService;

    private ObjectMapper mapper = new ObjectMapper();

    @Test
    @DisplayName("POST /api/categories api - 카테고리 추가 테스트")
    void 카테고리를_추가할_수_있다() throws Exception {
        //given
        CategoryCreateRequest createRequest = new CategoryCreateRequest(
                "name"
        );

        //when&then
        mockMvc.perform(post("/api/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(createRequest)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /api/categories api - 이미 존재하는 카테고리 이름")
    void 이미_존재하는_카테고리_이름으로_카테고리를_추가하면_409_에러가_발생한다() throws Exception {
        //given
        CategoryCreateRequest createRequest = new CategoryCreateRequest(
                "exists"
        );

        willThrow(new BusinessException(ErrorCode.DUPLICATE_CATEGORY))
                .given(categoryService)
                .addCategory(createRequest);

        //when&then
        mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(createRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(ErrorCode.DUPLICATE_CATEGORY.getMessage()));
    }

    @Test
    @DisplayName("POST /api/categories api - 입력값 검증 실패")
    void 카테고리_추가_시_이름이_공백일_수_없다() throws Exception {
        //given
        CategoryCreateRequest createRequest = new CategoryCreateRequest(
                ""
        );

        //when&then
        mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(createRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(ErrorCode.INVALID_INPUT_VALUE.getMessage()));
    }

    @Test
    @DisplayName("GET /api/categories api - 모든 카테고리 리스트 조회")
    void 모든_카테고리_정보를_조회한다() throws Exception {
        //given
        List<Category> list = new ArrayList<>();
        list.add(new Category("name1"));
        list.add(new Category("name2"));

        given(categoryService.getAllCategories()).willReturn(list);

        //when&then
        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("data.[0].name").value("name1"))
                .andExpect(jsonPath("data.[1].name").value("name2"));
    }

    @Test
    @DisplayName("PATCH /api/categories/{categoryId} api - 카테고리 수정 테스트")
    void 카테고리_이름을_변경한다() throws Exception {
        //given
        Category category = new Category("new category");
        ReflectionTestUtils.setField(category, "id", 1L);

        CategoryEditRequest editRequest = new CategoryEditRequest("edited");

        //when&then
        mockMvc.perform(patch("/api/categories/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(editRequest)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PATCH /api/categories/{categoryId} api - 카테고리 수정 시 같은 이름이 이미 존재하면 409 에러를 반환")
    void 카테고리_이름이_이미_존재한다면_에러를_반환한다() throws Exception {
        //given
        Category category = new Category("new category");
        ReflectionTestUtils.setField(category, "id", 1L);

        CategoryEditRequest editRequest = new CategoryEditRequest("edited");
        willThrow(new BusinessException(ErrorCode.DUPLICATE_CATEGORY))
                .given(categoryService)
                .editCategory(1L, editRequest);

        //when&then
        mockMvc.perform(patch("/api/categories/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(editRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(ErrorCode.DUPLICATE_CATEGORY.getMessage()));
    }

    @Test
    @DisplayName("DELETE /api/categories/{categoryId} api - 카테고리 삭제 테스트")
    void 카테고리_삭제_테스트() throws Exception {
        //given
        Category category = new Category("new category");
        ReflectionTestUtils.setField(category, "id", 1L);

        //when&then
        mockMvc.perform(delete("/api/categories/1"))
                .andExpect(status().isOk());
    }
}