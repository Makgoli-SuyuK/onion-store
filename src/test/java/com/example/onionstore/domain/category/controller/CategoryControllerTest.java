package com.example.onionstore.domain.category.controller;

import com.example.onionstore.domain.category.dto.CategoryCreateRequest;
import com.example.onionstore.domain.category.service.CategoryService;
import com.example.onionstore.global.exception.BusinessException;
import com.example.onionstore.global.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
}