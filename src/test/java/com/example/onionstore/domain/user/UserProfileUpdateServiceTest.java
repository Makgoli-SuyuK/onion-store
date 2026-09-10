package com.example.onionstore.domain.user;

import com.example.onionstore.domain.user.dto.UserProfileUpdateRequest;
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
class UserProfileUpdateServiceTest {
    @Mock UserRepository userRepository;
    @Mock PasswordEncoder passwordEncoder;
    @InjectMocks UserService userService;

    @Test
    void updatesOnlyNameAndPhoneNumber() {
        User user = new User("me@example.com", "hashed", "기존 이름", "01011111111", Role.CUSTOMER);
        given(userRepository.findById(1L)).willReturn(Optional.of(user));

        var response = userService.updateProfile(1L, new UserProfileUpdateRequest("새 이름", "01022222222"));

        assertThat(response.name()).isEqualTo("새 이름");
        assertThat(response.phoneNumber()).isEqualTo("01022222222");
        assertThat(response.email()).isEqualTo("me@example.com");
        assertThat(user.getPassword()).isEqualTo("hashed");
    }

    @Test
    void omittedFieldsRemainUnchanged() {
        User user = new User("me@example.com", "hashed", "기존 이름", "01011111111", Role.CUSTOMER);
        given(userRepository.findById(1L)).willReturn(Optional.of(user));

        var response = userService.updateProfile(1L, new UserProfileUpdateRequest(null, "01022222222"));

        assertThat(response.name()).isEqualTo("기존 이름");
        assertThat(response.phoneNumber()).isEqualTo("01022222222");
    }

    @Test
    void missingUserReturnsUserNotFound() {
        given(userRepository.findById(1L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> userService.updateProfile(1L, new UserProfileUpdateRequest("이름", null)))
                .isInstanceOfSatisfying(BusinessException.class,
                        e -> assertThat(e.getErrorCode()).isEqualTo(ErrorCode.USER_NOT_FOUND));
    }
}
