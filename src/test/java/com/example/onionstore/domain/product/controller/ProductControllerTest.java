package com.example.onionstore.domain.product.controller;

import com.example.onionstore.domain.category.entity.Category;
import com.example.onionstore.domain.product.dto.ProductCreateRequest;
import com.example.onionstore.domain.product.dto.ProductResponse;
import com.example.onionstore.domain.product.entity.Product;
import com.example.onionstore.domain.product.facade.ProductFacade;
import com.example.onionstore.domain.product.dto.ProductSimpleResponse;
import com.example.onionstore.domain.product.facade.ProductFacade;
import com.example.onionstore.domain.product.repository.dto.ProductSearchConditions;
import com.example.onionstore.domain.product.service.ProductService;
import com.example.onionstore.global.exception.ErrorCode;
import com.example.onionstore.global.security.DeletedUserTokenFilter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import java.util.List;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        value = ProductController.class,
        excludeFilters = {
                @ComponentScan.Filter(
                        type = FilterType.ASSIGNABLE_TYPE,
                        classes = DeletedUserTokenFilter.class
                )
        }
)
class ProductControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private ProductService productService;
    @MockitoBean
    private ProductFacade productFacade;

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    @DisplayName("POST /api/products api로 상품을 생성한다.")
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
        mockMvc.perform(post("/api/products")
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
        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(createRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(ErrorCode.INVALID_INPUT_VALUE.getMessage()));
    }

    @Test
    @DisplayName("GET /api/products/{productId} 상품 단일 정보 조회 테스트")
    void 상품_단일_정보_조회_api_테스트() throws Exception {
        //given
        Product product = Product.create(
                new Category("name"),
                "name",
                "desc",
                1000,
                40
        );
        ReflectionTestUtils.setField(product, "id", 1L);

        given(productService.findById(anyLong())).willReturn(ProductResponse.from(product));

        //when&then
        mockMvc.perform(get("/api/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.name").value("name"))
                .andExpect(jsonPath("$.data.description").value("desc"));
    }

    @Test
    @DisplayName("GET /api/products - 동적 조건 검색 api 테스트")
    void 상품_동적_조건_검색_테스트() throws Exception {
        //given
        ProductSearchConditions conditions = new ProductSearchConditions(
                null,
                null,
                1000L,
                10000L,
                0,
                null,
                null,
                1,
                10
        );

        ProductSimpleResponse content = new ProductSimpleResponse(
                1L,
                "category",
                "name",
                1000L,
                50
        );

        Page<ProductSimpleResponse> res = new PageImpl<>(
                List.of(content),
                PageRequest.of(0, 10),
                1
        );

        given(productService.searchWithConditions(conditions, 1, 10))
                .willReturn(res);

        //when&then
        mockMvc.perform(get("/api/products")
                .param("priceStart", "1000")
                .param("priceEnd", "10000")
                .param("likeCount", "0")
                .param("page", "1")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content.[0].name").value("name"))
                .andExpect(jsonPath("$.data.content.[0].categoryName").value("category"));
    }
}