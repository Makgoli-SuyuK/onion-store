package com.example.onionstore.domain.chat.interceptor;

import com.example.onionstore.domain.chat.entity.ChatRoom;
import com.example.onionstore.domain.chat.repository.ChatRoomRepository;
import com.example.onionstore.domain.user.entity.Role;
import com.example.onionstore.domain.user.entity.User;
import com.example.onionstore.domain.user.entity.UserStatus;
import com.example.onionstore.domain.user.repository.UserRepository;
import com.example.onionstore.global.exception.BusinessException;
import com.example.onionstore.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StompAuthChannelInterceptor implements ChannelInterceptor {

    private final JwtDecoder jwtDecoder;
    private final UserRepository userRepository;
    private final ChatRoomRepository chatRoomRepository;


    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor =
                MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if(accessor == null){
            return message;
        }

        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            String bearer = accessor.getFirstNativeHeader("Authorization");
            if (bearer == null || !bearer.startsWith("Bearer ")) {
                throw new IllegalArgumentException("토큰이 없습니다.");
            }

            Jwt jwt = jwtDecoder.decode(bearer.substring(7));
            Long userId = Long.valueOf(jwt.getSubject());

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

            if (user.getStatus() == UserStatus.DELETED) {
                throw new BusinessException(ErrorCode.UNAUTHORIZED);
            }

            accessor.setUser(new AuthenticatedUser(user.getId(),user.getRole()));
        }
        else if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())){
            AuthenticatedUser authenticatedUser = (AuthenticatedUser) accessor.getUser();

            Long roomId = extractRoomId(accessor.getDestination());

            ChatRoom room = chatRoomRepository.findById(roomId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.CHAT_ROOM_NOT_FOUND));

            if(authenticatedUser.getRole() == Role.CUSTOMER
            && !room.getUser().getId().equals(authenticatedUser.getUserId())){
                throw new BusinessException(ErrorCode.CHAT_ROOM_ACCESS_DENIED);
            }
        }
        return message;
    }

    private Long extractRoomId(String destination){
        String[] parts = destination.split("/");
        return Long.valueOf(parts[parts.length - 1]);
    }
}
