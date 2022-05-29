package com.smart.chat.api.model.response;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Builder
@Getter
@ToString
public class RoomMessageResource {
    private final String username;
    private final String roomId;
    private final Long timestamp;
    private final String body;
}
