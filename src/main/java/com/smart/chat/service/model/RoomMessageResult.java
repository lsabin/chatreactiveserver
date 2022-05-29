package com.smart.chat.service.model;

import com.smart.chat.api.model.ChatError;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Builder
@Getter
@ToString
public class RoomMessageResult {

    private String roomId;
    private String senderUsername;
    private Long timestamp;
    private String body;
    private ChatError error;
}