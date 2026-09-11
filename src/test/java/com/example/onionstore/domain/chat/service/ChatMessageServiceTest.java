package com.example.onionstore.domain.chat.service;

import com.example.onionstore.domain.chat.dto.response.ChatMessageBroadcastResponse;
import com.example.onionstore.domain.chat.dto.response.ChatMessageListResponse;
import com.example.onionstore.domain.chat.entity.ChatMessage;
import com.example.onionstore.domain.chat.entity.ChatRoom;
import com.example.onionstore.domain.chat.entity.ChatRoomStatus;
import com.example.onionstore.domain.chat.repository.ChatMessageRepository;
import com.example.onionstore.domain.chat.repository.ChatRoomRepository;
import com.example.onionstore.domain.user.entity.Role;
import com.example.onionstore.domain.user.entity.User;
import com.example.onionstore.domain.user.repository.UserRepository;
import com.example.onionstore.global.exception.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class ChatMessageServiceTest {

    @Autowired ChatMessageService chatMessageService;
    @Autowired ChatMessageRepository chatMessageRepository;
    @Autowired ChatRoomRepository chatRoomRepository;
    @Autowired UserRepository userRepository;

    @Test
    @DisplayName("커서 없이 조회하면 최신 메시지부터 size개, hasNext true")
    void getMessages_firstPage(){
        User customer = userRepository.save(new User("m1@test.com","pw","고객","01000000001", Role.CUSTOMER));
        ChatRoom room = chatRoomRepository.save(new ChatRoom(customer, "문의"));
        for(int i = 1; i <= 5; i++){
            chatMessageRepository.save(new ChatMessage(room, customer, "메시지" +i));
        }
        ChatMessageListResponse result = chatMessageService.getMessages(customer.getId(), room.getId(), null,2);

        assertThat(result.getMessages()).hasSize(2);
        assertThat(result.getMessages().get(0).getMessage()).isEqualTo("메시지5");
        assertThat(result.isHasNext()).isTrue();
        assertThat(result.getNextCursor()).isEqualTo(result.getMessages().get(1).getMessageId());
    }

    @Test
    @DisplayName("커서로 조회하면 그보다 오래된 메시지가 나온다.")
    void getMessages_withCursor(){
        User customer = userRepository.save(new User("m2@test.com","pw","고객","01000000002", Role.CUSTOMER));
        ChatRoom room = chatRoomRepository.save(new ChatRoom(customer,"문의"));
        chatMessageRepository.save(new ChatMessage(room, customer, "첫 번째"));
        chatMessageRepository.save(new ChatMessage(room, customer, "두 번째"));
        ChatMessage third = chatMessageRepository.save(new ChatMessage(room, customer, "세번 째"));

        ChatMessageListResponse result =
                chatMessageService.getMessages(customer.getId(), room.getId(), third.getId(), 10);

        assertThat(result.getMessages()).hasSize(2);
        assertThat(result.isHasNext()).isFalse();
    }

    @Test
    @DisplayName("메시지가 size 이하면 hasNext는 false")
    void getMessages_noNext(){
        User customer = userRepository.save(new User("m3@test.com", "pw", "고객", "01000000002", Role.CUSTOMER));
        ChatRoom room = chatRoomRepository.save(new ChatRoom(customer, "문의"));
        chatMessageRepository.save(new ChatMessage(room, customer, "하나"));

        ChatMessageListResponse result = chatMessageService.getMessages(customer.getId(),room.getId(), null, 10);

        assertThat(result.getMessages()).hasSize(1);
        assertThat(result.isHasNext()).isFalse();
    }

    @Test
    @DisplayName("고객이 남의 채팅방 메시지를 조회하면 거부된다.")
    void getMessages_otherRoom_rejected() {
        User owner = userRepository.save(new User("m4@test.com", "pw", "주인", "01000000004", Role.CUSTOMER));
        User stranger = userRepository.save(new User("m5@test.com", "pw", "남", "01000000005", Role.CUSTOMER));
        ChatRoom room = chatRoomRepository.save(new ChatRoom(owner, "문의"));

        assertThatThrownBy(() -> chatMessageService.getMessages(stranger.getId(), room.getId(), null, 10))
                .isInstanceOf(BusinessException.class);
    }
    @Test
    @DisplayName("존재하지 않는 채팅방 메시지를 조회하면 예외가 발생한다.")
    void getMessages_roomNotFound() {
        User customer = userRepository.save(new User("m6@test.com", "pw", "고객", "01000000006", Role.CUSTOMER));

        assertThatThrownBy(() -> chatMessageService.getMessages(customer.getId(), 999L, null, 10))
                .isInstanceOf(BusinessException.class);
    }
    @Test
    @DisplayName("메시지를 보내면 저장되고 응답 만들어짐.")
    void sendMessage_success(){
        User customer = userRepository.save(new User("sm1@test.com", "pw","고객","01000000021",Role.CUSTOMER));
        ChatRoom room = chatRoomRepository.save(new ChatRoom(customer, "문의"));

        ChatMessageBroadcastResponse response =
                chatMessageService.sendMessage(customer.getId(), room.getId(), "배송 언제 오나요?");

        assertThat(response.getMessageId()).isNotNull();
        assertThat(response.getRoomId()).isEqualTo(room.getId());
        assertThat(response.getSenderId()).isEqualTo(customer.getId());
        assertThat(response.getMessage()).isEqualTo("배송 언제 오나요?");
        assertThat(response.getType()).isEqualTo("TALK");
    }
    @Test
    @DisplayName("빈 메시지는 보낼 수 없다.")
    void sendMessage_empty_rejected(){
        User customer = userRepository.save(new User ("sm2@test.com","pw","고객","01000000022",Role.CUSTOMER));
        ChatRoom room = chatRoomRepository.save(new ChatRoom(customer, "문의"));

        assertThatThrownBy(() -> chatMessageService.sendMessage(customer.getId(), room.getId(), " "))
                .isInstanceOf(BusinessException.class);
    }
    @Test
    @DisplayName("1000자 넘는 메시지는 보낼 수 없다.")
    void sendMessage_tooLong_rejected(){
        User customer = userRepository.save(new User ("sm3@test.com", "pw","고객","01000000023",Role.CUSTOMER));
        ChatRoom room = chatRoomRepository.save(new ChatRoom(customer, "문의"));
        String longMessage ="a".repeat(1001);

        assertThatThrownBy(() -> chatMessageService.sendMessage(customer.getId(), room.getId(), longMessage))
                .isInstanceOf(BusinessException.class);
    }
    @Test
    @DisplayName("완료된 문의에는 메시지를 보낼 수 없다.")
    void sendMessage_completedRoom_rejected(){
        User customer = userRepository.save(new User("sm4@test.com","pw","고객","01000000024",Role.CUSTOMER));
        ChatRoom room = chatRoomRepository.save(new ChatRoom(customer, "문의"));
        room.changeStatus(ChatRoomStatus.IN_PROGRESS);
        room.changeStatus(ChatRoomStatus.COMPLETED);
        chatRoomRepository.save(room);

        assertThatThrownBy(() -> chatMessageService.sendMessage(customer.getId(), room.getId(), "안녕하세요"))
                .isInstanceOf(BusinessException.class);
    }
    @Test
    @DisplayName("고객이 남의 채팅방에 메시지를 보내면 거부된다.")
    void sendMessage_otherRoom_rejected(){
        User owner = userRepository.save(new User("sm5@test.com", "pw", "주인", "01000000025", Role.CUSTOMER));
        User stranger = userRepository.save(new User("sm6@test.com", "pw", "남", "01000000026", Role.CUSTOMER));
        ChatRoom room = chatRoomRepository.save(new ChatRoom(owner, "문의" ));

        assertThatThrownBy(() -> chatMessageService.sendMessage(stranger.getId(),room.getId(),"안녕"))
                .isInstanceOf(BusinessException.class);
    }
    @Test
    @DisplayName("존재하지 않는 방에 메시지를 보내면 예외가 발생한다.")
    void sendMessage_roomNotFound(){
        User customer = userRepository.save(new User("sm7@test.com","pw", "고객", "01000000027", Role.CUSTOMER));

        assertThatThrownBy(() -> chatMessageService.sendMessage(customer.getId(), 999L,"안녕"))
                .isInstanceOf(BusinessException.class);
    }
}


