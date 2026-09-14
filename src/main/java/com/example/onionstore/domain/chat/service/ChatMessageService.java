package com.example.onionstore.domain.chat.service;

import com.example.onionstore.domain.chat.dto.response.ChatMessageBroadcastResponse;
import com.example.onionstore.domain.chat.dto.response.ChatMessageListResponse;
import com.example.onionstore.domain.chat.dto.response.ChatMessageResponse;
import com.example.onionstore.domain.chat.entity.ChatMessage;
import com.example.onionstore.domain.chat.entity.ChatRoom;
import com.example.onionstore.domain.chat.entity.ChatRoomStatus;
import com.example.onionstore.domain.chat.repository.ChatMessageRepository;
import com.example.onionstore.domain.chat.repository.ChatRoomRepository;
import com.example.onionstore.domain.user.entity.Role;
import com.example.onionstore.domain.user.entity.User;
import com.example.onionstore.domain.user.repository.UserRepository;
import com.example.onionstore.global.exception.BusinessException;
import com.example.onionstore.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatMessageService {

    private final ChatMessageRepository chatMessageRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final UserRepository userRepository;

    public ChatMessageListResponse getMessages(Long userId, Long roomId, Long lastMessageId, int size) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        ChatRoom room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CHAT_ROOM_NOT_FOUND));

        if (user.getRole() == Role.CUSTOMER && !room.getUser().getId().equals(userId)) {
            throw new BusinessException(ErrorCode.CHAT_ROOM_ACCESS_DENIED);
        }

        PageRequest pageable = PageRequest.of(0, size + 1);

        List<ChatMessage> rows;
        if (lastMessageId == null) {
            rows = chatMessageRepository.findRecentMessages(roomId, pageable);
        } else {
            rows = chatMessageRepository.findMessagesBefore(roomId, lastMessageId, pageable);
        }

        boolean hasNext = rows.size() > size;
        if (hasNext) {
            rows = rows.subList(0, size);
        }

        List<ChatMessageResponse> messages = rows.stream()
                .map(ChatMessageResponse::new)
                .toList();

        Long nextCursor = null;
        if (!messages.isEmpty()) {
            nextCursor = messages.get(messages.size() - 1).getMessageId();
        }

        return new ChatMessageListResponse(messages, nextCursor, hasNext);
    }

    @Transactional
    public ChatMessageBroadcastResponse sendMessage(Long userId, Long roomId, String content){
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        ChatRoom room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CHAT_ROOM_NOT_FOUND));
        if(user.getRole() == Role.CUSTOMER && !room.getUser().getId().equals(userId)){
            throw new BusinessException(ErrorCode.CHAT_ROOM_ACCESS_DENIED);
        }

        if(content == null || content.isBlank()){
            throw new BusinessException(ErrorCode.CHAT_MESSAGE_EMPTY);
        }
        if(content.length() > 1000){
            throw new BusinessException(ErrorCode.INVALID_CHAT_MESSAGE_LENGTH);
        }
        if(room.getStatus() == ChatRoomStatus.COMPLETED){
            throw new BusinessException(ErrorCode.CANNOT_SEND_TO_COMPLETED_CHAT_ROOM);
        }

        ChatMessage saved = chatMessageRepository.save(new ChatMessage(room, user, content));

        return new ChatMessageBroadcastResponse(
                roomId,
                saved.getId(),
                user.getId(),
                user.getName(),
                saved.getMessage(),
                saved.getCreatedAt()
        );
    }
}



