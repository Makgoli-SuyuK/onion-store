package com.example.onionstore.domain.chat.entity;

import com.example.onionstore.global.entity.BaseCreatedTimeEntity;
import com.example.onionstore.domain.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "chat_messages", indexes = {
        @Index(name = "idx_chat_message_room_id", columnList = "chat_room_id,id")
})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatMessage extends BaseCreatedTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "chat_room_id", nullable = false)
    private ChatRoom chatRoom;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @Lob
    @Column(nullable = false)
    private String message;

    public ChatMessage(ChatRoom chatRoom, User sender, String message) {
        this.chatRoom = chatRoom;
        this.sender = sender;
        this.message = message;
    }
}
