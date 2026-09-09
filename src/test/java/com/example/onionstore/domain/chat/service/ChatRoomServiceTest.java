package com.example.onionstore.domain.chat.service;

import com.example.onionstore.domain.chat.dto.request.ChatRoomCreateRequest;
import com.example.onionstore.domain.chat.dto.response.ChatRoomCreateResponse;
import com.example.onionstore.domain.chat.dto.response.ChatRoomListResponse;
import com.example.onionstore.domain.chat.entity.ChatRoom;
import com.example.onionstore.domain.chat.entity.ChatRoomStatus;
import com.example.onionstore.domain.chat.repository.ChatRoomRepository;
import com.example.onionstore.domain.user.entity.Role;
import com.example.onionstore.domain.user.entity.User;
import com.example.onionstore.domain.user.repository.UserRepository;
import com.example.onionstore.global.exception.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Transactional
class ChatRoomServiceTest {

    @Autowired
    ChatRoomService chatRoomService;
    @Autowired
    UserRepository userRepository;
    @Autowired
    ChatRoomRepository chatRoomRepository;

    // ===== 채팅방 생성 =====

    @Test
    @DisplayName("고객이 채팅방을 생성하면 WAITING 상태로 만들어진다.")
    void createRoom_success() {
        User customer = userRepository.save(
                new User("customer@test.com", "pw", "김고객", "01000000000", Role.CUSTOMER));

        ChatRoomCreateResponse response = chatRoomService.createRoom(
                customer.getId(), new ChatRoomCreateRequest("주문 취소 관련 문의"));

        assertThat(response.getRoomId()).isNotNull();
        assertThat(response.getTitle()).isEqualTo("주문 취소 관련 문의");
        assertThat(response.getStatus()).isEqualTo(ChatRoomStatus.WAITING);
        assertThat(response.getCustomerId()).isEqualTo(customer.getId());
    }

    @Test
    @DisplayName("관리자는 채팅방을 생성할 수 없다.")
    void createRoom_admin_rejected() {
        User admin = userRepository.save(
                new User("admin@test.com", "pw", "관리자", "01000000000", Role.ADMIN));

        assertThatThrownBy(() -> chatRoomService.createRoom(
                admin.getId(), new ChatRoomCreateRequest("문의")))
                .isInstanceOf(BusinessException.class);
    }

    // ===== 채팅방 목록 조회 =====

    @Test
    @DisplayName("고객이 목록 조회하면 본인이 만든 방만 조회")
    void getRooms_customer_only() {
        User me = userRepository.save(new User("me@test.com", "pw", "나", "01000000000", Role.CUSTOMER));
        User other = userRepository.save(new User("other@test.com", "pw", "남", "01011111110", Role.CUSTOMER));
        chatRoomRepository.save(new ChatRoom(me, "내 문의1"));
        chatRoomRepository.save(new ChatRoom(me, "내 문의2"));
        chatRoomRepository.save(new ChatRoom(other, "남의 문의"));

        Page<ChatRoomListResponse> result =
                chatRoomService.getRooms(me.getId(), null, PageRequest.of(0, 10));

        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent()).allMatch(r -> r.getCustomerId().equals(me.getId()));
    }

    @Test
    @DisplayName("관리자가 목록 조회하면 전체방이 나온다.")
    void getRooms_admin_all() {
        User admin = userRepository.save(new User("admin2@test.com", "pw", "관리자", "01022222222", Role.ADMIN));
        User c1 = userRepository.save(new User("c1@test.com", "pw", "고객1", "01033333333", Role.CUSTOMER));
        chatRoomRepository.save(new ChatRoom(c1, "문의 A"));
        chatRoomRepository.save(new ChatRoom(c1, "문의 B"));

        Page<ChatRoomListResponse> result =
                chatRoomService.getRooms(admin.getId(), null, PageRequest.of(0, 10));

        assertThat(result.getTotalElements()).isEqualTo(2);
    }

    @Test
    @DisplayName("관리자가 상태 필터로 조회하면 해당 상태 방만 나온다.")
    void getRooms_admin_statusFilter() {
        User admin = userRepository.save(new User("admin3@test.com", "pw", "관리자", "01044444444", Role.ADMIN));
        User c2 = userRepository.save(new User("c2@test.com", "pw", "고객", "01055555555", Role.CUSTOMER));
        chatRoomRepository.save(new ChatRoom(c2, "문의"));

        Page<ChatRoomListResponse> waiting =
                chatRoomService.getRooms(admin.getId(), ChatRoomStatus.WAITING, PageRequest.of(0, 10));
        Page<ChatRoomListResponse> completed =
                chatRoomService.getRooms(admin.getId(), ChatRoomStatus.COMPLETED, PageRequest.of(0, 10));

        assertThat(waiting.getTotalElements()).isEqualTo(1);
        assertThat(completed.getTotalElements()).isEqualTo(0);
    }
}