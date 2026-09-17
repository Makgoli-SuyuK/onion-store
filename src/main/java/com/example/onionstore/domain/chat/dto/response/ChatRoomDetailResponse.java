package com.example.onionstore.domain.chat.dto.response;

import com.example.onionstore.domain.chat.entity.ChatRoomStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class ChatRoomDetailResponse {

    private Long roomId;
    private String title;
    private ChatRoomStatus status;
    private Long customerId;
    private String customerName;
    private LocalDateTime createdDate;

}
