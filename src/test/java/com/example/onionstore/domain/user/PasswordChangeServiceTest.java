package com.example.onionstore.domain.user;

import com.example.onionstore.domain.user.entity.Role;
import com.example.onionstore.domain.user.entity.User;
import com.example.onionstore.domain.user.repository.UserRepository;
import com.example.onionstore.domain.user.service.UserService;
import com.example.onionstore.global.exception.BusinessException;
import com.example.onionstore.global.exception.ErrorCode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class PasswordChangeServiceTest {
    @Mock UserRepository userRepository;
    @Mock PasswordEncoder passwordEncoder;
    @InjectMocks UserService userService;

    @Test
    void changesPasswordAfterVerifyingCurrentPassword() {
        User user = new User("me@example.com", "old-hash", "이름", "01011111111", Role.CUSTOMER);
        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(passwordEncoder.matches("old-password", "old-hash")).willReturn(true);
        given(passwordEncoder.encode("new-password")).willReturn("new-hash");

        userService.changePassword(1L, "old-password", "new-password");

        assertThat(user.getPassword()).isEqualTo("new-hash");
    }

    @Test
    void rejectsPasswordChangeWhenCurrentPasswordDoesNotMatch() {
        User user = new User("me@example.com", "old-hash", "이름", "01011111111", Role.CUSTOMER);
        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(passwordEncoder.matches("wrong-password", "old-hash")).willReturn(false);

        assertThatThrownBy(() -> userService.changePassword(1L, "wrong-password", "new-password"))
                .isInstanceOfSatisfying(BusinessException.class,
                        e -> assertThat(e.getErrorCode()).isEqualTo(ErrorCode.INVALID_CREDENTIALS));
        assertThat(user.getPassword()).isEqualTo("old-hash");
    }
}
