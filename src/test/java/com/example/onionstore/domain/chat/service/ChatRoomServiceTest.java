package com.example.onionstore.domain.chat.service;


import com.example.onionstore.domain.chat.dto.request.ChatRoomCreateRequest;
import com.example.onionstore.domain.chat.dto.response.ChatRoomCreateResponse;
import com.example.onionstore.domain.chat.entity.ChatRoomStatus;
import com.example.onionstore.domain.user.entity.Role;
import com.example.onionstore.domain.user.entity.User;
import com.example.onionstore.domain.user.repository.UserRepository;
import com.example.onionstore.global.exception.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Transactional

public class ChatRoomServiceTest {

    @Autowired
    ChatRoomService chatRoomService;

    @Autowired
    UserRepository userRepository;

    @Test
    @DisplayName("고객이 채팅방을 생성하면 WAITING 상태로 만들어진다.")

    void createRoom_success() {
        //given
        User customer = userRepository.save(
                new User("customer@test.com", "pw", "김고객", "010000000", Role.CUSTOMER));
        //when
        ChatRoomCreateResponse response = chatRoomService.createRoom(
                customer.getId(), new ChatRoomCreateRequest("주문 취소 관련 문의"));
        //then
        assertThat(response.getRoomId()).isNotNull();
        assertThat(response.getTitle()).isEqualTo("주문 취소 관련 문의");
        assertThat(response.getStatus()).isEqualTo(ChatRoomStatus.WAITING);
        assertThat(response.getCustomerId()).isEqualTo(customer.getId());
    }
    @Test
    @DisplayName("관리자는 채팅방을 생성할 수 없다.")
    void createRoom_admin_rejected() {
        // given
        User admin = userRepository.save(
                new User("admin@test.com", "pw", "관리자", "010000000", Role.ADMIN));

        // when & then
        assertThatThrownBy(() -> chatRoomService.createRoom(
                admin.getId(), new ChatRoomCreateRequest("문의")))
                .isInstanceOf(BusinessException.class);
    }
}
