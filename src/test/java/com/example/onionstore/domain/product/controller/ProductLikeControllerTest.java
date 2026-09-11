package com.example.onionstore.domain.product.controller;

import com.example.onionstore.config.TestWebSecurityConfig;
import com.example.onionstore.domain.product.facade.ProductLikeFacade;
import com.example.onionstore.global.security.DeletedUserTokenFilter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        value = ProductLikeController.class,
        excludeFilters = {
                @ComponentScan.Filter(
                        type = FilterType.ASSIGNABLE_TYPE,
                        classes = DeletedUserTokenFilter.class
                )
        }
)
@Import(TestWebSecurityConfig.class)
class ProductLikeControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private ProductLikeFacade facade;

    @Test
    @DisplayName("POST /api/products/{productId}/like")
    void 상품_좋아요_추가_api_테스트() throws Exception {
        //when&then
        mockMvc.perform(post("/api/products/1/like")
                .with(jwt().jwt(jwt -> jwt
                        .tokenValue("mockToken")
                        .subject("1"))
                        .authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))
                ))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("상품에 좋아요를 추가했습니다."));
        verify(facade).likeProduct(anyLong(), anyLong());
    }
}