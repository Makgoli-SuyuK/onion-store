package com.example.onionstore.domain.chat.entity;

import com.example.onionstore.global.entity.BaseTimeEntity;
import com.example.onionstore.domain.user.entity.User;
import com.example.onionstore.global.exception.BusinessException;
import com.example.onionstore.global.exception.ErrorCode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Getter
@Entity
@Table(name = "chat_rooms")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatRoom extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @Column(nullable = false, length = 50)
    private String title;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ChatRoomStatus status;

    public ChatRoom(User user, String title) {
        this.user = user;
        this.title = title;
        this.status = ChatRoomStatus.WAITING;
    }

    public void changeStatus(ChatRoomStatus next) {
        if (this.status == ChatRoomStatus.COMPLETED) {
            throw new BusinessException(ErrorCode.INVALID_CHAT_ROOM_STATUS_TRANSITION);
        }
        this.status = next;
    }
}
