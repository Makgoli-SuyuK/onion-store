package com.example.onionstore.domain.user.service;

import com.example.onionstore.domain.user.dto.UserMeResponse;
import com.example.onionstore.domain.user.dto.UserProfileUpdateRequest;
import com.example.onionstore.domain.user.entity.User;
import com.example.onionstore.domain.user.repository.UserRepository;
import com.example.onionstore.global.exception.BusinessException;
import com.example.onionstore.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public UserMeResponse getMe(Long userId) {
        return UserMeResponse.from(findUser(userId));
    }

    @Transactional
    public UserMeResponse updateProfile(Long userId, UserProfileUpdateRequest request) {
        User user = findUser(userId);
        user.updateProfile(request.name(), request.phoneNumber());
        return UserMeResponse.from(user);
    }

    @Transactional
    public void changePassword(Long userId, String currentPassword, String newPassword) {
        User user = findUser(userId);
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }
        user.updatePassword(passwordEncoder.encode(newPassword));
    }

    @Transactional(readOnly = true)
    public User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }
}
