package com.example.onionstore.domain.chat.dto.request;

import com.example.onionstore.domain.chat.entity.ChatRoomStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ChatRoomStatusUpdateRequest {
@NotNull
private ChatRoomStatus status;

}
