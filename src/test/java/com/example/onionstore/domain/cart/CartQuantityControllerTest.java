package com.example.onionstore.domain.cart;

import com.example.onionstore.domain.cart.controller.CartController;
import com.example.onionstore.domain.cart.facade.CartFacade;
import com.example.onionstore.global.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CartQuantityControllerTest {
    private CartFacade cartFacade;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        cartFacade = mock(CartFacade.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new CartController(cartFacade))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .build();

        // 요청 검증 테스트이므로 JWT 인증이 완료된 상황을 설정한다.
        Jwt jwt = Jwt.withTokenValue("test-token")
                .header("alg", "HS256")
                .subject("1")
                .build();
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(jwt));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @ParameterizedTest
    @ValueSource(strings = {"{\"quantity\":0}", "{\"quantity\":-1}"})
    void rejectsInvalidQuantityBeforeCallingFacade(String body) throws Exception {
        assertQuantityValidationFailure(body, "수량은 1개 이상이어야 합니다.");
    }

    @ParameterizedTest
    @ValueSource(strings = {"{}", "{\"quantity\":null}"})
    void rejectsMissingQuantityBeforeCallingFacade(String body) throws Exception {
        mockMvc.perform(patch("/api/carts/items/{cartItemId}", 10L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("COMMON_003"))
                .andExpect(jsonPath("$.message").value("요청 본문 형식이 올바르지 않습니다."))
                .andExpect(jsonPath("$.data").value(org.hamcrest.Matchers.nullValue()));

        verifyNoInteractions(cartFacade);
    }

    private void assertQuantityValidationFailure(String body, String expectedMessage) throws Exception {
        mockMvc.perform(patch("/api/carts/items/{cartItemId}", 10L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("COMMON_002"))
                .andExpect(jsonPath("$.message").value("입력값을 확인해주세요."))
                .andExpect(jsonPath("$.data.quantity").value(expectedMessage));

        verifyNoInteractions(cartFacade);
    }
}
