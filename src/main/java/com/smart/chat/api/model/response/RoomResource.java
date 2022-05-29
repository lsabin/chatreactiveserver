package com.smart.chat.api.model.response;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Builder
@Getter
@ToString
public class RoomResource {
    private final String name;
    private final String roomId;
}
