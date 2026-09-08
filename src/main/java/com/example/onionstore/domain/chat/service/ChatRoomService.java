package com.example.onionstore.domain.chat.service;

import com.example.onionstore.domain.chat.dto.request.ChatRoomCreateRequest;
import com.example.onionstore.domain.chat.dto.response.ChatRoomCreateResponse;
import com.example.onionstore.domain.chat.entity.ChatRoom;
import com.example.onionstore.domain.chat.repository.ChatRoomRepository;
import com.example.onionstore.domain.user.entity.Role;
import com.example.onionstore.domain.user.entity.User;
import com.example.onionstore.domain.user.repository.UserRepository;
import com.example.onionstore.global.exception.BusinessException;
import com.example.onionstore.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)

public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;
    private final UserRepository userRepository;

    @Transactional
    public ChatRoomCreateResponse createRoom(Long userId, ChatRoomCreateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (user.getRole() == Role.ADMIN) {
            throw new BusinessException(ErrorCode.CHAT_ROOM_CREATE_FORBIDDEN_FOR_ADMIN);
        }
        ChatRoom chatRoom = chatRoomRepository.save(new ChatRoom(user, request.getTitle()));

        return new ChatRoomCreateResponse(
                chatRoom.getId(),
                chatRoom.getTitle(),
                chatRoom.getStatus(),
                chatRoom.getUser().getId(),
                chatRoom.getCreatedAt()
        );
    }
}



