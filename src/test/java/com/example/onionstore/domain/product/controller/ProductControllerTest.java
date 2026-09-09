package com.example.onionstore.domain.product.controller;

import com.example.onionstore.domain.product.dto.ProductCreateRequest;
import com.example.onionstore.domain.product.service.ProductService;
import com.example.onionstore.global.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductController.class)
class ProductControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private ProductService productService;

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    @DisplayName("POST /products api로 상품을 생성한다.")
    void 상품_추가_api_테스트() throws Exception {
        //given
        ProductCreateRequest createRequest = new ProductCreateRequest(
                "product 1",
                "product1",
                "description",
                10000,
                10
        );

        //when&then
        mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(createRequest)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("spring validation 작동 확인 테스트")
    void spring_validation에서_걸리면_에러가_발생한다() throws Exception {
        //given
        ProductCreateRequest createRequest = new ProductCreateRequest(
                "",
                "product1",
                "description",
                10000,
                10
        );

        //when&then
        mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(createRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(ErrorCode.INVALID_INPUT_VALUE.getMessage()));
    }
}