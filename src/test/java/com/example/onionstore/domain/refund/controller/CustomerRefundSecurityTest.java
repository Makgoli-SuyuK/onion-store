package com.example.onionstore.domain.refund.controller;

import com.example.onionstore.domain.user.entity.Role;
import com.example.onionstore.domain.user.entity.User;
import com.example.onionstore.domain.user.repository.UserRepository;
import com.example.onionstore.global.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

@SpringBootTest
@AutoConfigureMockMvc
class CustomerRefundSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    private Long adminId;

    @BeforeEach
    void setUp() {
        User admin = userRepository.save(new User(
                "refund-" + UUID.randomUUID().toString().substring(0, 8) + "@onion.store",
                "password",
                "환불보안관리자",
                "01000000000",
                Role.ADMIN
        ));
        adminId = admin.getId();
    }

    @Test
    @DisplayName("인증 없이 고객 환불 요청 API를 호출하면 공통 401 응답을 반환한다")
    void 인증_없이_환불요청을_호출하면_401을_반환한다() throws Exception {
        mockMvc.perform(post("/api/orders/1/refunds")
                        .contentType("application/json")
                        .content("{\"reason\":\"단순 변심\",\"items\":[{\"orderItemId\":1,\"quantity\":1}]}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(ErrorCode.UNAUTHORIZED.getCode()));
    }

    @Test
    @DisplayName("관리자는 고객 환불 요청 API를 호출할 수 없다")
    void 관리자는_고객_환불요청을_호출할수없다() throws Exception {
        mockMvc.perform(post("/api/orders/1/refunds")
                        .with(jwt().jwt(token -> token.subject(String.valueOf(adminId)))
                                .authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                        .contentType("application/json")
                        .content("{\"reason\":\"단순 변심\",\"items\":[{\"orderItemId\":1,\"quantity\":1}]}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(ErrorCode.FORBIDDEN_ROLE.getCode()));
    }

    @Test
    @DisplayName("관리자는 고객 환불 목록 API를 호출할 수 없다")
    void 관리자는_고객_환불목록을_조회할수없다() throws Exception {
        mockMvc.perform(get("/api/refunds")
                        .with(jwt().jwt(token -> token.subject(String.valueOf(adminId)))
                                .authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(ErrorCode.FORBIDDEN_ROLE.getCode()));
    }
}
