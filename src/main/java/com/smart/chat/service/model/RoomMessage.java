package com.smart.chat.service.model;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Builder
@Getter
@ToString
public class RoomMessage {
    private String username;
    private String roomId;
    private String body;
}
