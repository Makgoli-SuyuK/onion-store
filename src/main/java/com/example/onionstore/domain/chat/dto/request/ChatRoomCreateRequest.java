package com.example.onionstore.domain.chat.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;


@Getter
@AllArgsConstructor
public class   ChatRoomCreateRequest{

    @NotBlank
    @Size(min = 1, max = 50)
    private String title;
}





