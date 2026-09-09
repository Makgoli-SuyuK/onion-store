package com.example.onionstore.domain.user.service;

import com.example.onionstore.domain.user.dto.SignupRequest;
import com.example.onionstore.domain.user.dto.SignupResponse;
import com.example.onionstore.domain.user.entity.User;
import com.example.onionstore.domain.user.repository.UserRepository;
import com.example.onionstore.global.exception.BusinessException;
import com.example.onionstore.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public SignupResponse signup(SignupRequest request) {
        // 탈퇴한 회원의 이메일도 중복 검사에 포함한다.
        if (userRepository.existsByEmail(request.email())) {
            throw new BusinessException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }
        User user = User.customer(request.email(), passwordEncoder.encode(request.password()),
                request.name(), request.phoneNumber());
        try {
            // 저장소 트랜잭션 종료 후 예외를 처리해 실패한 트랜잭션에서 재조회하지 않는다.
            return SignupResponse.from(userRepository.saveAndFlush(user));
        } catch (DataIntegrityViolationException exception) {
            if (userRepository.existsByEmail(request.email())) {
                throw new BusinessException(ErrorCode.EMAIL_ALREADY_EXISTS);
            }
            throw exception;
        }
    }
}
