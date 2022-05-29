package com.smart.chat.service.model;

import com.smart.chat.api.model.ChatError;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Builder
@Getter
@ToString
public class SaveRoomResult {
    private final String name;
    private final String roomId;
    private ChatError error;
}
