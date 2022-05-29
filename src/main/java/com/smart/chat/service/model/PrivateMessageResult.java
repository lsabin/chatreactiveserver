package com.smart.chat.service.model;

import com.smart.chat.api.model.ChatError;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Builder
@Getter
@ToString
public class PrivateMessageResult {

    private String senderUsername;
    private String destinationUsername;
    private Long timestamp;
    private ChatError error;
}