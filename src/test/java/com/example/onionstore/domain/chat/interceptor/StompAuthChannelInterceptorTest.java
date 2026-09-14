package com.example.onionstore.domain.chat.interceptor;

import com.example.onionstore.domain.chat.entity.ChatRoom;
import com.example.onionstore.domain.chat.repository.ChatRoomRepository;
import com.example.onionstore.domain.user.entity.Role;
import com.example.onionstore.domain.user.entity.User;
import com.example.onionstore.domain.user.entity.UserStatus;
import com.example.onionstore.domain.user.repository.UserRepository;
import com.example.onionstore.global.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.security.Principal;
import java.util.Optional;
import java.util.OptionalInt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.startsWith;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class StompAuthChannelInterceptorTest {

    private final JwtDecoder jwtDecoder = mock(JwtDecoder.class);
    private final UserRepository userRepository = mock(UserRepository.class);
    private final ChatRoomRepository chatRoomRepository = mock(ChatRoomRepository.class);
    private final MessageChannel channel = mock(MessageChannel.class);

    private final StompAuthChannelInterceptor interceptor =
            new StompAuthChannelInterceptor(jwtDecoder, userRepository, chatRoomRepository);

    // CONNECT
    @Test
    void 정상_토큰이면_Principal이_세팅된다() {
        // given
        Message<byte[]> connectMessage = connectMessageWithAuth("Bearer valid-token");

        Jwt fakeJwt = Jwt.withTokenValue("valid-token")
                .header("alg", "none")
                .subject("1")
                .build();
        when(jwtDecoder.decode("valid-token")).thenReturn(fakeJwt);

        User activeUser = new User("a@test.com", "pw", "고객", "010", Role.CUSTOMER);
        ReflectionTestUtils.setField(activeUser, "id", 1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(activeUser));

        // when
        interceptor.preSend(connectMessage, channel);

        // then
        StompHeaderAccessor resultAccessor =
                MessageHeaderAccessor.getAccessor(connectMessage, StompHeaderAccessor.class);
        AuthenticatedUser principal = (AuthenticatedUser) resultAccessor.getUser();

        assertThat(principal.getUserId()).isEqualTo(1L);
        assertThat(principal.getRole()).isEqualTo(Role.CUSTOMER);
    }

    @Test
    void Authorization_헤더_없으면_예외() {
        Message<byte[]> connentMessage = connectMessageWithAuth(null);
        assertThatThrownBy(() -> interceptor.preSend(connentMessage, channel))
                .isInstanceOf(IllegalArgumentException.class);
    }


    @Test
    void Bearer_형식_아니면_예외() {
        Message<byte[]> connectMessage = connectMessageWithAuth("Basic abcdef");

        assertThatThrownBy(() -> interceptor.preSend(connectMessage, channel))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void subject가_숫자아니면_예외() {
        Message<byte[]> connectMessage = connectMessageWithAuth("Bearer weird-token");

        Jwt fakeJwt = Jwt.withTokenValue("weird-token")
                .header("alg", "none")
                .subject("abc")
                .build();
        when(jwtDecoder.decode("weird-token")).thenReturn(fakeJwt);

        assertThatThrownBy(() -> interceptor.preSend(connectMessage, channel))
                .isInstanceOf(NumberFormatException.class);
    }

    @Test
    void 회원_없으면_예외() {
        Message<byte[]> connectMessage = connectMessageWithAuth("Bearer valid-token");

        Jwt fakeJwt = Jwt.withTokenValue("valid-token")
                .header("alg", "none")
                .subject("999")
                .build();
        when(jwtDecoder.decode("valid-token")).thenReturn(fakeJwt);
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> interceptor.preSend(connectMessage, channel))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void 탈퇴한_회원이면_예외() {
        Message<byte[]> connectMessage = connectMessageWithAuth("Bearer valid-token");

        Jwt fakeJwt = Jwt.withTokenValue("valid-token")
                .header("alg", "none")
                .subject("1")
                .build();
        when(jwtDecoder.decode("valid-token")).thenReturn(fakeJwt);

        User deletedUser = new User("a@test.com", "pw", "고객", "010", Role.CUSTOMER);
        ReflectionTestUtils.setField(deletedUser, "id", 1L);
        ReflectionTestUtils.setField(deletedUser, "status", UserStatus.DELETED);
        when(userRepository.findById(1L)).thenReturn(Optional.of(deletedUser));

        assertThatThrownBy(() -> interceptor.preSend(connectMessage, channel))
                .isInstanceOf(BusinessException.class);
    }

    private Message<byte[]> connectMessageWithAuth(String authorizationHeaderValue) {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.CONNECT);
        if (authorizationHeaderValue != null) {
            accessor.addNativeHeader("Authorization", authorizationHeaderValue);
        }
        accessor.setLeaveMutable(true);
        return MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());
    }

// SUBSCRIBE

    @Test
    void 본인_방_구독하면_통과한다() {
        ChatRoom room = roomOwnedBy(1L, 10L);
        AuthenticatedUser customer = new AuthenticatedUser(1L, Role.CUSTOMER);

        interceptor.preSend(subscribeMessage("/sub/chats/rooms/10", customer), channel);
    }

    @Test
    void 남의_방_구독하면_예외(){
        ChatRoom room = roomOwnedBy(1L, 10L);
        AuthenticatedUser stranger = new AuthenticatedUser(2L, Role.CUSTOMER);

        assertThatThrownBy(() -> interceptor.preSend(subscribeMessage("/sub/chats/rooms/10",stranger),channel))
                .isInstanceOf(BusinessException.class);

    }

    @Test
    void 관리자는_아무_방이나_구독_가능(){
        ChatRoom room = roomOwnedBy(1L,10L);
        AuthenticatedUser admin = new AuthenticatedUser(99L,Role.ADMIN);

        interceptor.preSend(subscribeMessage("/sub/chats/rooms/10", admin), channel);
    }

    @Test
    void 존재하지_않는_방을_구독하면_예외(){
        when(chatRoomRepository.findById(999L)).thenReturn(Optional.empty());
        AuthenticatedUser customer = new AuthenticatedUser(1L, Role.CUSTOMER);

        assertThatThrownBy(() ->
                interceptor.preSend(subscribeMessage("/sub/chats/rooms/999", customer), channel))
                .isInstanceOf(BusinessException.class);
    }
    @Test
    void 인증_안된_상태로_구독하면_예외(){
        assertThatThrownBy(() ->
                interceptor.preSend(subscribeMessage("/sub/chats/rooms/10", null), channel))
                .isInstanceOf(BusinessException.class);
    }
    @Test
    void 잘못된_destination이면_예외(){
        AuthenticatedUser customer = new AuthenticatedUser(1L, Role.CUSTOMER);

        assertThatThrownBy(() ->
                interceptor.preSend(subscribeMessage("/sub/anything/1", customer), channel))
                .isInstanceOf(BusinessException.class);
    }

    private ChatRoom roomOwnedBy(Long ownerId, Long roomId){
        User owner = new User("owner@test.com","pw","주인","010",Role.CUSTOMER);
        ReflectionTestUtils.setField(owner, "id", ownerId);
        ChatRoom room = new ChatRoom(owner, "문의");
        ReflectionTestUtils.setField(room,"id",roomId);
        when(chatRoomRepository.findById(roomId)).thenReturn(Optional.of(room));
        return room;
    }

    private Message<byte[]> subscribeMessage(String destination, Principal principal){
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.SUBSCRIBE);
        accessor.setDestination(destination);
        accessor.setUser(principal);
        accessor.setLeaveMutable(true);
        return MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());
    }
}