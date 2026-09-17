package com.example.onionstore.domain.user;

import com.example.onionstore.domain.user.dto.LoginRequest;
import com.example.onionstore.domain.user.entity.Role;
import com.example.onionstore.domain.user.entity.User;
import com.example.onionstore.domain.user.entity.UserStatus;
import com.example.onionstore.domain.user.repository.UserRepository;
import com.example.onionstore.domain.user.service.AuthService;
import com.example.onionstore.global.exception.BusinessException;
import com.example.onionstore.global.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class LoginIntegrationTest {
    @Autowired AuthService authService;
    @Autowired UserRepository userRepository;
    @Autowired PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    void activeUserWithCorrectPasswordCanLogin() {
        userRepository.save(User.customer("login@example.com", passwordEncoder.encode("pw"), "고객", "01012345678"));

        User user = authService.login("login@example.com", "pw");

        assertThat(user.getRole()).isEqualTo(Role.CUSTOMER);
    }

    @Test
    void wrongPasswordUnknownEmailAndDeletedUserUseSameError() {
        User user = userRepository.save(User.customer("login@example.com", passwordEncoder.encode("pw"), "고객", "01012345678"));
        assertThatThrownBy(() -> authService.login("login@example.com", "wrong"))
                .isInstanceOfSatisfying(BusinessException.class,
                        e -> assertThat(e.getErrorCode()).isEqualTo(ErrorCode.INVALID_CREDENTIALS));
        assertThatThrownBy(() -> authService.login("missing@example.com", "pw"))
                .isInstanceOfSatisfying(BusinessException.class,
                        e -> assertThat(e.getErrorCode()).isEqualTo(ErrorCode.INVALID_CREDENTIALS));
        ReflectionTestUtils.setField(user, "status", UserStatus.DELETED);
        assertThatThrownBy(() -> authService.login("login@example.com", "pw"))
                .isInstanceOfSatisfying(BusinessException.class,
                        e -> assertThat(e.getErrorCode()).isEqualTo(ErrorCode.INVALID_CREDENTIALS));
    }
}
