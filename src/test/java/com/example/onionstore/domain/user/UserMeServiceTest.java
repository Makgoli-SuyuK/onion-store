package com.example.onionstore.domain.user;

import com.example.onionstore.domain.user.dto.UserMeResponse;
import com.example.onionstore.domain.user.entity.Role;
import com.example.onionstore.domain.user.entity.User;
import com.example.onionstore.domain.user.repository.UserRepository;
import com.example.onionstore.domain.user.service.AuthService;
import com.example.onionstore.global.exception.BusinessException;
import com.example.onionstore.global.exception.ErrorCode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class UserMeServiceTest {
    @Mock UserRepository userRepository;
    @InjectMocks AuthService authService;

    @Test
    void returnsProfileWithoutPassword() {
        User user = new User("me@example.com", "hashed", "사용자", "01012345678", Role.CUSTOMER);
        given(userRepository.findById(1L)).willReturn(Optional.of(user));

        UserMeResponse response = authService.getMe(1L);

        assertThat(response.email()).isEqualTo("me@example.com");
        assertThat(response.role()).isEqualTo(Role.CUSTOMER);
    }

    @Test
    void missingUserReturnsUserNotFound() {
        given(userRepository.findById(1L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> authService.getMe(1L))
                .isInstanceOfSatisfying(BusinessException.class,
                        e -> assertThat(e.getErrorCode()).isEqualTo(ErrorCode.USER_NOT_FOUND));
    }
}
