package com.example.onionstore.domain.user;

import com.example.onionstore.domain.user.controller.AuthController;
import com.example.onionstore.domain.user.dto.SignupRequest;
import com.example.onionstore.domain.user.entity.Role;
import com.example.onionstore.domain.user.entity.UserStatus;
import com.example.onionstore.domain.user.repository.UserRepository;
import com.example.onionstore.domain.user.service.AuthService;
import com.example.onionstore.global.exception.BusinessException;
import com.example.onionstore.global.exception.ErrorCode;
import com.example.onionstore.global.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import java.util.concurrent.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = "spring.jpa.open-in-view=false")
class SignupIntegrationTest {
    @Autowired AuthController controller;
    @Autowired AuthService service;
    @Autowired UserRepository users;
    @Autowired PasswordEncoder encoder;
    @Autowired JdbcTemplate jdbc;
    @Autowired LocalValidatorFactoryBean validator;
    MockMvc mvc;

    @BeforeEach
    void setup() {
        users.deleteAll();
        mvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler()).setValidator(validator).build();
    }

    private String body(String password) {
        return """
                {"email":"customer@example.com","password":"%s","name":"고객","phoneNumber":"01012345678",
                 "role":"ADMIN","status":"DELETED"}
                """.formatted(password);
    }

    @Test
    void signupHashesPasswordAndIgnoresClientRoleAndStatus() throws Exception {
        mvc.perform(post("/api/auth/signup").contentType(MediaType.APPLICATION_JSON).content(body("a")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.userId").isNumber())
                .andExpect(jsonPath("$.data.password").doesNotExist());
        var user = users.findAll().get(0);
        assertThat(user.getRole()).isEqualTo(Role.CUSTOMER);
        assertThat(user.getStatus()).isEqualTo(UserStatus.ACTIVE);
        assertThat(user.getPassword()).isNotEqualTo("a");
        assertThat(encoder.matches("a", user.getPassword())).isTrue();
    }

    @Test
    void duplicateIncludingDeletedMemberIsRejected() throws Exception {
        service.signup(new SignupRequest("customer@example.com", "a", "고객", "01012345678"));
        for (String state : new String[]{"ACTIVE", "DELETED"}) {
            jdbc.update("update users set status = ?", state);
            mvc.perform(post("/api/auth/signup").contentType(MediaType.APPLICATION_JSON).content(body("b")))
                    .andExpect(status().isConflict()).andExpect(jsonPath("$.code").value("MEMBER_002"));
        }
        assertThat(users.count()).isEqualTo(1);
    }

    @Test
    void passwordValidationUsesUtf8Bytes() throws Exception {
        for (String password : new String[]{"", "a".repeat(73), "가".repeat(25)}) {
            mvc.perform(post("/api/auth/signup").contentType(MediaType.APPLICATION_JSON).content(body(password)))
                    .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("COMMON_002"));
        }
        mvc.perform(post("/api/auth/signup").contentType(MediaType.APPLICATION_JSON)
                        .content(body("a").replace("\"password\":\"a\",", "")))
                .andExpect(status().isBadRequest());
        for (String password : new String[]{"a".repeat(72), "가".repeat(24)}) {
            users.deleteAll();
            mvc.perform(post("/api/auth/signup").contentType(MediaType.APPLICATION_JSON).content(body(password)))
                    .andExpect(status().isCreated());
            assertThat(encoder.matches(password, users.findAll().get(0).getPassword())).isTrue();
        }
    }

    @Test
    void invalidEmailAndOversizedFieldsAreRejected() throws Exception {
        for (String invalidBody : new String[]{
                body("a").replace("customer@example.com", "invalid"),
                body("a").replace("customer@example.com", "a".repeat(40) + "@example.com"),
                body("a").replace("고객", "가".repeat(31)),
                body("a").replace("01012345678", "1".repeat(21))}) {
            mvc.perform(post("/api/auth/signup").contentType(MediaType.APPLICATION_JSON).content(invalidBody))
                    .andExpect(status().isBadRequest());
        }
        assertThat(users.count()).isZero();
    }

    @Test
    void concurrentSignupCreatesOnlyOneMember() throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch start = new CountDownLatch(1);
        Callable<String> signup = () -> {
            start.await(5, TimeUnit.SECONDS);
            try {
                service.signup(new SignupRequest("same@example.com", "a", "고객", "01012345678"));
                return "SUCCESS";
            } catch (BusinessException exception) {
                assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.DUPLICATE_EMAIL);
                return "DUPLICATE";
            }
        };
        try {
            Future<String> first = executor.submit(signup);
            Future<String> second = executor.submit(signup);
            start.countDown();
            assertThat(java.util.List.of(first.get(10, TimeUnit.SECONDS), second.get(10, TimeUnit.SECONDS)))
                    .containsExactlyInAnyOrder("SUCCESS", "DUPLICATE");
            assertThat(users.count()).isEqualTo(1);
        } finally {
            executor.shutdownNow();
        }
    }
}
